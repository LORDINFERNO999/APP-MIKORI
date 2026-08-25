# MIKORI Kids

App Android para el dispositivo del hijo (Kotlin + Jetpack Compose + Material 3),
construida sobre el Design System de MIKORI. **Transparente por diseño**: siempre
indica que el dispositivo está administrado por MIKORI y la familia.

## Cómo abrir y compilar

> Se compila en **Android Studio** (el sandbox no tiene red para Gradle).

1. Abre `mikori-kids/` en Android Studio y sincroniza Gradle.
2. Ejecuta `app` en un emulador (API 26+).
3. Backend debug: `http://10.0.2.2:8000/v1/` — levanta la API con
   `cd ../mikori-api && php -S 127.0.0.1:8000 -t public`.

## Flujo V1
1. **Onboarding transparente**: Kori explica qué hace MIKORI y solicita el
   **Acceso al uso** (Usage Access) — el usuario lo activa en Ajustes de Android.
2. **Vinculación**: el hijo introduce el código `MIKORI-XXXXXX` que generó el padre;
   el servidor entrega un **token de dispositivo** que se guarda localmente.
3. **Home**: saludo con el nombre, tiempo de hoy, restante y barra de progreso.
   Footer "· Administrado por MIKORI y tu familia ·".
4. **Tiempo agotado**: cuando se alcanza el límite, pantalla de descanso (Kori
   durmiendo, tono nocturno), con lenguaje amable — nunca "bloqueado".

## Recolección de uso (mecanismo legítimo)
- Usa **`UsageStatsManager`** (`queryAndAggregateUsageStats`) para medir el tiempo por
  app del día. **No** lee mensajes, contenido, cámara ni micrófono.
- **WorkManager** ejecuta la recolección + latido cada ~15 min, respetando las
  restricciones de batería (Doze/App Standby). `BootReceiver` la reprograma al reiniciar.
- Sube por lotes a `POST /devices/usage` (autenticado con el token de dispositivo).

## Permisos
| Permiso | Para qué |
|---|---|
| `PACKAGE_USAGE_STATS` (Usage Access) | Medir tiempo por app. Acceso especial (Ajustes). |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Subir estadísticas |
| `POST_NOTIFICATIONS` | Avisos (base para V3) |
| `RECEIVE_BOOT_COMPLETED` | Reprogramar recolección tras reiniciar |

## Arquitectura
```
app/src/main/java/com/mikori/kids/
├── core/ui/theme|components   # Design System (tokens + Kori, botones, progreso)
├── core/network               # apiCall (errores), DeviceAuthInterceptor
├── data/remote|local|repository
├── usage/                     # UsageAccess (permiso), UsageStatsCollector
├── work/                      # UsageWorker (@HiltWorker), WorkScheduler, BootReceiver
├── di/                        # Hilt (NetworkModule)
├── ui/                        # onboarding, linking, home + KidsApp/RootViewModel
└── MikoriKidsApp, MainActivity
```

## Anti-spyware (compromiso de diseño)
Sin keylogger, sin lectura de mensajes, sin micrófono/cámara ocultos, sin captura de
pantalla, sin evadir protecciones del sistema. Solo mecanismos oficiales de control
parental de Android, con total transparencia hacia el menor.



---

## V2 (Control) — enforcement

Además del monitoreo (V1), Kids aplica la **política de control** definida por el padre.

- **Mecanismo**: servicio en primer plano (`guard/MikoriGuardService`, tipo `specialUse`)
  que detecta la app en primer plano con `UsageStatsManager` y, cuando corresponde,
  muestra una **pantalla de descanso** como *overlay* (`SYSTEM_ALERT_WINDOW`). No usa
  AccessibilityService ni Device Owner. Ver decisión completa en
  [`../docs/04-v2-control.md`](../docs/04-v2-control.md).
- **Política**: se obtiene de `GET /devices/me/policy`, se **cachea** localmente
  (`PolicyRepository`) y se refresca cada ~30 s, de modo que el bloqueo funciona aun sin red.
- **Precedencia** (ver doc §6): pausa → horario → límite diario → app bloqueada → límite por app.

### Permisos nuevos (V2)
| Permiso | Para qué | Cómo se concede |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | Dibujar la pantalla de descanso | Ajustes ("Mostrar sobre otras apps") — la app lo solicita desde Home |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Servicio de vigilancia persistente | Declarado en manifest (Play exige justificación) |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Evitar que el sistema mate el servicio | Opcional, recomendado |

### Limitaciones (honestas)
Es un bloqueo **"suave"**: efectivo para niños, pero un adolescente con conocimientos
podría sortearlo. Para bloqueo infranqueable haría falta el "Modo reforzado" (Device
Owner), documentado como opción futura. MIKORI no oculta la app ni evade protecciones.
