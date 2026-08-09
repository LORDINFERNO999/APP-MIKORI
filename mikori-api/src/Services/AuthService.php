<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Core\Env;
use Mikori\Core\Exceptions\HttpException;
use Mikori\Repositories\PasswordResetRepository;
use Mikori\Repositories\SessionRepository;
use Mikori\Repositories\UserRepository;
use Mikori\Support\Clock;
use Mikori\Support\Hash;
use Mikori\Support\Token;

/**
 * Lógica de autenticación: registro, login, logout, refresh y recuperación.
 * Emite tokens opacos (access + refresh) almacenando solo su hash.
 */
final class AuthService
{
    private UserRepository $users;
    private SessionRepository $sessions;
    private PasswordResetRepository $resets;

    public function __construct()
    {
        $this->users = new UserRepository();
        $this->sessions = new SessionRepository();
        $this->resets = new PasswordResetRepository();
    }

    /**
     * @return array<string,mixed>
     */
    public function register(string $name, string $email, string $password, ?string $ua, ?string $ip): array
    {
        if ($this->users->findByEmail($email) !== null) {
            throw new HttpException(409, 'email_taken', 'Ya existe una cuenta con ese correo.');
        }

        $userId = $this->users->create($name, $email, Hash::make($password));
        $user = $this->users->findById($userId);

        return $this->issueTokens($userId, $user, $ua, $ip);
    }

    /**
     * @return array<string,mixed>
     */
    public function login(string $email, string $password, ?string $ua, ?string $ip): array
    {
        $user = $this->users->findByEmail($email);
        if ($user === null || !Hash::verify($password, $user['password_hash'])) {
            // Mensaje genérico: no revelar si el correo existe.
            throw new HttpException(401, 'invalid_credentials', 'Correo o contraseña incorrectos.');
        }

        // Rehash transparente si cambió el algoritmo/coste.
        if (Hash::needsRehash($user['password_hash'])) {
            $this->users->updatePassword((int) $user['id'], Hash::make($password));
        }

        return $this->issueTokens((int) $user['id'], $user, $ua, $ip);
    }

    public function logout(int $sessionId): void
    {
        $this->sessions->revoke($sessionId);
    }

    /**
     * @return array<string,mixed>
     */
    public function refresh(string $refreshToken): array
    {
        $session = $this->sessions->findValidByRefreshHash(Token::hash($refreshToken));
        if ($session === null) {
            throw new HttpException(401, 'invalid_token', 'Refresh token inválido o expirado.');
        }

        $access = Token::random();
        $refresh = Token::random();
        $ttl = Env::int('TOKEN_TTL_MINUTES', 43200);
        $this->sessions->rotate(
            (int) $session['id'],
            Token::hash($access),
            Token::hash($refresh),
            Clock::inMinutes($ttl)
        );

        return [
            'access_token' => $access,
            'refresh_token' => $refresh,
            'token_type' => 'Bearer',
            'expires_in' => $ttl * 60,
        ];
    }

    /**
     * Inicia recuperación. Siempre responde igual (no revela si el correo existe).
     * Devuelve el token de reset solo en modo debug (en producción se enviaría por email).
     * @return array<string,mixed>
     */
    public function forgotPassword(string $email): array
    {
        $user = $this->users->findByEmail($email);
        $response = ['message' => 'Si el correo existe, enviaremos instrucciones para restablecer la contraseña.'];

        if ($user !== null) {
            $token = Token::random();
            $ttl = Env::int('RESET_TTL_MINUTES', 60);
            $this->resets->create((int) $user['id'], Token::hash($token), Clock::inMinutes($ttl));

            // TODO(prod): enviar el token por correo. Aquí solo se expone en debug.
            if (Env::bool('APP_DEBUG', false)) {
                $response['debug_reset_token'] = $token;
            }
        }

        return $response;
    }

    public function resetPassword(string $token, string $newPassword): void
    {
        $reset = $this->resets->findValidByTokenHash(Token::hash($token));
        if ($reset === null) {
            throw new HttpException(400, 'invalid_reset_token', 'El enlace de recuperación no es válido o expiró.');
        }

        $this->users->updatePassword((int) $reset['user_id'], Hash::make($newPassword));
        $this->resets->markUsed((int) $reset['id']);
    }

    /**
     * @param array<string,mixed> $user
     * @return array<string,mixed>
     */
    private function issueTokens(int $userId, array $user, ?string $ua, ?string $ip): array
    {
        $access = Token::random();
        $refresh = Token::random();
        $ttl = Env::int('TOKEN_TTL_MINUTES', 43200);

        $this->sessions->create(
            $userId,
            Token::hash($access),
            Token::hash($refresh),
            Clock::inMinutes($ttl),
            $ua,
            $ip
        );

        unset($user['password_hash']);

        return [
            'user' => $this->presentUser($user),
            'access_token' => $access,
            'refresh_token' => $refresh,
            'token_type' => 'Bearer',
            'expires_in' => $ttl * 60,
        ];
    }

    /**
     * @param array<string,mixed> $user
     * @return array<string,mixed>
     */
    private function presentUser(array $user): array
    {
        return [
            'id' => (int) $user['id'],
            'name' => $user['name'],
            'email' => $user['email'],
            'created_at' => $user['created_at'],
        ];
    }
}
