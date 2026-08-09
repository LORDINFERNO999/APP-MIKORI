<?php

declare(strict_types=1);

namespace Mikori\Middleware;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Core\Request;
use Mikori\Repositories\SessionRepository;
use Mikori\Repositories\UserRepository;
use Mikori\Support\Token;

/**
 * Middleware de autenticación por token Bearer.
 *
 * Valida el token contra la tabla sessions (comparando su hash) y adjunta
 * el usuario autenticado al Request. Lanza 401 si el token falta o es inválido.
 */
final class AuthMiddleware
{
    public static function handle(Request $request): void
    {
        $token = $request->bearerToken();
        if ($token === null || $token === '') {
            throw new HttpException(401, 'unauthenticated', 'Falta el token de autenticación.');
        }

        $sessions = new SessionRepository();
        $session = $sessions->findValidByTokenHash(Token::hash($token));
        if ($session === null) {
            throw new HttpException(401, 'invalid_token', 'Token inválido o expirado.');
        }

        $users = new UserRepository();
        $user = $users->findById((int) $session['user_id']);
        if ($user === null) {
            throw new HttpException(401, 'invalid_token', 'La cuenta asociada ya no existe.');
        }

        unset($user['password_hash']);
        $request->authContext = [
            'user' => $user,
            'session_id' => (int) $session['id'],
        ];
    }
}
