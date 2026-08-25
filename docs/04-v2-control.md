# 🌸 MIKORI — V2 (Control): decisión técnica y diseño

> Regla del proyecto: **antes de implementar bloqueo se decide y documenta el
> mecanismo Android exacto y sus limitaciones.** Este documento es ese paso.

V2 añade **control** sobre el monitoreo de V1:
- Límite individual por aplicación.
- Bloqueo (marcar apps como restringidas).
- Horarios (escolar / nocturno / personalizado) con reglas por día.
- Pausas temporales (30 min / 1 h / 2 h / hasta cierta hora).

---

## 1. Las 3 vías de enforcement en Android (2026)

| Vía | Cómo bloquea | Requisitos | Fuerza | Riesgo Play / UX |
|---|---|---|---|---|
| **A. Overlay + detección de foreground** | Un servicio en primer plano detecta la app en uso (`UsageStatsManager`) y, si está restringida, dibuja una **ventana overlay** de "descanso" encima (`SYSTEM_ALERT_WINDOW`). | Usage Access (ya en V1) + `SYSTEM_ALERT_WINDOW` + foreground service + (ideal) exención de batería. | **Media** (bloqueo "suave"). | ✅ Viable con divulgación de control parental. UX: pide 2 permisos extra. |
| **B. AccessibilityService** | Escucha eventos de accesibilidad para detectar/tapar apps. | Activar servicio de accesibilidad. | Alta. | ⚠️ **Alto riesgo de rechazo** en Google Play para control parental; Google desaconseja usos no-accesibilidad. |
| **C. Device Owner (DevicePolicyManager)** | `setPackagesSuspended` / `setApplicationHidden` / `lockNow` a nivel de SO. | **Provisioning en dispositivo reseteado** (QR/`afw#setup`/ADB); un solo perfil. | **Muy alta** (bloqueo real, impide desinstalar). | ✅ Legítimo, pero **fricción de instalación altísima** para un consumidor. |

### Decisión para V2 → **Vía A (Overlay + UsageStats)**

Motivos:
- **Instalación normal desde Play** (sin factory reset), coherente con un producto de consumo.
- Reutiliza el **Usage Access** que Kids ya pide en V1.
- Evita el **alto riesgo de rechazo** de AccessibilityService (vía B).
- Es transparente y explicable al menor (encaja con "less control, more guidance").

**Vía C (Device Owner)** queda documentada como **"Modo reforzado" opcional a futuro**
(post-V3) para familias que acepten resetear el dispositivo del hijo y quieran bloqueo
infranqueable. No se implementa en V2.

**Vía B (Accessibility) se descarta** por política.

> Fuentes (ago-2026): las apps con `SYSTEM_ALERT_WINDOW` solo pueden iniciar un
> foreground service desde segundo plano si tienen una ventana overlay visible u otra
> exención ([Changes to foreground services](https://developer.android.com/develop/background-work/services/fgs/changes));
> el inicio de actividades en segundo plano (BAL) está restringido y `SYSTEM_ALERT_WINDOW`
> es una de las exenciones ([Background Activity Launch](https://developer.android.com/guide/components/activities/secure-bal)).
> *Contenido reformulado para cumplir restricciones de licencia.*

---

## 2. Mecanismo exacto (Vía A) y por qué así

1. **Servicio en primer plano persistente** (`MikoriGuardService`, tipo
   `specialUse`). Se inicia cuando la app está en primer plano (tras vincular/onboarding)
   y muestra una notificación permanente **"MIKORI está cuidando este dispositivo"**
   (transparencia + evita que el sistema lo mate).
2. **Detección de app en foreground**: el servicio consulta periódicamente
   `UsageStatsManager.queryEvents(...)` (eventos `ACTIVITY_RESUMED`) cada 1–2 s para
   saber qué app está al frente. No usa Accessibility.
3. **Aplicación de la política**: si la app al frente está restringida (por bloqueo,
   límite por app agotado, horario activo, pausa o límite diario alcanzado), el servicio
   **dibuja una ventana overlay** a pantalla completa (`WindowManager.addView`, tipo
   `TYPE_APPLICATION_OVERLAY`) con la pantalla amable de **"descanso"** de MIKORI.
   Se dibuja una *ventana* (no se lanza una Activity), evitando las restricciones de BAL.
4. **Política sincronizada**: el servicio obtiene la política del backend
   (`GET /devices/me/policy`), la cachea localmente (para funcionar sin red) y la refresca
   periódicamente y vía WorkManager. Así el bloqueo funciona aunque haya cortes de red.

### Permisos nuevos en Kids (V2)
| Permiso | Para qué | Cómo se concede |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | Dibujar la pantalla de descanso sobre la app | El usuario lo activa en Ajustes (`ACTION_MANAGE_OVERLAY_PERMISSION`). |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | Servicio de vigilancia persistente | Declarado en manifest; Play pide justificación del `specialUse`. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` *(opcional)* | Que el sistema no mate el servicio | El usuario acepta la exención (opcional pero recomendado). |

`PACKAGE_USAGE_STATS` ya se pide en V1.

---

## 3. Limitaciones (honestas)

- **Bloqueo "suave"**: un adolescente con conocimientos podría sortearlo (forzar cierre,
  quitar permisos, desinstalar). Para bloqueo infranqueable haría falta el Modo reforzado
  (Device Owner). MIKORI **alertará al padre** (V3) si detecta pérdida de permisos.
- **Latencia**: el overlay aparece ~1–2 s tras abrir la app (tiempo de sondeo).
- **Batería**: un servicio persistente consume; se mitiga con sondeo espaciado y se
  recomienda (no obliga) la exención de optimización.
- **Play**: el uso de `SYSTEM_ALERT_WINDOW` + FGS `specialUse` debe declararse como
  control parental con divulgación destacada. No usar para nada más.
- **Nunca** se ocultará la app ni se evadirán protecciones del sistema.

---

## 4. Modelo de datos V2 (además de V1)

Ya previstos en `mikori-database/schema.sql`; V2 los activa en la API:

- **`app_rules`** (`child_id`, `application_id`, `max_minutes` NULL, `is_blocked`, `active`):
  límite por app y/o bloqueo por app.
- **`schedules`** (`child_id`, `name`, `type` = school|night|custom, `start_time`,
  `end_time`, `days_mask` bitmask L–D, `active`): franjas horarias de bloqueo. Soporta
  cruce de medianoche (p. ej. nocturno 21:30–07:00).
- **`pauses`** (nueva) (`child_id`, `until_at`, `active`, `created_at`): pausa temporal
  = bloqueo total hasta `until_at`.

`days_mask`: bit0=Lunes … bit6=Domingo (127 = todos los días).

---

## 5. API V2

### Parent (user-auth)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/children/{id}/apps` | Catálogo de apps observadas del hijo (para elegir cuáles limitar/bloquear) |
| GET | `/children/{id}/app-rules` | Reglas por app actuales |
| PUT | `/children/{id}/app-rules` | Definir límite/bloqueo por app |
| GET | `/children/{id}/schedules` | Listar horarios |
| POST | `/children/{id}/schedules` | Crear horario |
| PUT | `/children/{id}/schedules/{sid}` | Editar horario |
| DELETE | `/children/{id}/schedules/{sid}` | Eliminar horario |
| POST | `/children/{id}/pause` | Iniciar pausa (minutos o hasta hora) |
| DELETE | `/children/{id}/pause` | Cancelar pausa |

### Kids (device-auth)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/devices/me/policy` | **Política de enforcement** que Kids aplica localmente |

**Respuesta de `/devices/me/policy`** (ejemplo):
```json
{
  "daily_limit_reached": false,
  "remaining_seconds": 1680,
  "blocked_packages": ["com.zhiliaoapp.musically"],
  "app_limits": [
    { "package": "com.google.android.youtube", "max_minutes": 60, "used_seconds": 2520, "exceeded": false }
  ],
  "active_schedule": { "name": "Horario nocturno", "type": "night" },
  "pause_until": null,
  "block_all": false
}
```

---

## 6. Precedencia de enforcement en Kids

El servicio bloquea (muestra "descanso") si se cumple **cualquiera** de estas, en orden:

1. **Pausa activa** (`pause_until` en el futuro) → bloquear todo.
2. **Horario activo** (hora actual dentro de una franja + día en `days_mask`) → bloquear todo.
3. **Límite diario total alcanzado** (`daily_limit_reached`) → bloquear todo.
4. **App bloqueada** (`blocked_packages` contiene la app al frente) → bloquear esa app.
5. **Límite por app agotado** (`app_limits[x].exceeded`) → bloquear esa app.

Si nada aplica, no se muestra overlay.

---

## 7. Plan de implementación V2

1. ✅ Este documento (decisión + diseño).
2. **Backend V2**: migraciones (`app_rules`, `schedules`, `pauses`) + endpoints Parent + `GET /devices/me/policy` + tests.
3. **Parent V2**: pantallas de límites por app, bloqueo, horarios y pausas.
4. **Kids V2**: `MikoriGuardService` (FGS) + detección de foreground + overlay de descanso + permisos (overlay, batería) + caché de política.
5. Pruebas E2E y cierre de V2. Solo después → V3.

---

*Documento vivo. Fuente de verdad de las decisiones de control de MIKORI.*
