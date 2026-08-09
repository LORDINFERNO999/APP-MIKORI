<?php

declare(strict_types=1);

namespace Mikori\Core;

use PDO;
use RuntimeException;

/**
 * Fábrica de conexión PDO (singleton).
 *
 * Soporta dos drivers vía .env:
 *   - mysql  (producción)
 *   - sqlite (desarrollo/pruebas en entornos sin MySQL)
 *
 * Usa siempre sentencias preparadas (protección contra SQL injection).
 */
final class Database
{
    private static ?PDO $pdo = null;

    public static function connection(): PDO
    {
        if (self::$pdo instanceof PDO) {
            return self::$pdo;
        }

        $driver = Env::get('DB_DRIVER', 'sqlite');

        $options = [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ];

        if ($driver === 'sqlite') {
            $path = Env::get('DB_SQLITE_PATH', 'storage/mikori.sqlite');
            if (!str_starts_with($path, '/')) {
                $path = MIKORI_BASE_PATH . '/' . $path;
            }
            $dir = dirname($path);
            if (!is_dir($dir)) {
                mkdir($dir, 0775, true);
            }
            $pdo = new PDO('sqlite:' . $path, null, null, $options);
            $pdo->exec('PRAGMA foreign_keys = ON');
        } elseif ($driver === 'mysql') {
            $host = Env::get('DB_HOST', '127.0.0.1');
            $port = Env::get('DB_PORT', '3306');
            $db = Env::get('DB_DATABASE', 'mikori');
            $user = Env::get('DB_USERNAME', 'root');
            $pass = Env::get('DB_PASSWORD', '');
            $dsn = sprintf('mysql:host=%s;port=%s;dbname=%s;charset=utf8mb4', $host, $port, $db);
            $pdo = new PDO($dsn, $user, $pass, $options);
        } else {
            throw new RuntimeException("Driver de base de datos no soportado: {$driver}");
        }

        self::$pdo = $pdo;
        return $pdo;
    }

    public static function driver(): string
    {
        return Env::get('DB_DRIVER', 'sqlite');
    }

    /** Útil para pruebas: fuerza reconexión. */
    public static function reset(): void
    {
        self::$pdo = null;
    }
}
