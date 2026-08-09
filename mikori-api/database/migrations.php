<?php

declare(strict_types=1);

/**
 * Definición de migraciones V1 de MIKORI.
 *
 * Cada migración expone DDL para 'mysql' (producción) y 'sqlite' (pruebas).
 * El esquema MySQL canónico completo vive en mikori-database/schema.sql;
 * aquí se replican las tablas V1 que la API necesita, en ambos dialectos.
 *
 * @return array<string, array{mysql:string, sqlite:string}>
 */

return [

    // ── users ────────────────────────────────────────────────────────────
    '001_users' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                name VARCHAR(120) NOT NULL,
                email VARCHAR(190) NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                email_verified_at DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                UNIQUE KEY uq_users_email (email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                email_verified_at TEXT NULL,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                updated_at TEXT NOT NULL DEFAULT (datetime('now'))
            )
        SQL,
    ],

    // ── children ─────────────────────────────────────────────────────────
    '002_children' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS children (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                user_id BIGINT UNSIGNED NOT NULL,
                name VARCHAR(120) NOT NULL,
                birthdate DATE NULL,
                avatar VARCHAR(190) NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                KEY idx_children_user (user_id),
                CONSTRAINT fk_children_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS children (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                birthdate TEXT NULL,
                avatar TEXT NULL,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                updated_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
            )
        SQL,
    ],

    // ── devices ──────────────────────────────────────────────────────────
    '003_devices' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS devices (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                child_id BIGINT UNSIGNED NOT NULL,
                device_uid VARCHAR(190) NOT NULL,
                model VARCHAR(120) NULL,
                android_version VARCHAR(40) NULL,
                fcm_token VARCHAR(255) NULL,
                api_token_hash VARCHAR(255) NULL,
                status ENUM('online','offline') NOT NULL DEFAULT 'offline',
                last_seen_at DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                UNIQUE KEY uq_devices_uid (device_uid),
                KEY idx_devices_child (child_id),
                CONSTRAINT fk_devices_child FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS devices (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                child_id INTEGER NOT NULL,
                device_uid TEXT NOT NULL UNIQUE,
                model TEXT NULL,
                android_version TEXT NULL,
                fcm_token TEXT NULL,
                api_token_hash TEXT NULL,
                status TEXT NOT NULL DEFAULT 'offline' CHECK (status IN ('online','offline')),
                last_seen_at TEXT NULL,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                updated_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE
            )
        SQL,
    ],

    // ── device_links ─────────────────────────────────────────────────────
    '004_device_links' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS device_links (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                user_id BIGINT UNSIGNED NOT NULL,
                child_id BIGINT UNSIGNED NOT NULL,
                code VARCHAR(20) NOT NULL,
                status ENUM('pending','linked','expired') NOT NULL DEFAULT 'pending',
                expires_at DATETIME NOT NULL,
                linked_device_id BIGINT UNSIGNED NULL,
                linked_at DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                UNIQUE KEY uq_links_code (code),
                KEY idx_links_user (user_id),
                KEY idx_links_child (child_id),
                CONSTRAINT fk_links_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                CONSTRAINT fk_links_child FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE,
                CONSTRAINT fk_links_device FOREIGN KEY (linked_device_id) REFERENCES devices (id) ON DELETE SET NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS device_links (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                child_id INTEGER NOT NULL,
                code TEXT NOT NULL UNIQUE,
                status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','linked','expired')),
                expires_at TEXT NOT NULL,
                linked_device_id INTEGER NULL,
                linked_at TEXT NULL,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE,
                FOREIGN KEY (linked_device_id) REFERENCES devices (id) ON DELETE SET NULL
            )
        SQL,
    ],

    // ── applications ─────────────────────────────────────────────────────
    '005_applications' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS applications (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                package_name VARCHAR(190) NOT NULL,
                app_label VARCHAR(190) NULL,
                category VARCHAR(80) NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                UNIQUE KEY uq_apps_package (package_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS applications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                package_name TEXT NOT NULL UNIQUE,
                app_label TEXT NULL,
                category TEXT NULL,
                created_at TEXT NOT NULL DEFAULT (datetime('now'))
            )
        SQL,
    ],

    // ── app_usage ────────────────────────────────────────────────────────
    '006_app_usage' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS app_usage (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                device_id BIGINT UNSIGNED NOT NULL,
                application_id BIGINT UNSIGNED NOT NULL,
                usage_date DATE NOT NULL,
                duration_seconds INT UNSIGNED NOT NULL DEFAULT 0,
                started_at DATETIME NULL,
                ended_at DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                KEY idx_usage_device_date (device_id, usage_date),
                KEY idx_usage_app (application_id),
                CONSTRAINT fk_usage_device FOREIGN KEY (device_id) REFERENCES devices (id) ON DELETE CASCADE,
                CONSTRAINT fk_usage_app FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS app_usage (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                device_id INTEGER NOT NULL,
                application_id INTEGER NOT NULL,
                usage_date TEXT NOT NULL,
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                started_at TEXT NULL,
                ended_at TEXT NULL,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (device_id) REFERENCES devices (id) ON DELETE CASCADE,
                FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
            )
        SQL,
    ],

    '006b_app_usage_index' => [
        'mysql' => 'SELECT 1',
        'sqlite' => 'CREATE INDEX IF NOT EXISTS idx_usage_device_date ON app_usage (device_id, usage_date)',
    ],

    // ── usage_rules ──────────────────────────────────────────────────────
    '007_usage_rules' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS usage_rules (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                child_id BIGINT UNSIGNED NOT NULL,
                day_of_week TINYINT UNSIGNED NOT NULL,
                daily_limit_minutes SMALLINT UNSIGNED NOT NULL,
                active TINYINT(1) NOT NULL DEFAULT 1,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                UNIQUE KEY uq_rules_child_day (child_id, day_of_week),
                CONSTRAINT fk_rules_child FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS usage_rules (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                child_id INTEGER NOT NULL,
                day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
                daily_limit_minutes INTEGER NOT NULL,
                active INTEGER NOT NULL DEFAULT 1,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                updated_at TEXT NOT NULL DEFAULT (datetime('now')),
                UNIQUE (child_id, day_of_week),
                FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE
            )
        SQL,
    ],

    // ── sessions ─────────────────────────────────────────────────────────
    '008_sessions' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS sessions (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                user_id BIGINT UNSIGNED NOT NULL,
                token_hash VARCHAR(255) NOT NULL,
                refresh_hash VARCHAR(255) NULL,
                user_agent VARCHAR(255) NULL,
                ip_address VARCHAR(45) NULL,
                expires_at DATETIME NOT NULL,
                revoked TINYINT(1) NOT NULL DEFAULT 0,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                KEY idx_sessions_user (user_id),
                KEY idx_sessions_token (token_hash),
                CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                token_hash TEXT NOT NULL,
                refresh_hash TEXT NULL,
                user_agent TEXT NULL,
                ip_address TEXT NULL,
                expires_at TEXT NOT NULL,
                revoked INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
            )
        SQL,
    ],

    // ── password_resets ──────────────────────────────────────────────────
    '009_password_resets' => [
        'mysql' => <<<SQL
            CREATE TABLE IF NOT EXISTS password_resets (
                id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                user_id BIGINT UNSIGNED NOT NULL,
                token_hash VARCHAR(255) NOT NULL,
                expires_at DATETIME NOT NULL,
                used TINYINT(1) NOT NULL DEFAULT 0,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                KEY idx_resets_token (token_hash),
                CONSTRAINT fk_resets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        SQL,
        'sqlite' => <<<SQL
            CREATE TABLE IF NOT EXISTS password_resets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                token_hash TEXT NOT NULL,
                expires_at TEXT NOT NULL,
                used INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
            )
        SQL,
    ],
];
