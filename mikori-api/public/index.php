<?php

declare(strict_types=1);

/**
 * MIKORI API — Front controller.
 * Todas las peticiones HTTPS entran por aquí.
 */

require __DIR__ . '/../bootstrap.php';

use Mikori\Core\Request;
use Mikori\Core\Router;

// CORS básico (las apps Android no lo requieren, útil para pruebas/dashboards).
header('Access-Control-Allow-Origin: ' . (\Mikori\Core\Env::get('CORS_ORIGIN', '*')));
header('Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'OPTIONS') {
    http_response_code(204);
    exit;
}

/** @var Router $router */
$router = require __DIR__ . '/../config/routes.php';

$request = Request::fromGlobals();
$response = $router->dispatch($request);
$response->send();
