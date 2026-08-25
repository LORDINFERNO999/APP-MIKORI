<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Core\Validator;
use Mikori\Repositories\DeviceRepository;

/**
 * Endpoints usados por la app Kids (autenticación por token de dispositivo).
 */
final class DeviceController
{
    private DeviceRepository $devices;

    public function __construct()
    {
        $this->devices = new DeviceRepository();
    }

    /** Latido: marca el dispositivo en línea y actualiza last_seen_at. */
    public function heartbeat(Request $request): Response
    {
        $device = $request->device();
        $this->devices->touchHeartbeat((int) $device['id'], 'online');
        return Response::ok(['status' => 'online', 'server_time' => gmdate('c')]);
    }

    /** Resumen de hoy del hijo vinculado a este dispositivo (para la app Kids). */
    public function today(Request $request): Response
    {
        $device = $request->device();
        $summary = (new \Mikori\Services\StatsService())->todayForDevice((int) $device['child_id']);
        return Response::ok($summary);
    }

    /** Política de enforcement (V2) para la app Kids. */
    public function policy(Request $request): Response
    {
        $device = $request->device();
        $policy = (new \Mikori\Services\PolicyService())->forChild((int) $device['child_id']);
        return Response::ok($policy);
    }

    /** Registra/actualiza el token FCM del dispositivo. */
    public function updateFcmToken(Request $request): Response
    {
        $data = Validator::validate($request->body, ['fcm_token' => 'required|string|max:255']);
        $device = $request->device();
        $this->devices->updateFcmToken((int) $device['id'], $data['fcm_token']);
        return Response::ok(['message' => 'Token FCM actualizado.']);
    }
}
