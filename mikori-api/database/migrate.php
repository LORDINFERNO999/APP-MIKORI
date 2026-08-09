<?php

declare(strict_types=1);

/**
 * MIKORI API — Ejecutor de migraciones.
 *
 * Uso (CLI):
 *   php database/migrate.php            # aplica migraciones pendientes
 *   php database/migrate.php --fresh    # elimina la BD SQLite y migra de cero
 *
 * Selecciona el DDL según DB_DRIVER (mysql | sqlite).
 */

require __DIR__ . '/../bootstrap.php';

use Mikori\Core\Database;
use Mikori\Core\Env;

$fresh = in_array('--fresh', $argv, true);
$driver = Database::driver();

if ($fresh && $driver === 'sqlite') {
    $path = Env::get('DB_SQLITE_PATH', 'storage/mikori.sqlite');
    if (!str_starts_with($path, '/')) {
        $path = MIKORI_BASE_PATH . '/' . $path;
    }
    if (is_file($path)) {
        unlink($path);
        fwrite(STDOUT, "🗑  BD SQLite eliminada: {$path}\n");
    }
    Database::reset();
}

$pdo = Database::connection();

// Tabla de control de migraciones.
if ($driver === 'sqlite') {
    $pdo->exec('CREATE TABLE IF NOT EXISTS migrations (name TEXT PRIMARY KEY, applied_at TEXT NOT NULL DEFAULT (datetime(\'now\')))');
} else {
    $pdo->exec('CREATE TABLE IF NOT EXISTS migrations (name VARCHAR(190) PRIMARY KEY, applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');
}

$applied = $pdo->query('SELECT name FROM migrations')->fetchAll(\PDO::FETCH_COLUMN);
$applied = array_flip($applied);

/** @var array<string, array{mysql:string, sqlite:string}> $migrations */
$migrations = require __DIR__ . '/migrations.php';

$count = 0;
foreach ($migrations as $name => $ddl) {
    if (isset($applied[$name])) {
        continue;
    }
    $sql = $ddl[$driver] ?? null;
    if ($sql === null) {
        fwrite(STDERR, "⚠  Sin DDL para driver '{$driver}' en migración {$name}\n");
        continue;
    }

    $pdo->exec($sql);
    $stmt = $pdo->prepare('INSERT INTO migrations (name) VALUES (:name)');
    $stmt->execute([':name' => $name]);

    fwrite(STDOUT, "✔  {$name}\n");
    $count++;
}

fwrite(STDOUT, $count === 0 ? "Nada que migrar. Todo al día.\n" : "Migraciones aplicadas: {$count} (driver: {$driver}).\n");
