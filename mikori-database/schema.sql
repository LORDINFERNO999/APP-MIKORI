-- ═══════════════════════════════════════════════════════════════════════
--  MIKORI — Esquema de base de datos (MySQL 8)
--  "Crece, juega y descubre."
--
--  Diseño normalizado (3NF).
--  Tablas V1: users, children, devices, device_links, applications,
--             app_usage, usage_rules, sessions
--  Tablas preparadas para V2/V3: app_rules, schedules, device_commands,
--             notifications
--
--  Convenciones:
--   - InnoDB + utf8mb4
--   - PK BIGINT UNSIGNED AUTO_INCREMENT
--   - Claves foráneas con ON DELETE CASCADE donde aplica el aislamiento por dueño
--   - Timestamps en UTC
-- ═══════════════════════════════════════════════════════════════════════

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- ─────────────────────────────────────────────────────────────
-- users : padres / tutores
-- ─────────────────────────────────────────────────────────────
CREATE TABLE users (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name            VARCHAR(120)    NOT NULL,
    email           VARCHAR(190)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    email_verified_at DATETIME      NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- children : perfiles de hijos (pertenecen a un padre)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE children (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id     BIGINT UNSIGNED NOT NULL,
    name        VARCHAR(120)    NOT NULL,
    birthdate   DATE            NULL,               -- edad opcional
    avatar      VARCHAR(190)    NULL,               -- clave/URL del avatar
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_children_user (user_id),
    CONSTRAINT fk_children_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- devices : dispositivos Android asociados a un hijo
-- ─────────────────────────────────────────────────────────────
CREATE TABLE devices (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    child_id        BIGINT UNSIGNED NOT NULL,
    device_uid      VARCHAR(190)    NOT NULL,        -- identificador estable generado por la app Kids
    model           VARCHAR(120)    NULL,
    android_version VARCHAR(40)     NULL,
    fcm_token       VARCHAR(255)    NULL,
    status          ENUM('online','offline') NOT NULL DEFAULT 'offline',
    last_seen_at    DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_devices_uid (device_uid),
    KEY idx_devices_child (child_id),
    CONSTRAINT fk_devices_child FOREIGN KEY (child_id)
        REFERENCES children (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- device_links : códigos de vinculación padre → dispositivo del hijo
--   El padre genera el código (MIKORI-XXXXXX). El hijo lo canjea.
--   El código expira.
-- ─────────────────────────────────────────────────────────────
CREATE TABLE device_links (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id           BIGINT UNSIGNED NOT NULL,
    child_id          BIGINT UNSIGNED NOT NULL,
    code              VARCHAR(20)     NOT NULL,      -- p.ej. MIKORI-483921
    status            ENUM('pending','linked','expired') NOT NULL DEFAULT 'pending',
    expires_at        DATETIME        NOT NULL,
    linked_device_id  BIGINT UNSIGNED NULL,
    linked_at         DATETIME        NULL,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_links_code (code),
    KEY idx_links_user (user_id),
    KEY idx_links_child (child_id),
    CONSTRAINT fk_links_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_links_child FOREIGN KEY (child_id)
        REFERENCES children (id) ON DELETE CASCADE,
    CONSTRAINT fk_links_device FOREIGN KEY (linked_device_id)
        REFERENCES devices (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- applications : catálogo de apps observadas (global, deduplicado por paquete)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE applications (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    package_name  VARCHAR(190)    NOT NULL,
    app_label     VARCHAR(190)    NULL,
    category      VARCHAR(80)     NULL,             -- juegos, social, navegador, etc.
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_apps_package (package_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- app_usage : historial de uso por dispositivo / app / día
--   Origen: UsageStatsManager (agregado por día; sesión con hora inicio/fin cuando es posible)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE app_usage (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    device_id         BIGINT UNSIGNED NOT NULL,
    application_id    BIGINT UNSIGNED NOT NULL,
    usage_date        DATE            NOT NULL,
    duration_seconds  INT UNSIGNED    NOT NULL DEFAULT 0,
    started_at        DATETIME        NULL,
    ended_at          DATETIME        NULL,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_usage_device_date (device_id, usage_date),
    KEY idx_usage_app (application_id),
    CONSTRAINT fk_usage_device FOREIGN KEY (device_id)
        REFERENCES devices (id) ON DELETE CASCADE,
    CONSTRAINT fk_usage_app FOREIGN KEY (application_id)
        REFERENCES applications (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- usage_rules : límites diarios de tiempo total por hijo (V1)
--   day_of_week: 1=Lunes ... 7=Domingo. Un registro por día.
-- ─────────────────────────────────────────────────────────────
CREATE TABLE usage_rules (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    child_id            BIGINT UNSIGNED NOT NULL,
    day_of_week         TINYINT UNSIGNED NOT NULL,  -- 1..7
    daily_limit_minutes SMALLINT UNSIGNED NOT NULL, -- p.ej. 120 = 2h
    active              TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_rules_child_day (child_id, day_of_week),
    CONSTRAINT fk_rules_child FOREIGN KEY (child_id)
        REFERENCES children (id) ON DELETE CASCADE,
    CONSTRAINT chk_rules_dow CHECK (day_of_week BETWEEN 1 AND 7)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- sessions : tokens de autenticación (access/refresh) por usuario
-- ─────────────────────────────────────────────────────────────
CREATE TABLE sessions (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id       BIGINT UNSIGNED NOT NULL,
    token_hash    VARCHAR(255)    NOT NULL,
    refresh_hash  VARCHAR(255)    NULL,
    user_agent    VARCHAR(255)    NULL,
    ip_address    VARCHAR(45)     NULL,
    expires_at    DATETIME        NOT NULL,
    revoked       TINYINT(1)      NOT NULL DEFAULT 0,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sessions_user (user_id),
    KEY idx_sessions_token (token_hash),
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════════════
--  TABLAS PREPARADAS PARA V2 / V3
--  (se crean ahora para no romper el esquema después; se usan más adelante)
-- ═══════════════════════════════════════════════════════════════════════

-- app_rules (V2): límite por app y bloqueo por app
CREATE TABLE app_rules (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    child_id        BIGINT UNSIGNED NOT NULL,
    application_id  BIGINT UNSIGNED NOT NULL,
    max_minutes     SMALLINT UNSIGNED NULL,         -- límite diario por app
    is_blocked      TINYINT(1)      NOT NULL DEFAULT 0,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_apprules_child_app (child_id, application_id),
    CONSTRAINT fk_apprules_child FOREIGN KEY (child_id)
        REFERENCES children (id) ON DELETE CASCADE,
    CONSTRAINT fk_apprules_app FOREIGN KEY (application_id)
        REFERENCES applications (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- schedules (V2): horarios escolar/nocturno/personalizado
CREATE TABLE schedules (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    child_id    BIGINT UNSIGNED NOT NULL,
    name        VARCHAR(120)    NOT NULL,
    type        ENUM('school','night','custom') NOT NULL DEFAULT 'custom',
    start_time  TIME            NOT NULL,
    end_time    TIME            NOT NULL,
    days_mask   TINYINT UNSIGNED NOT NULL DEFAULT 127, -- bitmask L..D (bit0=Lun ... bit6=Dom)
    active      TINYINT(1)      NOT NULL DEFAULT 1,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sched_child (child_id),
    CONSTRAINT fk_sched_child FOREIGN KEY (child_id)
        REFERENCES children (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- device_commands (V3): comandos remotos (bloquear/desbloquear/pausar...)
CREATE TABLE device_commands (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    device_id   BIGINT UNSIGNED NOT NULL,
    type        ENUM('lock','unlock','pause','resume','sync') NOT NULL,
    payload     JSON            NULL,
    status      ENUM('pending','delivered','applied','failed') NOT NULL DEFAULT 'pending',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at DATETIME       NULL,
    applied_at  DATETIME        NULL,
    PRIMARY KEY (id),
    KEY idx_cmd_device_status (device_id, status),
    CONSTRAINT fk_cmd_device FOREIGN KEY (device_id)
        REFERENCES devices (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- notifications (V3): alertas al padre
CREATE TABLE notifications (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id     BIGINT UNSIGNED NOT NULL,
    child_id    BIGINT UNSIGNED NULL,
    type        VARCHAR(60)     NOT NULL,           -- limit_reached, device_offline, etc.
    message     VARCHAR(255)    NOT NULL,
    read_at     DATETIME        NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notif_user (user_id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_child FOREIGN KEY (child_id)
        REFERENCES children (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
