# MIKORI Parent

App Android para padres (Kotlin + Jetpack Compose + Material 3), construida sobre el
Design System de MIKORI ("Modern Japanese Kids").

Rol: **lectura y gestión**. Consume la MIKORI API V1.

## Cómo abrir y compilar

> Este proyecto se compila en **Android Studio** (última versión estable). El sandbox de
> desarrollo no tiene red, así que Gradle descarga las dependencias **en tu equipo**.

1. Abre la carpeta `mikori-parent/` en Android Studio.
2. Deja que sincronice Gradle. Si sugiere actualizar AGP/Gradle/versiones del
   `gradle/libs.versions.toml`, acepta el asistente.
3. Ejecuta la configuración `app` en un emulador (API 26+).

### Conexión con el backend
- **Debug** apunta a `http://10.0.2.2:8000/v1/` (localhost del PC visto desde el emulador).
  Levanta la API: `cd ../mikori-api && php -S 127.0.0.1:8000 -t public`.
- **Release** apunta a `https://api.mikori.app/v1/` (ajústalo en `app/build.gradle.kts`).

### Fuentes de marca (paso opcional, 1 min)
La tipografía usa por defecto la del sistema para compilar sin binarios. Para activar
**M PLUS Rounded 1c** + **Nunito** (Design System §4): en Android Studio,
`File > New > Font` y añade ambas como *Downloadable Fonts* (genera los certificados
automáticamente); luego ajusta `DisplayFamily`/`BodyFamily` en `core/ui/theme/Type.kt`.

## Arquitectura (MVVM + Repository)
```
app/src/main/java/com/mikori/parent/
├── core/
│   ├── ui/theme/        # Design System: Color, Type, Shape, Dimens, MikoriTheme
│   ├── ui/components/   # Botones, cards, ChildCard, TimeProgressBar, Kori, charts…
│   ├── network/         # apiCall (errores), AuthInterceptor
│   └── util/            # TimeFormat
├── data/
│   ├── remote/          # MikoriApiService (Retrofit) + DTOs
│   ├── local/           # SessionStore (DataStore)
│   └── repository/      # Auth / Family / Stats / Limits
├── di/                  # Hilt (NetworkModule)
├── feature/             # auth, dashboard, child, stats, limits, linking, settings
│   └── <feature>/       # <Feature>ViewModel + <Feature>Screen
└── navigation/          # MikoriApp, Routes, RootViewModel
```

## Funciones V1
- Registro / Login / Logout / Recuperación de contraseña.
- Dashboard "Mi familia" con tarjetas de cada hijo (estado, tiempo de hoy, restante).
- Crear hijo · Detalle con top de apps · Eliminar.
- Estadísticas: gráfico semanal + apps más usadas.
- Límites diarios (mismo para todos o por día).
- Vinculación por código `MIKORI-XXXXXX` con estado en vivo.

## Notas
- Sin Dynamic Color a propósito: la identidad matcha/sakura es constante.
- Tokens de color/forma/tipografía = fuente única en `core/ui/theme/`, alineados con
  [`../docs/02-design-system.md`](../docs/02-design-system.md).
- Los tokens de versión de `libs.versions.toml` son recientes (ago-2026) pero pueden
  requerir ajuste según tu Android Studio.
