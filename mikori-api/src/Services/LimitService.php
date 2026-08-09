<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Core\Exceptions\ValidationException;
use Mikori\Repositories\ChildRepository;
use Mikori\Repositories\UsageRuleRepository;

/**
 * Límites diarios de tiempo total por hijo (V1).
 * day_of_week: 1=Lunes ... 7=Domingo.
 */
final class LimitService
{
    private ChildRepository $children;
    private UsageRuleRepository $rules;

    /** @var array<int,string> */
    private const DAY_NAMES = [
        1 => 'Lunes', 2 => 'Martes', 3 => 'Miércoles', 4 => 'Jueves',
        5 => 'Viernes', 6 => 'Sábado', 7 => 'Domingo',
    ];

    public function __construct()
    {
        $this->children = new ChildRepository();
        $this->rules = new UsageRuleRepository();
    }

    /**
     * @return array<string,mixed>
     */
    public function get(int $userId, int $childId): array
    {
        $this->requireOwned($userId, $childId);

        $existing = [];
        foreach ($this->rules->allForChild($childId) as $rule) {
            $existing[(int) $rule['day_of_week']] = (int) $rule['daily_limit_minutes'];
        }

        $days = [];
        foreach (self::DAY_NAMES as $dow => $name) {
            $days[] = [
                'day_of_week' => $dow,
                'day_name' => $name,
                'daily_limit_minutes' => $existing[$dow] ?? null,
            ];
        }

        return ['child_id' => $childId, 'days' => $days];
    }

    /**
     * Define límites. Acepta dos formatos:
     *   - { "all": 120 }                          → mismo límite todos los días
     *   - { "days": [ {day_of_week, minutes}, ...] } → por día
     *
     * @param array<string,mixed> $payload
     * @return array<string,mixed>
     */
    public function set(int $userId, int $childId, array $payload): array
    {
        $this->requireOwned($userId, $childId);

        if (array_key_exists('all', $payload) && $payload['all'] !== null) {
            $minutes = $this->validMinutes($payload['all'], 'all');
            for ($dow = 1; $dow <= 7; $dow++) {
                $this->rules->upsert($childId, $dow, $minutes);
            }
        } elseif (isset($payload['days']) && is_array($payload['days'])) {
            $errors = [];
            foreach ($payload['days'] as $i => $entry) {
                $dow = (int) ($entry['day_of_week'] ?? 0);
                if ($dow < 1 || $dow > 7) {
                    $errors["days.{$i}.day_of_week"] = 'Debe estar entre 1 y 7.';
                    continue;
                }
                $minutes = $this->validMinutes($entry['minutes'] ?? null, "days.{$i}.minutes");
                $this->rules->upsert($childId, $dow, $minutes);
            }
            if ($errors !== []) {
                throw new ValidationException($errors);
            }
        } else {
            throw new ValidationException(['limits' => "Envía 'all' (minutos) o 'days' (lista por día)."]);
        }

        return $this->get($userId, $childId);
    }

    private function validMinutes(mixed $value, string $field): int
    {
        if (!is_int($value) && !(is_string($value) && ctype_digit($value))) {
            throw new ValidationException([$field => 'Debe ser un número de minutos.']);
        }
        $minutes = (int) $value;
        if ($minutes < 0 || $minutes > 1440) {
            throw new ValidationException([$field => 'Debe estar entre 0 y 1440 minutos.']);
        }
        return $minutes;
    }

    private function requireOwned(int $userId, int $childId): void
    {
        if ($this->children->findForUser($childId, $userId) === null) {
            throw new HttpException(404, 'child_not_found', 'No se encontró el hijo.');
        }
    }
}
