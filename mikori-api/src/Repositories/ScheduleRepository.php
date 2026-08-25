<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class ScheduleRepository extends BaseRepository
{
    /**
     * @return list<array<string,mixed>>
     */
    public function allForChild(int $childId): array
    {
        return $this->run(
            'SELECT * FROM schedules WHERE child_id = :cid ORDER BY start_time ASC',
            [':cid' => $childId]
        )->fetchAll();
    }

    /**
     * @return array<string,mixed>|null
     */
    public function findForChild(int $id, int $childId): ?array
    {
        $row = $this->run(
            'SELECT * FROM schedules WHERE id = :id AND child_id = :cid',
            [':id' => $id, ':cid' => $childId]
        )->fetch();
        return $row ?: null;
    }

    public function create(int $childId, string $name, string $type, string $start, string $end, int $daysMask): int
    {
        $this->run(
            'INSERT INTO schedules (child_id, name, type, start_time, end_time, days_mask, active, created_at, updated_at)
             VALUES (:cid, :name, :type, :start, :end, :mask, 1, :now, :now)',
            [':cid' => $childId, ':name' => $name, ':type' => $type, ':start' => $start, ':end' => $end, ':mask' => $daysMask, ':now' => Clock::nowString()]
        );
        return $this->lastId();
    }

    public function update(int $id, int $childId, string $name, string $type, string $start, string $end, int $daysMask, bool $active): void
    {
        $this->run(
            'UPDATE schedules SET name = :name, type = :type, start_time = :start, end_time = :end,
                    days_mask = :mask, active = :active, updated_at = :now
             WHERE id = :id AND child_id = :cid',
            [
                ':name' => $name, ':type' => $type, ':start' => $start, ':end' => $end,
                ':mask' => $daysMask, ':active' => $active ? 1 : 0, ':now' => Clock::nowString(),
                ':id' => $id, ':cid' => $childId,
            ]
        );
    }

    public function delete(int $id, int $childId): void
    {
        $this->run('DELETE FROM schedules WHERE id = :id AND child_id = :cid', [':id' => $id, ':cid' => $childId]);
    }
}
