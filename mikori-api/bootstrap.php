<?php

declare(strict_types=1);

/**
 * MIKORI API — Bootstrap
 *
 * Carga el autoloader PSR-4 propio (sin Composer, por restricción de red del
 * sandbox) y las variables de entorno. Producción puede usar Composer si hay red.
 */

define('MIKORI_BASE_PATH', __DIR__);

// ─── Autoloader PSR-4 mínimo ──────────────────────────────────────────────
// Namespace raíz: Mikori\  →  src/
spl_autoload_register(static function (string $class): void {
    $prefix = 'Mikori\\';
    $baseDir = __DIR__ . '/src/';

    if (!str_starts_with($class, $prefix)) {
        return;
    }

    $relative = substr($class, strlen($prefix));
    $file = $baseDir . str_replace('\\', '/', $relative) . '.php';

    if (is_file($file)) {
        require $file;
    }
});

// ─── Variables de entorno (.env) ──────────────────────────────────────────
\Mikori\Core\Env::load(__DIR__ . '/.env');
