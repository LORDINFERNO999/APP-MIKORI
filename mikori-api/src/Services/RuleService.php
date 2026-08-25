<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Core\Exceptions\ValidationException;
use Mikori\Repositories\AppRuleRepository;
use Mikori\Repositories\AppUsageRepository;
use Mikori\Repositories\ApplicationRepository;
use Mikori\Repositories\ChildRepository;

/**
 * Reglas por aplicación (V2): límite por app y/o bloqueo por app.
 */
final class RuleService
{
    private ChildRepository $children;
    private AppRuleRepository $rules;
    private ApplicationRepository $apps;
    private AppUsageRepository $usage;

    public function __construct()
    {
        $this->children = new ChildRepository();
        $this->rules = new AppRuleRepository();
        $this->apps = new ApplicationRepository();
        $this->usage = new AppUsageRepository();
    }

    /** Catálogo de apps usadas por el hijo (para elegir cuáles regular). */
    public function apps(int $userId, int $childId): array
    {
        $this->requireOwned($userId, $childId);
        return array_map(fn (array $a) => [
            'package_name' => $a['package_name'],
            'app_label' => $a['app_label'],
            'category' => $a['category'],
        ], $this->usage->appsCatalogForChild($childId));
    }

    /** Reglas actuales por app. */
    public function get(int $userId, int $childId): array
    {
        $this->requireOwned($userId, $childId);
        return array_map(fn (array $r) => $this->present($r), $this->rules->allForChild($childId));
    }

    /**
     * Define reglas por app. Payload: { rules: [ {package, max_minutes?, is_blocked?} ] }
     * @param array<string,mixed> $payload
     */
    public function set(int $userId, int $childId, array $payload): array
    {
        $this->requireOwned($userId, $childId);

        $rules = $payload['rules'] ?? null;
        if (!is_array($rules)) {
            throw new ValidationException(['rules' => "Envía 'rules' como lista de reglas por app."]);
        }

        foreach ($rules as $i => $entry) {
            $package = isset($entry['package']) ? (string) $entry['package'] : '';
            if ($package === '') {
                throw new ValidationException(["rules.{$i}.package" => 'El nombre de paquete es obligatorio.']);
            }
            $maxMinutes = null;
            if (array_key_exists('max_minutes', $entry) && $entry['max_minutes'] !== null) {
                $m = (int) $entry['max_minutes'];
                if ($m < 0 || $m > 1440) {
                    throw new ValidationException(["rules.{$i}.max_minutes" => 'Debe estar entre 0 y 1440.']);
                }
                $maxMinutes = $m;
            }
            $isBlocked = (bool) ($entry['is_blocked'] ?? false);

            $appId = $this->apps->findOrCreate($package, $entry['label'] ?? null, $entry['category'] ?? null);
            $this->rules->upsert($childId, $appId, $maxMinutes, $isBlocked);
        }

        return $this->get($userId, $childId);
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
    private function present(array $r): array
    {
        return [
            'package_name' => $r['package_name'],
            'app_label' => $r['app_label'],
            'category' => $r['category'],
            'max_minutes' => $r['max_minutes'] !== null ? (int) $r['max_minutes'] : null,
            'is_blocked' => (bool) $r['is_blocked'],
        ];
    }
}
