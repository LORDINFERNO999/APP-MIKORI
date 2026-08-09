<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class DeviceRepository extends BaseRepository
{
    /**
     * @return array<string,mixed>|null
     */
    public function findByUid(string $uid): ?array
    {
        $row = $this->run('SELECT * FROM devices WHERE device_uid = :uid', [':uid' => $uid])->fetch();
        return $row ?: null;
    }

    /**
     * @return array<string,mixed>|null
     */
    public function findById(int $id): ?array
    {
        $row = $this->run('SELECT * FROM devices WHERE id = :id', [':id' => $id])->fetch();
        return $row ?: null;
    }

    /**
     * Busca un dispositivo verificando que pertenece a un hijo del usuario.
     * @return array<string,mixed>|null
     */
    public function findForUser(int $deviceId, int $userId): ?array
    {
        $row = $this->run(
            'SELECT d.* FROM devices d
             INNER JOIN children c ON c.id = d.child_id
             WHERE d.id = :id AND c.user_id = :uid',
            [':id' => $deviceId, ':uid' => $userId]
        )->fetch();
        return $row ?: null;
    }

    /**
     * @return list<array<string,mixed>>
     */
    public function allForChild(int $childId): array
    {
        return $this->run(
            'SELECT * FROM devices WHERE child_id = :cid ORDER BY created_at ASC',
            [':cid' => $childId]
        )->fetchAll();
    }

    public function create(int $childId, string $uid, ?string $model, ?string $androidVersion, ?string $fcmToken, string $apiTokenHash): int
    {
        $this->run(
            'INSERT INTO devices (child_id, device_uid, model, android_version, fcm_token, api_token_hash, status, last_seen_at, created_at, updated_at)
             VALUES (:cid, :uid, :model, :av, :fcm, :tok, :status, :now, :now, :now)',
            [
                ':cid' => $childId,
                ':uid' => $uid,
                ':model' => $model,
                ':av' => $androidVersion,
                ':fcm' => $fcmToken,
                ':tok' => $apiTokenHash,
                ':status' => 'online',
                ':now' => Clock::nowString(),
            ]
        );
        return $this->lastId();
    }

    /**
     * Reasigna un dispositivo existente (re-vinculación) a un hijo, con nuevo token.
     */
    public function relink(int $deviceId, int $childId, ?string $model, ?string $androidVersion, ?string $fcmToken, string $apiTokenHash): void
    {
        $this->run(
            'UPDATE devices SET child_id = :cid, model = :model, android_version = :av, fcm_token = :fcm,
                    api_token_hash = :tok, status = :status, last_seen_at = :now, updated_at = :now
             WHERE id = :id',
            [
                ':cid' => $childId,
                ':model' => $model,
                ':av' => $androidVersion,
                ':fcm' => $fcmToken,
                ':tok' => $apiTokenHash,
                ':status' => 'online',
                ':now' => Clock::nowString(),
                ':id' => $deviceId,
            ]
        );
    }

    /**
     * @return array<string,mixed>|null
     */
    public function findByApiTokenHash(string $hash): ?array
    {
        $row = $this->run('SELECT * FROM devices WHERE api_token_hash = :h', [':h' => $hash])->fetch();
        return $row ?: null;
    }

    public function touchHeartbeat(int $deviceId, string $status): void
    {
        $this->run(
            'UPDATE devices SET status = :status, last_seen_at = :now, updated_at = :now WHERE id = :id',
            [':status' => $status, ':now' => Clock::nowString(), ':id' => $deviceId]
        );
    }

    public function updateFcmToken(int $deviceId, string $token): void
    {
        $this->run(
            'UPDATE devices SET fcm_token = :fcm, updated_at = :now WHERE id = :id',
            [':fcm' => $token, ':now' => Clock::nowString(), ':id' => $deviceId]
        );
    }
}
