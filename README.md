# 🌸 MIKORI

### Crece, juega y descubre.

**MIKORI** es una plataforma de control y acompañamiento parental para **Android**, enfocada en ayudar a las familias a construir hábitos saludables de uso del celular de forma **transparente, segura y legítima**.

> Solo Android por ahora. Sin iOS. Sin funciones de vigilancia oculta.

---

## Componentes

| Carpeta | Componente | Descripción | Estado |
|---|---|---|---|
| [`mikori-parent/`](./mikori-parent) | **MIKORI Parent** | App Android para padres | ✅ V1 (compilar en Android Studio) |
| [`mikori-kids/`](./mikori-kids) | **MIKORI Kids** | App Android en el dispositivo del hijo | ✅ V1 (compilar en Android Studio) |
| [`mikori-api/`](./mikori-api) | **MIKORI API** | Backend REST (PHP + SQLite/MySQL) | ✅ V1 (probado, 46 tests) |
| [`mikori-database/`](./mikori-database) | **MIKORI Database** | Esquema y documentación de BD | ✅ |
| [`docs/`](./docs) | **Documentación** | Arquitectura, diseño y guía de pruebas | ✅ |

## Roadmap

- **V1 — Monitoreo:** registro/login, perfiles de hijos, vinculación por código, tiempo de uso (total y por app), estadísticas, límites diarios, historial.
- **V2 — Control:** bloqueo de apps, límites por app, horarios (escolar/nocturno), reglas por día, pausas.
- **V3 — Control remoto:** bloquear/desbloquear ahora, bloqueos programados, alertas, estado del dispositivo, multi-hijo.

> **Regla:** no se avanza a V2 hasta que V1 esté funcional y probada; ni a V3 hasta que V2 lo esté.

## Principios

- **Transparencia total:** el hijo siempre sabe que el dispositivo está gestionado por MIKORI.
- **Solo mecanismos legítimos** de control parental de Android. Sin spyware.
- **Privacidad de menores** por diseño; permisos mínimos y justificados.
- **Aislamiento de datos:** un padre nunca accede a datos de otro usuario.

## Documentación

- 👉 [`docs/01-arquitectura.md`](./docs/01-arquitectura.md) — análisis técnico completo, decisiones, endpoints, permisos, riesgos y plan de V1.
- 🌸 [`docs/02-design-system.md`](./docs/02-design-system.md) — sistema visual "Modern Japanese Kids": paleta, tipografía, logo, mascota Kori, componentes, pantallas y dark mode.
- 🧪 [`docs/03-guia-pruebas.md`](./docs/03-guia-pruebas.md) — **cómo probar V1 paso a paso** (backend + apps) cuando tengas tu equipo.

## Probar V1

Todo está en `main`. Guía completa en [`docs/03-guia-pruebas.md`](./docs/03-guia-pruebas.md). Resumen:

```bash
git clone https://github.com/LORDINFERNO999/APP-MIKORI.git
cd APP-MIKORI/mikori-api && ./serve.sh          # backend en http://localhost:8000 (SQLite, sin config)
# Abre mikori-parent/ y mikori-kids/ en Android Studio y ejecútalas (emulador API 26+ / teléfono real).
```

