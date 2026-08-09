<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class UsageRuleRepository extends BaseRepository
{
    /**
     * @return list<array<string,mixed>>  ordenadas por día (1..7)
     */
    public function allForChild(int $childId): array
    {
        return $this->run(
            'SELECT * FROM usage_rules WHERE child_id = :cid ORDER BY day_of_week ASC',
            [':cid' => $childId]
        )->fetchAll();
    }

    public function limitForChildOnDay(int $childId, int $dayOfWeek): ?int
    {
        $row = $this->run(
            'SELECT daily_limit_minutes FROM usage_rules WHERE child_id = :cid AND day_of_week = :dow AND active = 1',
            [':cid' => $childId, ':dow' => $dayOfWeek]
        )->fetch();
        return $row ? (int) $row['daily_limit_minutes'] : null;
    }

    /**
     * Inserta o actualiza el límite de un día concreto (upsert portable).
     */
    public function upsert(int $childId, int $dayOfWeek, int $minutes): void
    {
        $existing = $this->run(
            'SELECT id FROM usage_rules WHERE child_id = :cid AND day_of_week = :dow',
            [':cid' => $childId, ':dow' => $dayOfWeek]
        )->fetch();

        if ($existing) {
            $this->run(
                'UPDATE usage_rules SET daily_limit_minutes = :min, active = 1, updated_at = :now WHERE id = :id',
                [':min' => $minutes, ':now' => Clock::nowString(), ':id' => (int) $existing['id']]
            );
            return;
        }

        $this->run(
            'INSERT INTO usage_rules (child_id, day_of_week, daily_limit_minutes, active, created_at, updated_at)
             VALUES (:cid, :dow, :min, 1, :now, :now)',
            [':cid' => $childId, ':dow' => $dayOfWeek, ':min' => $minutes, ':now' => Clock::nowString()]
        );
    }
}
