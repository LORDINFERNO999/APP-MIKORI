<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class AppUsageRepository extends BaseRepository
{
    /**
     * Acumula uso para (device, application, fecha). Upsert portable.
     * Suma la duración y ajusta started_at (mínimo) / ended_at (máximo).
     */
    public function accumulate(int $deviceId, int $applicationId, string $date, int $seconds, ?string $startedAt, ?string $endedAt): void
    {
        $existing = $this->run(
            'SELECT id, duration_seconds, started_at, ended_at FROM app_usage
             WHERE device_id = :dev AND application_id = :app AND usage_date = :date',
            [':dev' => $deviceId, ':app' => $applicationId, ':date' => $date]
        )->fetch();

        if ($existing) {
            $newDuration = (int) $existing['duration_seconds'] + $seconds;
            $newStart = $this->minDate($existing['started_at'] ?? null, $startedAt);
            $newEnd = $this->maxDate($existing['ended_at'] ?? null, $endedAt);
            $this->run(
                'UPDATE app_usage SET duration_seconds = :dur, started_at = :start, ended_at = :end WHERE id = :id',
                [':dur' => $newDuration, ':start' => $newStart, ':end' => $newEnd, ':id' => (int) $existing['id']]
            );
            return;
        }

        $this->run(
            'INSERT INTO app_usage (device_id, application_id, usage_date, duration_seconds, started_at, ended_at, created_at)
             VALUES (:dev, :app, :date, :dur, :start, :end, :now)',
            [
                ':dev' => $deviceId,
                ':app' => $applicationId,
                ':date' => $date,
                ':dur' => $seconds,
                ':start' => $startedAt,
                ':end' => $endedAt,
                ':now' => Clock::nowString(),
            ]
        );
    }

    /** Segundos totales de uso de un hijo en una fecha. */
    public function totalSecondsForChildOnDate(int $childId, string $date): int
    {
        $row = $this->run(
            'SELECT COALESCE(SUM(u.duration_seconds), 0) AS total
             FROM app_usage u
             INNER JOIN devices d ON d.id = u.device_id
             WHERE d.child_id = :cid AND u.usage_date = :date',
            [':cid' => $childId, ':date' => $date]
        )->fetch();
        return (int) ($row['total'] ?? 0);
    }

    /**
     * Top de apps por uso de un hijo en una fecha.
     * @return list<array<string,mixed>>
     */
    public function topAppsForChildOnDate(int $childId, string $date, int $limit = 10): array
    {
        $limit = max(1, min(50, $limit));
        return $this->run(
            "SELECT a.package_name, a.app_label, a.category, SUM(u.duration_seconds) AS seconds
             FROM app_usage u
             INNER JOIN devices d ON d.id = u.device_id
             INNER JOIN applications a ON a.id = u.application_id
             WHERE d.child_id = :cid AND u.usage_date = :date
             GROUP BY a.id, a.package_name, a.app_label, a.category
             ORDER BY seconds DESC
             LIMIT {$limit}",
            [':cid' => $childId, ':date' => $date]
        )->fetchAll();
    }

    /**
     * Uso total por día en un rango (para gráfico semanal).
     * @return list<array<string,mixed>>  filas [usage_date, seconds]
     */
    public function dailyTotalsForChild(int $childId, string $fromDate, string $toDate): array
    {
        return $this->run(
            'SELECT u.usage_date, SUM(u.duration_seconds) AS seconds
             FROM app_usage u
             INNER JOIN devices d ON d.id = u.device_id
             WHERE d.child_id = :cid AND u.usage_date >= :from AND u.usage_date <= :to
             GROUP BY u.usage_date
             ORDER BY u.usage_date ASC',
            [':cid' => $childId, ':from' => $fromDate, ':to' => $toDate]
        )->fetchAll();
    }

    /**
     * Uso por app en un rango.
     * @return list<array<string,mixed>>
     */
    public function appTotalsForChild(int $childId, string $fromDate, string $toDate): array
    {
        return $this->run(
            'SELECT a.package_name, a.app_label, a.category, SUM(u.duration_seconds) AS seconds
             FROM app_usage u
             INNER JOIN devices d ON d.id = u.device_id
             INNER JOIN applications a ON a.id = u.application_id
             WHERE d.child_id = :cid AND u.usage_date >= :from AND u.usage_date <= :to
             GROUP BY a.id, a.package_name, a.app_label, a.category
             ORDER BY seconds DESC',
            [':cid' => $childId, ':from' => $fromDate, ':to' => $toDate]
        )->fetchAll();
    }

    private function minDate(?string $a, ?string $b): ?string
    {
        if ($a === null) {
            return $b;
        }
        if ($b === null) {
            return $a;
        }
        return strtotime($a) <= strtotime($b) ? $a : $b;
    }

    private function maxDate(?string $a, ?string $b): ?string
    {
        if ($a === null) {
            return $b;
        }
        if ($b === null) {
            return $a;
        }
        return strtotime($a) >= strtotime($b) ? $a : $b;
    }
}
