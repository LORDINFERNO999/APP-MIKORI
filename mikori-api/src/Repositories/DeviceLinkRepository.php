<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class DeviceLinkRepository extends BaseRepository
{
    public function create(int $userId, int $childId, string $code, string $expiresAt): int
    {
        $this->run(
            'INSERT INTO device_links (user_id, child_id, code, status, expires_at, created_at)
             VALUES (:uid, :cid, :code, :status, :exp, :now)',
            [
                ':uid' => $userId,
                ':cid' => $childId,
                ':code' => $code,
                ':status' => 'pending',
                ':exp' => $expiresAt,
                ':now' => Clock::nowString(),
            ]
        );
        return $this->lastId();
    }

    /**
     * @return array<string,mixed>|null
     */
    public function findPendingByCode(string $code): ?array
    {
        $row = $this->run(
            "SELECT * FROM device_links WHERE code = :code AND status = 'pending'",
            [':code' => $code]
        )->fetch();
        return $row ?: null;
    }

    /**
     * Último enlace de un hijo (para consultar estado desde Parent).
     * @return array<string,mixed>|null
     */
    public function latestForChild(int $childId, int $userId): ?array
    {
        $row = $this->run(
            'SELECT * FROM device_links WHERE child_id = :cid AND user_id = :uid ORDER BY created_at DESC',
            [':cid' => $childId, ':uid' => $userId]
        )->fetch();
        return $row ?: null;
    }

    public function markLinked(int $linkId, int $deviceId): void
    {
        $this->run(
            "UPDATE device_links SET status = 'linked', linked_device_id = :dev, linked_at = :now WHERE id = :id",
            [':dev' => $deviceId, ':now' => Clock::nowString(), ':id' => $linkId]
        );
    }

    public function markExpired(int $linkId): void
    {
        $this->run("UPDATE device_links SET status = 'expired' WHERE id = :id", [':id' => $linkId]);
    }
}
