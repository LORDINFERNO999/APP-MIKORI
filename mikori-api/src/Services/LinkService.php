<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Core\Env;
use Mikori\Core\Exceptions\HttpException;
use Mikori\Repositories\ChildRepository;
use Mikori\Repositories\DeviceLinkRepository;
use Mikori\Repositories\DeviceRepository;
use Mikori\Support\Clock;
use Mikori\Support\Token;

/**
 * Vinculación padre ↔ dispositivo del hijo mediante código con expiración.
 *
 * Flujo:
 *   1. El padre genera un código (MIKORI-XXXXXX) para un hijo.
 *   2. La app Kids canjea el código enviando la info del dispositivo.
 *   3. El servidor valida (código pendiente y no expirado), crea/reasigna el
 *      dispositivo y entrega un token de dispositivo para futuras peticiones.
 */
final class LinkService
{
    private ChildRepository $children;
    private DeviceLinkRepository $links;
    private DeviceRepository $devices;

    public function __construct()
    {
        $this->children = new ChildRepository();
        $this->links = new DeviceLinkRepository();
        $this->devices = new DeviceRepository();
    }

    /**
     * Genera un código de vinculación para un hijo del usuario.
     * @return array<string,mixed>
     */
    public function generate(int $userId, int $childId): array
    {
        $child = $this->children->findForUser($childId, $userId);
        if ($child === null) {
            throw new HttpException(404, 'child_not_found', 'No se encontró el hijo.');
        }

        $ttl = Env::int('LINK_CODE_TTL_MINUTES', 15);
        $expiresAt = Clock::inMinutes($ttl);

        // Genera un código único (reintenta ante colisión improbable).
        $code = $this->uniqueCode();
        $this->links->create($userId, $childId, $code, $expiresAt);

        return [
            'code' => $code,
            'expires_at' => $expiresAt,
            'expires_in' => $ttl * 60,
        ];
    }

    /**
     * Canje del código desde la app Kids.
     * @return array<string,mixed>
     */
    public function redeem(string $code, string $deviceUid, ?string $model, ?string $androidVersion, ?string $fcmToken): array
    {
        $link = $this->links->findPendingByCode($code);
        if ($link === null) {
            throw new HttpException(404, 'invalid_code', 'El código no es válido o ya fue utilizado.');
        }

        if (Clock::isPast($link['expires_at'])) {
            $this->links->markExpired((int) $link['id']);
            throw new HttpException(410, 'code_expired', 'El código de vinculación expiró. Genera uno nuevo.');
        }

        $deviceToken = Token::random();
        $tokenHash = Token::hash($deviceToken);
        $childId = (int) $link['child_id'];

        // Si el dispositivo ya existía (por device_uid), se reasigna; si no, se crea.
        $existing = $this->devices->findByUid($deviceUid);
        if ($existing !== null) {
            $deviceId = (int) $existing['id'];
            $this->devices->relink($deviceId, $childId, $model, $androidVersion, $fcmToken, $tokenHash);
        } else {
            $deviceId = $this->devices->create($childId, $deviceUid, $model, $androidVersion, $fcmToken, $tokenHash);
        }

        $this->links->markLinked((int) $link['id'], $deviceId);

        return [
            'device_id' => $deviceId,
            'child_id' => $childId,
            'device_token' => $deviceToken,
            'token_type' => 'Bearer',
        ];
    }

    /**
     * Estado de la última vinculación de un hijo (consulta del padre).
     * @return array<string,mixed>
     */
    public function status(int $userId, int $childId): array
    {
        $child = $this->children->findForUser($childId, $userId);
        if ($child === null) {
            throw new HttpException(404, 'child_not_found', 'No se encontró el hijo.');
        }

        $link = $this->links->latestForChild($childId, $userId);
        if ($link === null) {
            return ['status' => 'none'];
        }

        // Marca como expirado si corresponde (limpieza perezosa).
        $status = $link['status'];
        if ($status === 'pending' && Clock::isPast($link['expires_at'])) {
            $this->links->markExpired((int) $link['id']);
            $status = 'expired';
        }

        return [
            'status' => $status,
            'code' => $link['code'],
            'expires_at' => $link['expires_at'],
            'linked_device_id' => $link['linked_device_id'] !== null ? (int) $link['linked_device_id'] : null,
            'linked_at' => $link['linked_at'],
        ];
    }

    private function uniqueCode(): string
    {
        for ($i = 0; $i < 5; $i++) {
            $code = Token::linkCode();
            if ($this->links->findPendingByCode($code) === null) {
                return $code;
            }
        }
        // Fallback extremadamente improbable.
        return 'MIKORI-' . random_int(100000, 999999);
    }
}
