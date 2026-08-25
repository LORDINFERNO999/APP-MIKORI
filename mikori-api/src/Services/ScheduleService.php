<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Core\Exceptions\ValidationException;
use Mikori\Repositories\ChildRepository;
use Mikori\Repositories\ScheduleRepository;

/**
 * Horarios de bloqueo (V2): escolar / nocturno / personalizado, con reglas por día.
 */
final class ScheduleService
{
    private ChildRepository $children;
    private ScheduleRepository $schedules;

    private const TYPES = ['school', 'night', 'custom'];

    public function __construct()
    {
        $this->children = new ChildRepository();
        $this->schedules = new ScheduleRepository();
    }

    public function list(int $userId, int $childId): array
    {
        $this->requireOwned($userId, $childId);
        return array_map(fn (array $s) => $this->present($s), $this->schedules->allForChild($childId));
    }

    /**
     * @param array<string,mixed> $payload
     */
    public function create(int $userId, int $childId, array $payload): array
    {
        $this->requireOwned($userId, $childId);
        [$name, $type, $start, $end, $mask] = $this->validate($payload);
        $id = $this->schedules->create($childId, $name, $type, $start, $end, $mask);
        return $this->present($this->schedules->findForChild($id, $childId) ?? []);
    }

    /**
     * @param array<string,mixed> $payload
     */
    public function update(int $userId, int $childId, int $scheduleId, array $payload): array
    {
        $this->requireOwned($userId, $childId);
        if ($this->schedules->findForChild($scheduleId, $childId) === null) {
            throw new HttpException(404, 'schedule_not_found', 'No se encontró el horario.');
        }
        [$name, $type, $start, $end, $mask] = $this->validate($payload);
        $active = (bool) ($payload['active'] ?? true);
        $this->schedules->update($scheduleId, $childId, $name, $type, $start, $end, $mask, $active);
        return $this->present($this->schedules->findForChild($scheduleId, $childId) ?? []);
    }

    public function delete(int $userId, int $childId, int $scheduleId): void
    {
        $this->requireOwned($userId, $childId);
        $this->schedules->delete($scheduleId, $childId);
    }

    /**
     * @param array<string,mixed> $payload
     * @return array{0:string,1:string,2:string,3:string,4:int}
     */
    private function validate(array $payload): array
    {
        $errors = [];
        $name = trim((string) ($payload['name'] ?? ''));
        if ($name === '') {
            $errors['name'] = 'El nombre es obligatorio.';
        }
        $type = (string) ($payload['type'] ?? 'custom');
        if (!in_array($type, self::TYPES, true)) {
            $errors['type'] = 'Tipo no válido (school|night|custom).';
        }
        $start = $this->normalizeTime($payload['start_time'] ?? null);
        if ($start === null) {
            $errors['start_time'] = 'Hora de inicio inválida (HH:MM).';
        }
        $end = $this->normalizeTime($payload['end_time'] ?? null);
        if ($end === null) {
            $errors['end_time'] = 'Hora de fin inválida (HH:MM).';
        }
        $mask = (int) ($payload['days_mask'] ?? 127);
        if ($mask < 1 || $mask > 127) {
            $errors['days_mask'] = 'days_mask debe estar entre 1 y 127.';
        }
        if ($errors !== []) {
            throw new ValidationException($errors);
        }
        return [$name, $type, $start, $end, $mask];
    }

    private function normalizeTime(mixed $value): ?string
    {
        if (!is_string($value)) {
            return null;
        }
        if (!preg_match('/^([01]\d|2[0-3]):([0-5]\d)(:[0-5]\d)?$/', $value, $m)) {
            return null;
        }
        return sprintf('%s:%s:00', $m[1], $m[2]);
    }

    private function requireOwned(int $userId, int $childId): void
    {
        if ($this->children->findForUser($childId, $userId) === null) {
            throw new HttpException(404, 'child_not_found', 'No se encontró el hijo.');
        }
    }

    /**
     * @param array<string,mixed> $s
     * @return array<string,mixed>
     */
    private function present(array $s): array
    {
        return [
            'id' => (int) $s['id'],
            'name' => $s['name'] ?? null,
            'type' => $s['type'] ?? null,
            'start_time' => isset($s['start_time']) ? substr((string) $s['start_time'], 0, 5) : null,
            'end_time' => isset($s['end_time']) ? substr((string) $s['end_time'], 0, 5) : null,
            'days_mask' => (int) ($s['days_mask'] ?? 127),
            'active' => (bool) ($s['active'] ?? true),
        ];
    }
}
