<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class PasswordResetRepository extends BaseRepository
{
    public function create(int $userId, string $tokenHash, string $expiresAt): int
    {
        $this->run(
            'INSERT INTO password_resets (user_id, token_hash, expires_at, used, created_at)
             VALUES (:uid, :th, :exp, 0, :now)',
            [':uid' => $userId, ':th' => $tokenHash, ':exp' => $expiresAt, ':now' => Clock::nowString()]
        );
        return $this->lastId();
    }

    /**
     * @return array<string,mixed>|null
     */
    public function findValidByTokenHash(string $tokenHash): ?array
    {
        $row = $this->run(
            'SELECT * FROM password_resets WHERE token_hash = :th AND used = 0 AND expires_at > :now',
            [':th' => $tokenHash, ':now' => Clock::nowString()]
        )->fetch();
        return $row ?: null;
    }

    public function markUsed(int $id): void
    {
        $this->run('UPDATE password_resets SET used = 1 WHERE id = :id', [':id' => $id]);
    }
}
