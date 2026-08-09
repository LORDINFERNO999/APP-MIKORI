# MIKORI Kids

App Android instalada en el dispositivo del hijo (Kotlin + Jetpack Compose + Material 3).

Rol: **recolección transparente** de estadísticas de uso mediante APIs oficiales de Android. Indica siempre que el dispositivo está gestionado por MIKORI y el tutor.

## Stack
- Kotlin, Jetpack Compose, Material 3
- minSdk 26 / targetSdk 36
- MVVM + Repository, Hilt, Retrofit/OkHttp, Room (buffer offline), WorkManager, Foreground Service, FCM

## Funciones V1
- Onboarding transparente + solicitud guiada de **Usage Access**
- Canje de código de vinculación (`MIKORI-XXXXXX`)
- Recolección de uso con `UsageStatsManager`
- Subida por lotes + heartbeat de estado
- Pantalla "Gestionado por MIKORI"

## Anti-spyware por diseño
Sin keylogger, sin micrófono/cámara ocultos, sin lectura de mensajes, sin captura secreta de pantalla, sin evadir protecciones del sistema. Solo mecanismos legítimos de control parental.

> Permisos y APIs detallados en [`docs/01-arquitectura.md`](../docs/01-arquitectura.md) §11–§12.

_Pendiente de scaffolding del proyecto Android (Fase 2 del plan V1)._
