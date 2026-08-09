<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class UserRepository extends BaseRepository
{
    /**
     * @return array<string,mixed>|null
     */
    public function findByEmail(string $email): ?array
    {
        $row = $this->run('SELECT * FROM users WHERE email = :email', [':email' => strtolower($email)])->fetch();
        return $row ?: null;
    }

    /**
     * @return array<string,mixed>|null
     */
    public function findById(int $id): ?array
    {
        $row = $this->run('SELECT * FROM users WHERE id = :id', [':id' => $id])->fetch();
        return $row ?: null;
    }

    public function create(string $name, string $email, string $passwordHash): int
    {
        $this->run(
            'INSERT INTO users (name, email, password_hash, created_at, updated_at)
             VALUES (:name, :email, :hash, :now, :now)',
            [
                ':name' => $name,
                ':email' => strtolower($email),
                ':hash' => $passwordHash,
                ':now' => Clock::nowString(),
            ]
        );
        return $this->lastId();
    }

    public function updatePassword(int $userId, string $passwordHash): void
    {
        $this->run(
            'UPDATE users SET password_hash = :hash, updated_at = :now WHERE id = :id',
            [':hash' => $passwordHash, ':now' => Clock::nowString(), ':id' => $userId]
        );
    }
}
