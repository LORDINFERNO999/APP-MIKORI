# MIKORI Parent

App Android para padres (Kotlin + Jetpack Compose + Material 3).

Rol: **lectura y gestión**. No requiere permisos especiales de Android (solo notificaciones).

## Stack
- Kotlin, Jetpack Compose, Material 3
- minSdk 26 / targetSdk 36
- MVVM + Repository, Hilt, Retrofit/OkHttp, Coroutines/Flow, DataStore, Navigation Compose, FCM

## Pantallas V1
- Registro / Login / Recuperación de contraseña
- Dashboard "Mi familia" (estado, uso de hoy, tiempo restante)
- Crear perfil de hijo + generar código de vinculación
- Estadísticas (hoy / semana) + apps más usadas
- Límites diarios

> Estructura detallada en [`docs/01-arquitectura.md`](../docs/01-arquitectura.md) §6.

_Pendiente de scaffolding del proyecto Android (Fase 3 del plan V1)._
