<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class PauseRepository extends BaseRepository
{
    /**
     * Pausa activa y aún vigente del hijo, si existe.
     * @return array<string,mixed>|null
     */
    public function activeForChild(int $childId): ?array
    {
        $row = $this->run(
            'SELECT * FROM pauses WHERE child_id = :cid AND active = 1 AND until_at > :now ORDER BY until_at DESC',
            [':cid' => $childId, ':now' => Clock::nowString()]
        )->fetch();
        return $row ?: null;
    }

    public function start(int $childId, string $untilAt): int
    {
        // Cancela pausas previas para no acumular.
        $this->cancel($childId);
        $this->run(
            'INSERT INTO pauses (child_id, until_at, active, created_at) VALUES (:cid, :until, 1, :now)',
            [':cid' => $childId, ':until' => $untilAt, ':now' => Clock::nowString()]
        );
        return $this->lastId();
    }

    public function cancel(int $childId): void
    {
        $this->run('UPDATE pauses SET active = 0 WHERE child_id = :cid AND active = 1', [':cid' => $childId]);
    }
}
