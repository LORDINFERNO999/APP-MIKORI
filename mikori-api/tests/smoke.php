<?php

declare(strict_types=1);

/**
 * MIKORI API — Smoke test de extremo a extremo (sin servidor HTTP).
 *
 * Construye peticiones y las despacha por el Router real, ejerciendo
 * middleware, servicios, repositorios y base de datos. Útil en entornos
 * sin posibilidad de abrir un socket.
 *
 * Uso: php tests/smoke.php
 * (Ejecuta antes: php database/migrate.php --fresh)
 */

require __DIR__ . '/../bootstrap.php';

use Mikori\Core\Request;
use Mikori\Core\Router;

/** @var Router $router */
$router = require __DIR__ . '/../config/routes.php';

$pass = 0;
$fail = 0;

/**
 * Despacha una petición simulada y devuelve [status, payload].
 * @return array{0:int,1:array<string,mixed>|null}
 */
function call(Router $router, string $method, string $path, array $body = [], ?string $token = null): array
{
    $headers = ['user-agent' => 'MikoriSmokeTest/1.0'];
    if ($token !== null) {
        $headers['authorization'] = 'Bearer ' . $token;
    }
    $request = new Request($method, $path, $body, [], [], $headers);
    $response = $router->dispatch($request);
    return [$response->status, $response->payload];
}

function check(string $label, bool $condition, mixed $context = null): void
{
    global $pass, $fail;
    if ($condition) {
        $pass++;
        fwrite(STDOUT, "  ✔ {$label}\n");
    } else {
        $fail++;
        fwrite(STDOUT, "  x {$label}\n");
        fwrite(STDOUT, '     contexto: ' . json_encode($context, JSON_UNESCAPED_UNICODE) . "\n");
    }
}

fwrite(STDOUT, "\n== MIKORI API — Smoke test ==\n\n");

// 1) Health
[$s, $p] = call($router, 'GET', '/v1/health');
check('health responde 200', $s === 200, [$s, $p]);
check('health status ok', ($p['data']['status'] ?? null) === 'ok', $p);

// 2) Registro
$email = 'mama+' . substr(bin2hex(random_bytes(4)), 0, 8) . '@mikori.test';
[$s, $p] = call($router, 'POST', '/v1/auth/register', [
    'name' => 'Mamá López', 'email' => $email, 'password' => 'secret1234',
]);
check('registro responde 201', $s === 201, [$s, $p]);
$token = $p['data']['access_token'] ?? null;
$refresh = $p['data']['refresh_token'] ?? null;
check('registro entrega access_token', is_string($token) && $token !== '', $p);

// 3) Registro duplicado → 409
[$s, $p] = call($router, 'POST', '/v1/auth/register', [
    'name' => 'Otra', 'email' => $email, 'password' => 'secret1234',
]);
check('registro duplicado responde 409', $s === 409, [$s, $p]);

// 4) Validación (password corta) → 422
[$s, $p] = call($router, 'POST', '/v1/auth/register', [
    'name' => 'X', 'email' => 'x@y.test', 'password' => '123',
]);
check('validación password corta responde 422', $s === 422, [$s, $p]);

// 5) Login
[$s, $p] = call($router, 'POST', '/v1/auth/login', ['email' => $email, 'password' => 'secret1234']);
check('login responde 200', $s === 200, [$s, $p]);
$token = $p['data']['access_token'] ?? $token;

// 6) Login credenciales inválidas → 401
[$s, $p] = call($router, 'POST', '/v1/auth/login', ['email' => $email, 'password' => 'mala']);
check('login inválido responde 401', $s === 401, [$s, $p]);

// 7) Acceso sin token → 401
[$s, $p] = call($router, 'GET', '/v1/children');
check('children sin token responde 401', $s === 401, [$s, $p]);

// 8) Crear hijo
[$s, $p] = call($router, 'POST', '/v1/children', ['name' => 'Mateo', 'birthdate' => '2016-05-10'], $token);
check('crear hijo responde 201', $s === 201, [$s, $p]);
$childId = $p['data']['id'] ?? null;
check('hijo tiene id', is_int($childId), $p);

// 9) Listar hijos
[$s, $p] = call($router, 'GET', '/v1/children', [], $token);
check('listar hijos responde 200', $s === 200, [$s, $p]);
check('lista contiene 1 hijo', is_array($p['data']) && count($p['data']) === 1, $p);

// 10) Aislamiento: otro usuario no ve al hijo
$email2 = 'papa+' . substr(bin2hex(random_bytes(4)), 0, 8) . '@mikori.test';
[, $p2] = call($router, 'POST', '/v1/auth/register', ['name' => 'Papá', 'email' => $email2, 'password' => 'secret1234']);
$token2 = $p2['data']['access_token'];
[$s, $p] = call($router, 'GET', '/v1/children/' . $childId, [], $token2);
check('otro usuario NO accede al hijo (404)', $s === 404, [$s, $p]);

// 11) Generar código de vinculación
[$s, $p] = call($router, 'POST', '/v1/children/' . $childId . '/link-code', [], $token);
check('generar código responde 201', $s === 201, [$s, $p]);
$code = $p['data']['code'] ?? null;
check('código con formato MIKORI-XXXXXX', is_string($code) && str_starts_with($code, 'MIKORI-'), $p);

// 12) Canjear código (Kids)
[$s, $p] = call($router, 'POST', '/v1/link/redeem', [
    'code' => $code, 'device_uid' => 'device-abc-123', 'model' => 'Pixel 7', 'android_version' => '16',
]);
check('canje responde 201', $s === 201, [$s, $p]);
$deviceToken = $p['data']['device_token'] ?? null;
check('canje entrega device_token', is_string($deviceToken) && $deviceToken !== '', $p);

// 13) Canjear código ya usado → 404
[$s, $p] = call($router, 'POST', '/v1/link/redeem', ['code' => $code, 'device_uid' => 'device-abc-123']);
check('canje de código usado responde 404', $s === 404, [$s, $p]);

// 14) Estado de vinculación (Parent)
[$s, $p] = call($router, 'GET', '/v1/children/' . $childId . '/link-status', [], $token);
check('link-status = linked', ($p['data']['status'] ?? null) === 'linked', [$s, $p]);

// 15) Ingesta de uso (Kids) con token de dispositivo
$today = gmdate('Y-m-d');
[$s, $p] = call($router, 'POST', '/v1/devices/usage', ['items' => [
    ['package' => 'com.google.android.youtube', 'label' => 'YouTube', 'category' => 'video', 'date' => $today, 'seconds' => 2520],
    ['package' => 'com.whatsapp', 'label' => 'WhatsApp', 'category' => 'social', 'date' => $today, 'seconds' => 1140],
    ['package' => 'com.android.chrome', 'label' => 'Chrome', 'category' => 'browser', 'date' => $today, 'seconds' => 900],
]], $deviceToken);
check('ingesta responde 201', $s === 201, [$s, $p]);
check('ingesta acepta 3 registros', ($p['data']['accepted'] ?? 0) === 3, $p);

// 16) Ingesta sin token de dispositivo → 401
[$s, $p] = call($router, 'POST', '/v1/devices/usage', ['items' => []]);
check('ingesta sin token responde 401', $s === 401, [$s, $p]);

// 17) Ingesta acumulativa (mismo package suma)
[$s, $p] = call($router, 'POST', '/v1/devices/usage', ['items' => [
    ['package' => 'com.google.android.youtube', 'date' => $today, 'seconds' => 480],
]], $deviceToken);
check('segunda ingesta responde 201', $s === 201, [$s, $p]);

// 18) Stats de hoy (Parent)
[$s, $p] = call($router, 'GET', '/v1/children/' . $childId . '/stats/today', [], $token);
check('stats today responde 200', $s === 200, [$s, $p]);
$total = $p['data']['total_seconds'] ?? 0;
check('total = 2520+1140+900+480 = 5040s', $total === 5040, $p);
check('top_apps tiene 3 apps', count($p['data']['top_apps'] ?? []) === 3, $p);
check('app top es YouTube (3000s)', ($p['data']['top_apps'][0]['package_name'] ?? '') === 'com.google.android.youtube' && ($p['data']['top_apps'][0]['seconds'] ?? 0) === 3000, $p['data']['top_apps'] ?? null);

// 19) Definir límites (mismo para todos)
[$s, $p] = call($router, 'PUT', '/v1/children/' . $childId . '/limits', ['all' => 120], $token);
check('definir límites (all) responde 200', $s === 200, [$s, $p]);
check('7 días con límite 120', count(array_filter($p['data']['days'], fn ($d) => $d['daily_limit_minutes'] === 120)) === 7, $p['data']['days'] ?? null);

// 20) Límite por día concreto
[$s, $p] = call($router, 'PUT', '/v1/children/' . $childId . '/limits', ['days' => [
    ['day_of_week' => 6, 'minutes' => 240], ['day_of_week' => 7, 'minutes' => 240],
]], $token);
check('definir límites (por día) responde 200', $s === 200, [$s, $p]);

// 21) Stats today ahora refleja límite y restante
[$s, $p] = call($router, 'GET', '/v1/children/' . $childId . '/stats/today', [], $token);
$limit = $p['data']['limit_minutes'] ?? null;
$remaining = $p['data']['remaining_seconds'] ?? null;
check('límite de hoy presente', is_int($limit), $p['data']);
check('restante = límite*60 - 5040', $remaining === ($limit * 60 - 5040), $p['data']);

// 22) Semana
[$s, $p] = call($router, 'GET', '/v1/children/' . $childId . '/stats/week', [], $token);
check('stats week responde 200', $s === 200, [$s, $p]);
check('semana tiene 7 días', count($p['data']['days'] ?? []) === 7, $p);

// 23) Uso por app
[$s, $p] = call($router, 'GET', '/v1/children/' . $childId . '/stats/apps', [], $token);
check('stats apps responde 200', $s === 200, [$s, $p]);
check('apps devuelve 3', count($p['data']['apps'] ?? []) === 3, $p);

// 24) Heartbeat de dispositivo
[$s, $p] = call($router, 'POST', '/v1/devices/heartbeat', [], $deviceToken);
check('heartbeat responde 200', $s === 200, [$s, $p]);

// 25) Refresh token
[$s, $p] = call($router, 'POST', '/v1/auth/refresh', ['refresh_token' => $refresh]);
check('refresh responde 200', $s === 200, [$s, $p]);

// 26) Logout y luego acceso denegado
[$s, $p] = call($router, 'POST', '/v1/auth/logout', [], $token);
check('logout responde 200', $s === 200, [$s, $p]);
[$s, $p] = call($router, 'GET', '/v1/children', [], $token);
check('tras logout, token inválido (401)', $s === 401, [$s, $p]);

// 27) Ruta inexistente → 404
[$s, $p] = call($router, 'GET', '/v1/no-existe');
check('ruta inexistente responde 404', $s === 404, [$s, $p]);

fwrite(STDOUT, "\n== Resultado: {$pass} OK, {$fail} fallos ==\n");
exit($fail === 0 ? 0 : 1);
