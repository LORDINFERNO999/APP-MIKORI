# MIKORI — Modelo de datos (ERD)

Diagrama entidad-relación del esquema definido en [`schema.sql`](./schema.sql).

```mermaid
erDiagram
    users ||--o{ children : "tiene"
    users ||--o{ device_links : "genera"
    users ||--o{ sessions : "abre"
    users ||--o{ notifications : "recibe"

    children ||--o{ devices : "usa"
    children ||--o{ device_links : "se vincula por"
    children ||--o{ usage_rules : "tiene limites"
    children ||--o{ app_rules : "tiene reglas (V2)"
    children ||--o{ schedules : "tiene horarios (V2)"
    children ||--o{ notifications : "referida en"

    devices ||--o{ app_usage : "registra"
    devices ||--o{ device_commands : "recibe (V3)"
    devices |o--o| device_links : "canjeado por"

    applications ||--o{ app_usage : "aparece en"
    applications ||--o{ app_rules : "regulada por (V2)"

    users {
        bigint id PK
        varchar email UK
        varchar password_hash
    }
    children {
        bigint id PK
        bigint user_id FK
        varchar name
        date birthdate
        varchar avatar
    }
    devices {
        bigint id PK
        bigint child_id FK
        varchar device_uid UK
        varchar fcm_token
        enum status
        datetime last_seen_at
    }
    device_links {
        bigint id PK
        bigint user_id FK
        bigint child_id FK
        varchar code UK
        enum status
        datetime expires_at
    }
    applications {
        bigint id PK
        varchar package_name UK
        varchar app_label
        varchar category
    }
    app_usage {
        bigint id PK
        bigint device_id FK
        bigint application_id FK
        date usage_date
        int duration_seconds
    }
    usage_rules {
        bigint id PK
        bigint child_id FK
        tinyint day_of_week
        smallint daily_limit_minutes
    }
    sessions {
        bigint id PK
        bigint user_id FK
        varchar token_hash
        datetime expires_at
    }
```

## Cardinalidades clave
- Un **padre** tiene varios **hijos**.
- Un **hijo** puede tener uno o varios **dispositivos**.
- Un **dispositivo** acumula historial en **app_usage**.
- Un **hijo** tiene **límites diarios** (`usage_rules`), y en V2 reglas por app y horarios.

## Notas de versión
- **V1** usa: `users`, `children`, `devices`, `device_links`, `applications`, `app_usage`, `usage_rules`, `sessions`.
- **V2/V3** usan: `app_rules`, `schedules`, `device_commands`, `notifications` (creadas desde ya).
