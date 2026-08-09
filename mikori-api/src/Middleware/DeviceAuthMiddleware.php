<?php

declare(strict_types=1);

namespace Mikori\Middleware;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Core\Request;
use Mikori\Repositories\DeviceRepository;
use Mikori\Support\Token;

/**
 * Middleware de autenticación para la app MIKORI Kids.
 *
 * El dispositivo recibe un token opaco al vincularse (redeem). Se valida
 * comparando su hash contra devices.api_token_hash. Adjunta el dispositivo
 * autenticado al Request.
 */
final class DeviceAuthMiddleware
{
    public static function handle(Request $request): void
    {
        $token = $request->bearerToken();
        if ($token === null || $token === '') {
            throw new HttpException(401, 'unauthenticated', 'Falta el token del dispositivo.');
        }

        $devices = new DeviceRepository();
        $device = $devices->findByApiTokenHash(Token::hash($token));
        if ($device === null) {
            throw new HttpException(401, 'invalid_device_token', 'Token de dispositivo inválido.');
        }

        $request->authContext = ['device' => $device];
    }
}
