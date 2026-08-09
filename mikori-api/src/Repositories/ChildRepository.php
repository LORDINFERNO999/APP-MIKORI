<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class ChildRepository extends BaseRepository
{
    /**
     * @return list<array<string,mixed>>
     */
    public function allForUser(int $userId): array
    {
        return $this->run(
            'SELECT * FROM children WHERE user_id = :uid ORDER BY created_at ASC',
            [':uid' => $userId]
        )->fetchAll();
    }

    /**
     * Busca un hijo por id (sin filtro de usuario). Uso interno / device-auth.
     * @return array<string,mixed>|null
     */
    public function findById(int $childId): ?array
    {
        $row = $this->run('SELECT * FROM children WHERE id = :id', [':id' => $childId])->fetch();
        return $row ?: null;
    }

    /**
     * Busca un hijo asegurando que pertenece al usuario (aislamiento por dueño).
     * @return array<string,mixed>|null
     */
    public function findForUser(int $childId, int $userId): ?array
    {
        $row = $this->run(
            'SELECT * FROM children WHERE id = :id AND user_id = :uid',
            [':id' => $childId, ':uid' => $userId]
        )->fetch();
        return $row ?: null;
    }

    public function create(int $userId, string $name, ?string $birthdate, ?string $avatar): int
    {
        $this->run(
            'INSERT INTO children (user_id, name, birthdate, avatar, created_at, updated_at)
             VALUES (:uid, :name, :bd, :av, :now, :now)',
            [
                ':uid' => $userId,
                ':name' => $name,
                ':bd' => $birthdate,
                ':av' => $avatar,
                ':now' => Clock::nowString(),
            ]
        );
        return $this->lastId();
    }

    /**
     * @param array<string,mixed> $fields
     */
    public function update(int $childId, int $userId, array $fields): void
    {
        if ($fields === []) {
            return;
        }
        $set = [];
        $params = [':id' => $childId, ':uid' => $userId, ':now' => Clock::nowString()];
        foreach ($fields as $key => $value) {
            $set[] = "{$key} = :{$key}";
            $params[":{$key}"] = $value;
        }
        $set[] = 'updated_at = :now';
        $sql = 'UPDATE children SET ' . implode(', ', $set) . ' WHERE id = :id AND user_id = :uid';
        $this->run($sql, $params);
    }

    public function delete(int $childId, int $userId): void
    {
        $this->run('DELETE FROM children WHERE id = :id AND user_id = :uid', [':id' => $childId, ':uid' => $userId]);
    }
}
