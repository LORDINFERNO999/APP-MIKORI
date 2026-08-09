# 🌸 MIKORI — Arquitectura Técnica

> **Crece, juega y descubre.**
>
> Plataforma de control y acompañamiento parental para **Android**.
> Este documento es la fuente de verdad de la arquitectura. Se actualiza antes de implementar cambios estructurales.

---

## Índice

1. [Resumen ejecutivo y decisión clave](#1-resumen-ejecutivo-y-decisión-clave)
2. [Componentes del producto](#2-componentes-del-producto)
3. [Arquitectura general](#3-arquitectura-general)
4. [Tecnologías definitivas](#4-tecnologías-definitivas)
5. [Estructura de carpetas (monorepo)](#5-estructura-de-carpetas-monorepo)
6. [Arquitectura MIKORI Parent](#6-arquitectura-mikori-parent)
7. [Arquitectura MIKORI Kids](#7-arquitectura-mikori-kids)
8. [Arquitectura del backend (API)](#8-arquitectura-del-backend-api)
9. [Base de datos](#9-base-de-datos)
10. [Endpoints principales de la API](#10-endpoints-principales-de-la-api)
11. [Permisos Android necesarios para V1](#11-permisos-android-necesarios-para-v1)
12. [APIs de Android que utilizaremos](#12-apis-de-android-que-utilizaremos)
13. [Riesgos y limitaciones](#13-riesgos-y-limitaciones)
14. [Plan de desarrollo de V1](#14-plan-de-desarrollo-de-v1)

---

## 1. Resumen ejecutivo y decisión clave

La decisión de arquitectura que condiciona todo el proyecto es:

> **¿Cómo se aplicará el bloqueo/control en el dispositivo del hijo (V2/V3)?**

En Android moderno solo existen 3 caminos reales, y ninguno es "gratis":

| Camino | Poder de control | Costo / Restricción | Viable en Google Play |
|---|---|---|---|
| **A. UsageStats + Overlay** (Accessibility opcional) | Medio (bloqueo "suave") | Fácil de instalar, el niño puede sortearlo | ⚠️ Con declaración |
| **B. AccessibilityService** | Alto (detecta y tapa apps en primer plano) | Google audita y rechaza muchas apps de control parental aquí | ⚠️ Alto riesgo |
| **C. Device Owner (DevicePolicyManager)** | Muy alto (bloqueo real a nivel de SO) | Requiere factory reset + provisioning, no instalación normal | ✅ Legítimo, UX dura |

**Decisión recomendada:**

- **V1 = MONITOREO PURO** → NO toca este dilema. Usa únicamente `UsageStatsManager` (lectura). 100% legítimo, sin zonas grises. Es donde empezamos.
- **La elección A vs C se decide ANTES de V2** (control real). Ver [Riesgos](#13-riesgos-y-limitaciones).

> Fuentes verificadas (ago-2026): Google Play exige que las apps nuevas targeten **Android 16 (API 36)+** ([target-sdk](https://developer.android.com/google/play/requirements/target-sdk)); el uso de `AccessibilityService` requiere completar el **Permissions Declaration Form** y aprobación ([uso de AccessibilityService API](https://support.google.com/googleplay/android-developer/answer/10964491)). *Contenido reformulado para cumplir restricciones de licencia.*

---

## 2. Componentes del producto

| Componente | Descripción | Tecnología |
|---|---|---|
| **MIKORI Parent** | App Android para padres (lectura/gestión) | Kotlin + Compose |
| **MIKORI Kids** | App Android en el dispositivo del hijo (recolección transparente) | Kotlin + Compose |
| **MIKORI API** | Backend REST | PHP (Laravel) |
| **MIKORI Database** | Base de datos relacional | MySQL 8 |
| **MIKORI Cloud** | Comunicación y sincronización | FCM + REST |

---

## 3. Arquitectura general

```
┌────────────────┐         HTTPS/REST        ┌─────────────────┐
│ MIKORI Parent  │ ────────────────────────► │                 │
│  (Android)     │ ◄──────── FCM ──────────── │   MIKORI API    │
└────────────────┘                            │   (PHP REST)    │
                                               │                 │
┌────────────────┐         HTTPS/REST         │                 │
│  MIKORI Kids   │ ────────────────────────► │                 │
│  (Android)     │ ◄──────── FCM ──────────── │                 │
└────────────────┘                            └────────┬────────┘
       │ (recolecta uso local)                          │
       ▼                                                 ▼
  UsageStatsManager                              ┌──────────────┐
                                                 │    MySQL 8   │
                                                 └──────────────┘
```

Principios:
- **Sin conexiones permanentes en segundo plano.** Kids sube por lotes con WorkManager.
- **Comandos remotos (V3)** = patrón *notification-as-trigger*: FCM avisa, el comando real se descarga por REST.
- **Estado en línea** = heartbeat ligero + `last_seen_at`, no sockets.
- **Aislamiento por usuario (multi-tenant):** cada consulta filtra por el `user_id` del token.

Patrón por app Android: **MVVM + Repository**
```
UI (Compose) → ViewModel → UseCase (opc.) → Repository → {
    RemoteDataSource (Retrofit),
    LocalDataSource (Room / DataStore),
    AndroidDataSource (UsageStats) }
```

---

## 4. Tecnologías definitivas

### Android (Parent y Kids)
- Kotlin, Jetpack Compose, Material 3
- **minSdk 26 (Android 8.0)** / **targetSdk 36 (Android 16)** — obligatorio para publicar apps nuevas en Play.
- Retrofit + OkHttp + Kotlinx Serialization
- Room (cache/offline), DataStore (tokens/settings)
- Hilt (DI), Coroutines + Flow
- WorkManager (sync periódica), Firebase Messaging (FCM)
- Navigation Compose

### Backend (MIKORI API)
- **PHP 8.2+ con Laravel** (auth, Eloquent, migraciones, validación, colas)
- MySQL 8.x, HTTPS obligatorio
- Autenticación por tokens (Sanctum o JWT access+refresh)
- Firebase Admin SDK (envío de FCM)

### Infra
- Configuración por ambientes (`.env`: dev / staging / prod)
- Migraciones versionadas de BD

---

## 5. Estructura de carpetas (monorepo)

```
APP-MIKORI/
├── mikori-parent/            # App Android padres
├── mikori-kids/              # App Android hijo
├── mikori-api/               # Backend PHP (Laravel)
├── mikori-database/          # schema.sql, ERD, docs de esquema
└── docs/                     # Arquitectura, decisiones, permisos
```

Layout interno de cada app Android:
```
app/src/main/java/com/mikori/<parent|kids>/
├── ui/               # Compose screens + theme (Material 3, identidad MIKORI)
├── viewmodel/
├── data/{remote,local,repository,dto,mapper}
├── domain/{model,usecase}
├── di/               # Hilt modules
├── core/{network,error,session}
└── service/          # (solo Kids) UsageCollector, ForegroundService, FCMService
```

---

## 6. Arquitectura MIKORI Parent

App de **lectura/gestión**, sin permisos especiales de Android (solo notificaciones).

- **UI (Compose):** Login/Registro, Dashboard "Mi familia", Detalle de hijo, Estadísticas (hoy/semana), Límites, Vinculación (generar código).
- **ViewModels:** exponen `StateFlow<UiState>` (Loading / Success / Error).
- **Repositories:** `AuthRepository`, `FamilyRepository`, `StatsRepository`, `LimitsRepository`.
- **Remote:** Retrofit `MikoriApiService`. **Local:** DataStore para token/sesión.
- **Errores centralizados:** `sealed class MikoriError` + interceptor OkHttp (HTTP → error de dominio).
- En V1 hace *pull*; en V3 recibe alertas por FCM.

---

## 7. Arquitectura MIKORI Kids

App **transparente**, instalada en el teléfono del hijo. Indica siempre que está gestionada por MIKORI y el tutor.

- **Onboarding:** explica qué hace la app + solicitud guiada de permisos con justificación (Usage Access).
- **UsageCollector:** lee tiempo de pantalla y por app con `UsageStatsManager`.
- **Foreground Service ligero + WorkManager:** recolecta y sube por lotes respetando batería.
- **FCMService (V3):** recibe el trigger → descarga el comando por REST → ejecuta con mecanismos oficiales.
- **Room:** buffer offline; reintenta subida al recuperar red.
- **Anti-spyware por diseño:** sin micrófono/cámara/keylogger/lectura de mensajes. Solo APIs oficiales de uso.

---

## 8. Arquitectura del backend (API)

- **Capas:** Controller → Service (lógica) → Repository → Model (Eloquent).
- **Auth:** registro / login / logout / refresh; hash **bcrypt/argon2**; tokens con expiración.
- **Autorización estricta:** cada query filtra por `user_id` del token (policies/middleware). Un padre nunca ve datos de otro.
- **Módulos:** Auth, Users, Children, Devices, Linking, Stats, Rules (V2), Schedules (V2), Commands (V3), Notifications (V3).
- **Seguridad:** prepared statements (Eloquent), rate limiting, validación (Form Requests), HTTPS forzado, logs de eventos.
- **DTO / API Resources:** nunca exponer el modelo crudo.

---

## 9. Base de datos

Diseño normalizado (3NF). El detalle completo (DDL) está en [`mikori-database/schema.sql`](../mikori-database/schema.sql).

Tablas **V1**: `users`, `children`, `devices`, `device_links`, `applications`, `app_usage`, `usage_rules`, `sessions`.

Tablas creadas ahora pero usadas en **V2/V3**: `app_rules`, `schedules`, `device_commands`, `notifications`.

Reglas de cardinalidad:
- `users 1—N children`
- `children 1—N devices`
- `devices 1—N app_usage`
- `children 1—N usage_rules / app_rules / schedules`

---

## 10. Endpoints principales de la API

Base: `https://api.mikori.app/v1` — todos bajo HTTPS. `[P]` = usado por Parent, `[K]` = usado por Kids.

### Auth
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Registro de padre `[P]` |
| POST | `/auth/login` | Login (devuelve access+refresh) `[P]` |
| POST | `/auth/logout` | Cerrar sesión `[P]` |
| POST | `/auth/refresh` | Renovar token `[P][K]` |
| POST | `/auth/password/forgot` | Solicitar recuperación `[P]` |
| POST | `/auth/password/reset` | Restablecer con token `[P]` |

### Hijos (children)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/children` | Listar hijos del padre `[P]` |
| POST | `/children` | Crear perfil de hijo `[P]` |
| GET | `/children/{id}` | Detalle de hijo `[P]` |
| PATCH | `/children/{id}` | Editar (nombre, edad, avatar) `[P]` |
| DELETE | `/children/{id}` | Eliminar hijo `[P]` |

### Vinculación (linking)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/children/{id}/link-code` | Generar código `MIKORI-XXXXXX` con expiración `[P]` |
| POST | `/link/redeem` | El dispositivo del hijo canjea el código `[K]` |
| GET | `/children/{id}/link-status` | Estado de la vinculación `[P]` |

### Dispositivos (devices)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/children/{id}/devices` | Dispositivos del hijo `[P]` |
| POST | `/devices/{id}/heartbeat` | Latido de estado (online/last_seen) `[K]` |
| PATCH | `/devices/{id}/fcm-token` | Registrar/actualizar token FCM `[K]` |

### Estadísticas (stats)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/devices/{id}/usage` | Subida por lotes del uso recolectado `[K]` |
| GET | `/children/{id}/stats/today` | Resumen de hoy (total + top apps) `[P]` |
| GET | `/children/{id}/stats/week` | Datos semanales para gráficos `[P]` |
| GET | `/children/{id}/stats/apps` | Uso por aplicación `[P]` |

### Límites (usage_rules) — V1
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/children/{id}/limits` | Obtener límites diarios `[P][K]` |
| PUT | `/children/{id}/limits` | Definir límites por día (o mismo para todos) `[P]` |

### Preparados para V2/V3 (definidos, sin implementar aún)
`GET/PUT /children/{id}/app-rules` · `GET/PUT /children/{id}/schedules` · `POST /devices/{id}/commands` · `GET /devices/{id}/commands/pending` · `GET /notifications`

**Convenciones:** respuestas JSON `{ data, meta, error }`; errores con código de dominio + HTTP correcto; paginación por cursor en listados grandes (`app_usage`).

---

## 11. Permisos Android necesarios para V1

### MIKORI Kids
| Permiso / Acceso | Para qué | Tipo |
|---|---|---|
| `PACKAGE_USAGE_STATS` (Usage Access) | Leer tiempo de pantalla y uso por app | **Acceso especial** — el usuario lo concede en Ajustes del sistema (`Settings.ACTION_USAGE_ACCESS_SETTINGS`), no es un runtime permission normal |
| `INTERNET` | Subir estadísticas a la API | Normal |
| `ACCESS_NETWORK_STATE` | Detectar red antes de sincronizar | Normal |
| `POST_NOTIFICATIONS` (API 33+) | Notificación del foreground service | Runtime |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` | Servicio de recolección/sync visible | Normal (con tipo) |
| `RECEIVE_BOOT_COMPLETED` | Reprogramar recolección tras reinicio | Normal |
| `QUERY_ALL_PACKAGES` *(evaluar)* | Resolver nombres de apps del catálogo | **Sensible** — Play exige justificación; preferimos `<queries>` específicas si es posible |

### MIKORI Parent
| Permiso | Para qué |
|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Consumir la API |
| `POST_NOTIFICATIONS` (API 33+) | Alertas (base para V3) |

> **Nota clave:** Usage Access NO se puede pedir con el diálogo normal de permisos; se dirige al usuario a la pantalla de Ajustes. El onboarding de Kids debe explicarlo con claridad (transparencia).

---

## 12. APIs de Android que utilizaremos

### V1 (monitoreo)
- **`UsageStatsManager`** — fuente principal de datos de uso.
  - `queryUsageStats(...)` para agregados por rango.
  - `queryEvents(...)` (`MOVE_TO_FOREGROUND` / `MOVE_TO_BACKGROUND`) para calcular sesiones y hora inicio/fin con más precisión.
  - Limitación: los datos se agregan por día y pueden tener granularidad limitada; no da "en vivo" al segundo.
- **`PackageManager`** — resolver etiqueta/ícono/categoría de cada `packageName`.
- **Foreground Service (tipo `dataSync`)** + **WorkManager** — recolección y subida por lotes respetando restricciones de batería (Doze / App Standby).
- **Firebase Cloud Messaging** — registro de token (base para comandos de V3).

### V2/V3 (a decidir antes de implementar) — ver Riesgos
- **`DevicePolicyManager`** (camino C, Device Owner): `setApplicationHidden`, `setPackagesSuspended`, `lockNow`, límites de tiempo. Máximo control, requiere provisioning.
- **`AccessibilityService`** (camino B): detectar app en primer plano y superponer pantalla de bloqueo. Alto escrutinio en Play.
- **`SYSTEM_ALERT_WINDOW`** (camino A): overlay de bloqueo "suave".

> **Regla del proyecto:** no se implementa ninguna función de bloqueo hasta documentar el mecanismo exacto y sus limitaciones.

---

## 13. Riesgos y limitaciones

| # | Riesgo | Impacto | Mitigación |
|---|---|---|---|
| R1 | **Google Play rechaza `AccessibilityService`** usado para control parental | Bloquea V2 por camino B | Preferir camino A (overlay) o C (Device Owner); completar Permissions Declaration Form; documentar uso legítimo |
| R2 | **Device Owner requiere factory reset** | Fricción de instalación alta para padres | Ofrecer flujo guiado (QR/afw#setup) solo para quien quiera control fuerte; camino A como opción sin reset |
| R3 | **Restricciones de batería (Doze/Standby)** cortan la recolección | Datos incompletos | Foreground service + WorkManager; sincronización por lotes; pedir exclusión de optimización solo si es imprescindible |
| R4 | **Usage Access revocable** por el usuario/niño | Deja de haber datos | Detectar pérdida de permiso y alertar al padre (V3); pantalla de estado |
| R5 | **El niño desinstala Kids** | Se pierde el control | Con Device Owner se puede impedir desinstalación; sin él, alertar al padre por "dispositivo desconectado" |
| R6 | **Políticas de Play Families / privacidad de menores** | Rechazo o retiro | Cumplir Families Policy, permisos mínimos, disclosure prominente, sin publicidad personalizada |
| R7 | **`QUERY_ALL_PACKAGES` sensible** | Requiere justificación | Usar `<queries>` específicas; resolver nombres en servidor si es viable |
| R8 | **Fugas de datos entre usuarios** | Grave (menores) | Autorización por `user_id` en cada endpoint; tests de autorización |
| R9 | **Precisión de `UsageStatsManager`** | Estadísticas aproximadas | Comunicar como "aproximado"; combinar `queryEvents` para sesiones |

**Lo que NO haremos (por diseño):** keylogger, robo de contraseñas, lectura secreta de mensajes, grabación oculta de micrófono/cámara, captura secreta de pantalla, evasión de protecciones del SO.

---

## 14. Plan de desarrollo de V1

> Regla: **no** desarrollar V1 + V2 + V3 a la vez. V1 debe estar funcional y probada antes de V2.

### Fase 0 — Fundaciones (este PR)
- [x] Análisis técnico documentado
- [x] Estructura del monorepo
- [x] Esquema inicial de BD

### Fase 1 — Backend V1 (MIKORI API)
1. Setup Laravel + MySQL + `.env` por ambiente
2. Migraciones de tablas V1
3. Auth (registro/login/logout/refresh/recuperación) con tokens
4. CRUD de hijos + autorización por usuario
5. Vinculación por código con expiración
6. Ingesta de estadísticas (`POST /devices/{id}/usage`) + heartbeat
7. Endpoints de stats (hoy/semana/apps) y límites
8. Pruebas de API + de autorización

### Fase 2 — MIKORI Kids V1
1. Proyecto Android + Compose + Hilt + Retrofit
2. Onboarding transparente + solicitud de Usage Access
3. Canje de código de vinculación
4. `UsageCollector` (UsageStatsManager) + Foreground Service + WorkManager
5. Subida por lotes + buffer offline (Room) + heartbeat
6. Pantalla de estado "gestionado por MIKORI"

### Fase 3 — MIKORI Parent V1
1. Proyecto Android + Compose + Hilt + Retrofit + tema/identidad MIKORI
2. Registro/Login/recuperación
3. Crear perfil de hijo + generar código de vinculación
4. Dashboard "Mi familia" (estado, uso de hoy, restante)
5. Estadísticas hoy/semana (gráficos) + top apps
6. Definir límites diarios

### Fase 4 — Integración y pruebas E2E
1. Sincronización real Kids → API → Parent
2. Pruebas de extremo a extremo del flujo completo
3. Ajustes de batería/estabilidad
4. Cierre de V1 ✅ → recién entonces empezar V2

---

*Documento vivo. Última actualización: se versiona junto al código.*
