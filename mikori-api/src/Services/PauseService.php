<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Core\Exceptions\ValidationException;
use Mikori\Repositories\ChildRepository;
use Mikori\Repositories\PauseRepository;
use Mikori\Support\Clock;

/**
 * Pausas temporales (V2): bloqueo total del dispositivo hasta cierta hora.
 */
final class PauseService
{
    private ChildRepository $children;
    private PauseRepository $pauses;

    public function __construct()
    {
        $this->children = new ChildRepository();
        $this->pauses = new PauseRepository();
    }

    /**
     * Inicia una pausa. Acepta { minutes: N } o { until: "Y-m-d H:i:s" }.
     * @param array<string,mixed> $payload
     */
    public function start(int $userId, int $childId, array $payload): array
    {
        $this->requireOwned($userId, $childId);

        $until = null;
        if (isset($payload['minutes']) && $payload['minutes'] !== null) {
            $minutes = (int) $payload['minutes'];
            if ($minutes < 1 || $minutes > 1440) {
                throw new ValidationException(['minutes' => 'Debe estar entre 1 y 1440.']);
            }
            $until = Clock::inMinutes($minutes);
        } elseif (isset($payload['until']) && is_string($payload['until'])) {
            $ts = strtotime($payload['until']);
            if ($ts === false || $ts <= time()) {
                throw new ValidationException(['until' => 'Fecha/hora futura inválida.']);
            }
            $until = date('Y-m-d H:i:s', $ts);
        } else {
            throw new ValidationException(['pause' => "Envía 'minutes' o 'until'."]);
        }

        $this->pauses->start($childId, $until);
        return ['pause_until' => $until];
    }

    public function cancel(int $userId, int $childId): void
    {
        $this->requireOwned($userId, $childId);
        $this->pauses->cancel($childId);
    }

    private function requireOwned(int $userId, int $childId): void
    {
        if ($this->children->findForUser($childId, $userId) === null) {
            throw new HttpException(404, 'child_not_found', 'No se encontró el hijo.');
        }
    }
}
