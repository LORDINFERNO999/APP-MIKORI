<?php

declare(strict_types=1);

namespace Mikori\Services;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Repositories\ChildRepository;
use Mikori\Repositories\DeviceRepository;

/**
 * Gestión de perfiles de hijos con aislamiento estricto por usuario.
 */
final class ChildService
{
    private ChildRepository $children;
    private DeviceRepository $devices;

    public function __construct()
    {
        $this->children = new ChildRepository();
        $this->devices = new DeviceRepository();
    }

    /**
     * @return list<array<string,mixed>>
     */
    public function list(int $userId): array
    {
        $rows = $this->children->allForUser($userId);
        return array_map(fn (array $c) => $this->present($c), $rows);
    }

    /**
     * @return array<string,mixed>
     */
    public function create(int $userId, string $name, ?string $birthdate, ?string $avatar): array
    {
        $id = $this->children->create($userId, $name, $birthdate, $avatar);
        return $this->present($this->children->findForUser($id, $userId) ?? []);
    }

    /**
     * @return array<string,mixed>
     */
    public function get(int $userId, int $childId): array
    {
        $child = $this->requireOwned($userId, $childId);
        $data = $this->present($child);
        $data['devices'] = array_map(
            fn (array $d) => $this->presentDevice($d),
            $this->devices->allForChild($childId)
        );
        return $data;
    }

    /**
     * @param array<string,mixed> $fields
     * @return array<string,mixed>
     */
    public function update(int $userId, int $childId, array $fields): array
    {
        $this->requireOwned($userId, $childId);
        $this->children->update($childId, $userId, $fields);
        return $this->present($this->children->findForUser($childId, $userId) ?? []);
    }

    public function delete(int $userId, int $childId): void
    {
        $this->requireOwned($userId, $childId);
        $this->children->delete($childId, $userId);
    }

    /**
     * @return array<string,mixed>
     */
    private function requireOwned(int $userId, int $childId): array
    {
        $child = $this->children->findForUser($childId, $userId);
        if ($child === null) {
            throw new HttpException(404, 'child_not_found', 'No se encontró el hijo.');
        }
        return $child;
    }

    /**
     * @param array<string,mixed> $c
     * @return array<string,mixed>
     */
    private function present(array $c): array
    {
        return [
            'id' => (int) $c['id'],
            'name' => $c['name'] ?? null,
            'birthdate' => $c['birthdate'] ?? null,
            'avatar' => $c['avatar'] ?? null,
            'created_at' => $c['created_at'] ?? null,
        ];
    }

    /**
     * @param array<string,mixed> $d
     * @return array<string,mixed>
     */
    private function presentDevice(array $d): array
    {
        return [
            'id' => (int) $d['id'],
            'model' => $d['model'],
            'android_version' => $d['android_version'],
            'status' => $d['status'],
            'last_seen_at' => $d['last_seen_at'],
        ];
    }
}
