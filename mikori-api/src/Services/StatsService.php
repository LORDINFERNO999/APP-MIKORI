<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Repositories\AppUsageRepository;
use Mikori\Repositories\ApplicationRepository;
use Mikori\Repositories\ChildRepository;
use Mikori\Repositories\DeviceRepository;
use Mikori\Repositories\UsageRuleRepository;
use Mikori\Support\Clock;

/**
 * Ingesta de estadísticas (app Kids) y lectura de estadísticas (app Parent).
 */
final class StatsService
{
    private ApplicationRepository $apps;
    private AppUsageRepository $usage;
    private ChildRepository $children;
    private DeviceRepository $devices;
    private UsageRuleRepository $rules;

    public function __construct()
    {
        $this->apps = new ApplicationRepository();
        $this->usage = new AppUsageRepository();
        $this->children = new ChildRepository();
        $this->devices = new DeviceRepository();
        $this->rules = new UsageRuleRepository();
    }

    /**
     * Ingesta por lotes desde el dispositivo del hijo.
     * @param list<array<string,mixed>> $items
     * @return array<string,mixed>
     */
    public function ingest(int $deviceId, array $items): array
    {
        $accepted = 0;
        foreach ($items as $item) {
            $package = isset($item['package']) ? (string) $item['package'] : '';
            if ($package === '') {
                continue;
            }
            $date = isset($item['date']) ? (string) $item['date'] : Clock::today();
            $seconds = (int) ($item['seconds'] ?? 0);
            if ($seconds < 0) {
                $seconds = 0;
            }
            $label = isset($item['label']) ? (string) $item['label'] : null;
            $category = isset($item['category']) ? (string) $item['category'] : null;
            $startedAt = isset($item['started_at']) ? (string) $item['started_at'] : null;
            $endedAt = isset($item['ended_at']) ? (string) $item['ended_at'] : null;

            $appId = $this->apps->findOrCreate($package, $label, $category);
            $this->usage->accumulate($deviceId, $appId, $date, $seconds, $startedAt, $endedAt);
            $accepted++;
        }

        // La ingesta cuenta como señal de actividad → dispositivo en línea.
        $this->devices->touchHeartbeat($deviceId, 'online');

        return ['accepted' => $accepted];
    }

    /**
     * Resumen de hoy para un hijo (total, límite, restante, top apps).
     * @return array<string,mixed>
     */
    public function today(int $userId, int $childId): array
    {
        $this->requireOwned($userId, $childId);
        return $this->buildToday($childId);
    }

    /**
     * Resumen de hoy para la app Kids (autenticada por dispositivo).
     * No requiere usuario: el dispositivo ya está vinculado a ese hijo.
     * @return array<string,mixed>
     */
    public function todayForDevice(int $childId): array
    {
        $summary = $this->buildToday($childId);
        $child = $this->children->findById($childId);
        $summary['child_name'] = $child['name'] ?? null;
        return $summary;
    }

    /**
     * @return array<string,mixed>
     */
    private function buildToday(int $childId): array
    {
        $date = Clock::today();
        $dayOfWeek = (int) date('N'); // 1..7
        $totalSeconds = $this->usage->totalSecondsForChildOnDate($childId, $date);
        $limitMinutes = $this->rules->limitForChildOnDay($childId, $dayOfWeek);

        $remainingSeconds = null;
        if ($limitMinutes !== null) {
            $remainingSeconds = max(0, $limitMinutes * 60 - $totalSeconds);
        }

        $topApps = array_map(
            fn (array $r) => $this->presentAppUsage($r),
            $this->usage->topAppsForChildOnDate($childId, $date, 10)
        );

        return [
            'date' => $date,
            'total_seconds' => $totalSeconds,
            'limit_minutes' => $limitMinutes,
            'remaining_seconds' => $remainingSeconds,
            'limit_reached' => $limitMinutes !== null && $remainingSeconds === 0,
            'top_apps' => $topApps,
        ];
    }

    /**
     * Totales diarios de los últimos 7 días (para gráfico semanal).
     * @return array<string,mixed>
     */
    public function week(int $userId, int $childId): array
    {
        $this->requireOwned($userId, $childId);

        $from = Clock::daysAgo(6);
        $to = Clock::today();
        $rows = $this->usage->dailyTotalsForChild($childId, $from, $to);

        // Mapa fecha => segundos para rellenar días sin datos con 0.
        $byDate = [];
        foreach ($rows as $r) {
            $byDate[$r['usage_date']] = (int) $r['seconds'];
        }

        $days = [];
        for ($i = 6; $i >= 0; $i--) {
            $d = Clock::daysAgo($i);
            $days[] = ['date' => $d, 'total_seconds' => $byDate[$d] ?? 0];
        }

        return ['from' => $from, 'to' => $to, 'days' => $days];
    }

    /**
     * Uso por app en un rango (por defecto hoy).
     * @return array<string,mixed>
     */
    public function apps(int $userId, int $childId, ?string $from, ?string $to): array
    {
        $this->requireOwned($userId, $childId);

        $from = $from ?: Clock::today();
        $to = $to ?: Clock::today();
        $rows = array_map(
            fn (array $r) => $this->presentAppUsage($r),
            $this->usage->appTotalsForChild($childId, $from, $to)
        );

        return ['from' => $from, 'to' => $to, 'apps' => $rows];
    }

    private function requireOwned(int $userId, int $childId): void
    {
        if ($this->children->findForUser($childId, $userId) === null) {
            throw new HttpException(404, 'child_not_found', 'No se encontró el hijo.');
        }
    }

    /**
     * @param array<string,mixed> $r
     * @return array<string,mixed>
     */
    private function presentAppUsage(array $r): array
    {
        return [
            'package_name' => $r['package_name'],
            'app_label' => $r['app_label'],
            'category' => $r['category'],
            'seconds' => (int) $r['seconds'],
        ];
    }
}
