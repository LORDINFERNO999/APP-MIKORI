<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class SessionRepository extends BaseRepository
{
    public function create(int $userId, string $tokenHash, string $refreshHash, string $expiresAt, ?string $userAgent, ?string $ip): int
    {
        $this->run(
            'INSERT INTO sessions (user_id, token_hash, refresh_hash, user_agent, ip_address, expires_at, revoked, created_at)
             VALUES (:uid, :th, :rh, :ua, :ip, :exp, 0, :now)',
            [
                ':uid' => $userId,
                ':th' => $tokenHash,
                ':rh' => $refreshHash,
                ':ua' => $userAgent,
                ':ip' => $ip,
                ':exp' => $expiresAt,
                ':now' => Clock::nowString(),
            ]
        );
        return $this->lastId();
    }

    /**
     * Devuelve la sesión válida (no revocada ni expirada) para un token.
     * @return array<string,mixed>|null
     */
    public function findValidByTokenHash(string $tokenHash): ?array
    {
        $row = $this->run(
            'SELECT * FROM sessions WHERE token_hash = :th AND revoked = 0 AND expires_at > :now',
            [':th' => $tokenHash, ':now' => Clock::nowString()]
        )->fetch();
        return $row ?: null;
    }

    /**
     * @return array<string,mixed>|null
     */
    public function findValidByRefreshHash(string $refreshHash): ?array
    {
        $row = $this->run(
            'SELECT * FROM sessions WHERE refresh_hash = :rh AND revoked = 0 AND expires_at > :now',
            [':rh' => $refreshHash, ':now' => Clock::nowString()]
        )->fetch();
        return $row ?: null;
    }

    public function revoke(int $sessionId): void
    {
        $this->run('UPDATE sessions SET revoked = 1 WHERE id = :id', [':id' => $sessionId]);
    }

    public function rotate(int $sessionId, string $tokenHash, string $refreshHash, string $expiresAt): void
    {
        $this->run(
            'UPDATE sessions SET token_hash = :th, refresh_hash = :rh, expires_at = :exp WHERE id = :id',
            [':th' => $tokenHash, ':rh' => $refreshHash, ':exp' => $expiresAt, ':id' => $sessionId]
        );
    }
}
