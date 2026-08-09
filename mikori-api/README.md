# MIKORI API

Backend REST de MIKORI.

> **Nota de implementación:** El sandbox de desarrollo no tiene acceso a internet,
> por lo que Composer no puede instalar Laravel. Para que el backend sea **real y
> ejecutable ya**, esta V1 está construida en **PHP puro (sin dependencias)** con una
> arquitectura por capas limpia (Router → Controller → Service → Repository) y un
> autoloader PSR-4 propio. El código apunta a **MySQL** en producción y usa **SQLite**
> para desarrollo/pruebas (driver conmutable por `.env`). Es migrable a Laravel cuando
> haya red, conservando la misma separación de capas.

## Requisitos
- PHP 8.2+ con extensiones `pdo_mysql` (prod) o `pdo_sqlite` (dev), `mbstring`, `openssl`.

## Puesta en marcha (desarrollo, SQLite)
```bash
cd mikori-api
cp .env.example .env          # DB_DRIVER=sqlite por defecto
php database/migrate.php      # crea las tablas V1
php -S 127.0.0.1:8000 -t public
```
La API queda en `http://127.0.0.1:8000`.

### Producción (MySQL)
1. Crear la BD e importar el esquema canónico: `mysql mikori < ../mikori-database/schema.sql`
   (o ejecutar `php database/migrate.php` con `DB_DRIVER=mysql`).
2. Configurar `.env` con credenciales MySQL y `APP_DEBUG=false`.
3. Servir `public/` detrás de HTTPS (Nginx/Apache + PHP-FPM).

## Arquitectura
```
public/index.php         → front controller
src/Core/                → Router, Request, Response, Database, Validator, Env, Route
src/Middleware/          → AuthMiddleware (padre), DeviceAuthMiddleware (Kids)
src/Support/             → Hash, Token, Clock
src/Repositories/        → acceso a datos (PDO preparado)
src/Services/            → lógica de negocio
src/Controllers/         → endpoints HTTP
config/routes.php        → definición de rutas
database/                → migrate.php + migrations.php
```

## Autenticación
- **Padre (usuario):** `Authorization: Bearer <access_token>` — obtenido en register/login.
- **Dispositivo (Kids):** `Authorization: Bearer <device_token>` — obtenido al canjear el código.
- Los tokens se entregan en claro una sola vez; en la BD solo se guarda su hash SHA-256.

## Endpoints (v1)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/v1/health` | — | Estado del servicio |
| POST | `/v1/auth/register` | — | Registro de padre |
| POST | `/v1/auth/login` | — | Login |
| POST | `/v1/auth/refresh` | — | Renovar tokens |
| POST | `/v1/auth/password/forgot` | — | Solicitar recuperación |
| POST | `/v1/auth/password/reset` | — | Restablecer contraseña |
| POST | `/v1/auth/logout` | padre | Cerrar sesión |
| GET | `/v1/children` | padre | Listar hijos |
| POST | `/v1/children` | padre | Crear hijo |
| GET | `/v1/children/{id}` | padre | Detalle (+ dispositivos) |
| PATCH | `/v1/children/{id}` | padre | Editar hijo |
| DELETE | `/v1/children/{id}` | padre | Eliminar hijo |
| POST | `/v1/children/{id}/link-code` | padre | Generar código de vinculación |
| GET | `/v1/children/{id}/link-status` | padre | Estado de vinculación |
| POST | `/v1/link/redeem` | — (Kids) | Canjear código y obtener token de dispositivo |
| GET | `/v1/children/{id}/stats/today` | padre | Resumen de hoy |
| GET | `/v1/children/{id}/stats/week` | padre | Serie semanal |
| GET | `/v1/children/{id}/stats/apps` | padre | Uso por app (`?from=&to=`) |
| GET | `/v1/children/{id}/limits` | padre | Obtener límites diarios |
| PUT | `/v1/children/{id}/limits` | padre | Definir límites |
| POST | `/v1/devices/usage` | dispositivo | Ingesta por lotes de uso |
| POST | `/v1/devices/heartbeat` | dispositivo | Latido de estado |
| PATCH | `/v1/devices/fcm-token` | dispositivo | Actualizar token FCM |

### Formato de respuesta
- Éxito: `{ "data": ... , "meta"?: ... }`
- Error: `{ "error": { "code": "...", "message": "...", "details"?: ... } }`

### Ejemplo: definir límites
```json
// PUT /v1/children/1/limits  — mismo límite todos los días (minutos)
{ "all": 120 }

// o por día (1=Lunes ... 7=Domingo)
{ "days": [ {"day_of_week": 5, "minutes": 180}, {"day_of_week": 6, "minutes": 240} ] }
```

### Ejemplo: ingesta de uso (Kids)
```json
// POST /v1/devices/usage
{
  "items": [
    { "package": "com.google.android.youtube", "label": "YouTube", "category": "video", "date": "2026-08-09", "seconds": 2520 },
    { "package": "com.whatsapp", "label": "WhatsApp", "date": "2026-08-09", "seconds": 1140 }
  ]
}
```

> Endpoints y decisiones de diseño ampliados en [`../docs/01-arquitectura.md`](../docs/01-arquitectura.md) §10.
