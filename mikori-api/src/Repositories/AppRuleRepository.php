<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class AppRuleRepository extends BaseRepository
{
    /**
     * Reglas por app de un hijo, con el package_name resuelto.
     * @return list<array<string,mixed>>
     */
    public function allForChild(int $childId): array
    {
        return $this->run(
            'SELECT r.*, a.package_name, a.app_label, a.category
             FROM app_rules r
             INNER JOIN applications a ON a.id = r.application_id
             WHERE r.child_id = :cid AND r.active = 1
             ORDER BY a.app_label ASC',
            [':cid' => $childId]
        )->fetchAll();
    }

    /**
     * Inserta o actualiza la regla de una app (upsert portable).
     */
    public function upsert(int $childId, int $applicationId, ?int $maxMinutes, bool $isBlocked): void
    {
        $existing = $this->run(
            'SELECT id FROM app_rules WHERE child_id = :cid AND application_id = :aid',
            [':cid' => $childId, ':aid' => $applicationId]
        )->fetch();

        if ($existing) {
            $this->run(
                'UPDATE app_rules SET max_minutes = :max, is_blocked = :blk, active = 1, updated_at = :now WHERE id = :id',
                [':max' => $maxMinutes, ':blk' => $isBlocked ? 1 : 0, ':now' => Clock::nowString(), ':id' => (int) $existing['id']]
            );
            return;
        }

        $this->run(
            'INSERT INTO app_rules (child_id, application_id, max_minutes, is_blocked, active, created_at, updated_at)
             VALUES (:cid, :aid, :max, :blk, 1, :now, :now)',
            [':cid' => $childId, ':aid' => $applicationId, ':max' => $maxMinutes, ':blk' => $isBlocked ? 1 : 0, ':now' => Clock::nowString()]
        );
    }

    public function delete(int $childId, int $applicationId): void
    {
        $this->run(
            'DELETE FROM app_rules WHERE child_id = :cid AND application_id = :aid',
            [':cid' => $childId, ':aid' => $applicationId]
        );
    }
}
