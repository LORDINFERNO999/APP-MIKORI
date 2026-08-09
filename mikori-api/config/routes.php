<?php

declare(strict_types=1);

/**
 * MIKORI API — Definición de rutas (v1).
 *
 * Guardias:
 *   ->auth()        exige token de usuario (padre)
 *   ->deviceAuth()  exige token de dispositivo (app Kids)
 *   (sin guardia)   público
 */

use Mikori\Controllers\AuthController;
use Mikori\Controllers\ChildController;
use Mikori\Controllers\DeviceController;
use Mikori\Controllers\HealthController;
use Mikori\Controllers\LimitController;
use Mikori\Controllers\LinkController;
use Mikori\Controllers\StatsController;
use Mikori\Core\Router;

$router = new Router();

// ── Salud ─────────────────────────────────────────────────────────────────
$router->get('/', [HealthController::class, 'index']);
$router->get('/v1/health', [HealthController::class, 'index']);

// ── Auth ────────────────────────────────────────────────────────────────── 
$router->post('/v1/auth/register', [AuthController::class, 'register']);
$router->post('/v1/auth/login', [AuthController::class, 'login']);
$router->post('/v1/auth/refresh', [AuthController::class, 'refresh']);
$router->post('/v1/auth/password/forgot', [AuthController::class, 'forgotPassword']);
$router->post('/v1/auth/password/reset', [AuthController::class, 'resetPassword']);
$router->post('/v1/auth/logout', [AuthController::class, 'logout'])->auth();

// ── Hijos (Parent) ──────────────────────────────────────────────────────────
$router->get('/v1/children', [ChildController::class, 'index'])->auth();
$router->post('/v1/children', [ChildController::class, 'store'])->auth();
$router->get('/v1/children/{id}', [ChildController::class, 'show'])->auth();
$router->patch('/v1/children/{id}', [ChildController::class, 'update'])->auth();
$router->delete('/v1/children/{id}', [ChildController::class, 'destroy'])->auth();

// ── Vinculación ─────────────────────────────────────────────────────────────
$router->post('/v1/children/{id}/link-code', [LinkController::class, 'generate'])->auth();
$router->get('/v1/children/{id}/link-status', [LinkController::class, 'status'])->auth();
$router->post('/v1/link/redeem', [LinkController::class, 'redeem']); // [Kids] público (canjea código)

// ── Estadísticas (Parent lee) ─────────────────────────────────────────────── 
$router->get('/v1/children/{id}/stats/today', [StatsController::class, 'today'])->auth();
$router->get('/v1/children/{id}/stats/week', [StatsController::class, 'week'])->auth();
$router->get('/v1/children/{id}/stats/apps', [StatsController::class, 'apps'])->auth();

// ── Límites ──────────────────────────────────────────────────────────────── 
$router->get('/v1/children/{id}/limits', [LimitController::class, 'index'])->auth();
$router->put('/v1/children/{id}/limits', [LimitController::class, 'update'])->auth();

// ── Dispositivo (Kids) ──────────────────────────────────────────────────────
$router->post('/v1/devices/usage', [StatsController::class, 'ingest'])->deviceAuth();
$router->post('/v1/devices/heartbeat', [DeviceController::class, 'heartbeat'])->deviceAuth();
$router->patch('/v1/devices/fcm-token', [DeviceController::class, 'updateFcmToken'])->deviceAuth();

return $router;
