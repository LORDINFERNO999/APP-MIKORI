<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Repositories\AppRuleRepository;
use Mikori\Repositories\AppUsageRepository;
use Mikori\Repositories\PauseRepository;
use Mikori\Repositories\ScheduleRepository;
use Mikori\Repositories\UsageRuleRepository;
use Mikori\Support\Clock;

/**
 * Construye la política de enforcement que la app Kids aplica localmente (V2).
 * Ver precedencia en docs/04-v2-control.md §6.
 */
final class PolicyService
{
    private AppRuleRepository $appRules;
    private AppUsageRepository $usage;
    private ScheduleRepository $schedules;
    private PauseRepository $pauses;
    private UsageRuleRepository $usageRules;

    public function __construct()
    {
        $this->appRules = new AppRuleRepository();
        $this->usage = new AppUsageRepository();
        $this->schedules = new ScheduleRepository();
        $this->pauses = new PauseRepository();
        $this->usageRules = new UsageRuleRepository();
    }

    /**
     * @return array<string,mixed>
     */
    public function forChild(int $childId): array
    {
        $date = Clock::today();
        $dayOfWeek = (int) date('N'); // 1..7 (Lun..Dom)
        $nowMinutes = ((int) date('H')) * 60 + ((int) date('i'));

        // Límite diario total
        $totalSeconds = $this->usage->totalSecondsForChildOnDate($childId, $date);
        $limitMinutes = $this->usageRules->limitForChildOnDay($childId, $dayOfWeek);
        $remainingSeconds = $limitMinutes !== null ? max(0, $limitMinutes * 60 - $totalSeconds) : null;
        $dailyLimitReached = $limitMinutes !== null && $remainingSeconds === 0;

        // Reglas por app
        $usedByPackage = $this->usage->usedSecondsByPackageToday($childId, $date);
        $blockedPackages = [];
        $appLimits = [];
        foreach ($this->appRules->allForChild($childId) as $rule) {
            $pkg = $rule['package_name'];
            if ((int) $rule['is_blocked'] === 1) {
                $blockedPackages[] = $pkg;
            }
            if ($rule['max_minutes'] !== null) {
                $used = $usedByPackage[$pkg] ?? 0;
                $max = (int) $rule['max_minutes'];
                $appLimits[] = [
                    'package' => $pkg,
                    'max_minutes' => $max,
                    'used_seconds' => $used,
                    'exceeded' => $used >= $max * 60,
                ];
            }
        }

        // Horario activo
        $activeSchedule = null;
        foreach ($this->schedules->allForChild($childId) as $s) {
            if ((int) $s['active'] !== 1) {
                continue;
            }
            $mask = (int) $s['days_mask'];
            if (($mask & (1 << ($dayOfWeek - 1))) === 0) {
                continue;
            }
            $start = $this->toMinutes((string) $s['start_time']);
            $end = $this->toMinutes((string) $s['end_time']);
            if ($start < $end) {
                $inWindow = $nowMinutes >= $start && $nowMinutes < $end;
            } else {
                // Cruza medianoche (p. ej. 21:30 → 07:00)
                $inWindow = $nowMinutes >= $start || $nowMinutes < $end;
            }
            if ($inWindow) {
                $activeSchedule = ['name' => $s['name'], 'type' => $s['type']];
                break;
            }
        }

        // Pausa activa
        $pause = $this->pauses->activeForChild($childId);
        $pauseUntil = $pause['until_at'] ?? null;

        $blockAll = $pauseUntil !== null || $activeSchedule !== null || $dailyLimitReached;

        return [
            'daily_limit_reached' => $dailyLimitReached,
            'remaining_seconds' => $remainingSeconds,
            'blocked_packages' => $blockedPackages,
            'app_limits' => $appLimits,
            'active_schedule' => $activeSchedule,
            'pause_until' => $pauseUntil,
            'block_all' => $blockAll,
        ];
    }

    private function toMinutes(string $time): int
    {
        $parts = explode(':', $time);
        $h = (int) ($parts[0] ?? 0);
        $m = (int) ($parts[1] ?? 0);
        return $h * 60 + $m;
    }
}
