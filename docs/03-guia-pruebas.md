# 🧪 MIKORI — Guía de pruebas de V1 (paso a paso)

Todo el proyecto está en `main`. Cuando tengas tu equipo, sigue esta guía para
probar el flujo completo **Kids → API → Parent** de extremo a extremo.

> No necesitas MySQL para probar: el backend corre con **SQLite** sin configuración.

---

## 0. Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| **PHP** | 8.2+ (con `pdo_sqlite`, `mbstring`, `openssl`) | Backend MIKORI API |
| **Android Studio** | Ladybug/última estable | Compilar MIKORI Parent y Kids |
| **JDK** | 17 (viene con Android Studio) | Build Android |
| Git | cualquiera | Clonar el repo |
| (opcional) Un teléfono Android real | API 26+ | Probar Kids con uso real de apps |

---

## 1. Clonar

```bash
git clone https://github.com/LORDINFERNO999/APP-MIKORI.git
cd APP-MIKORI
```

---

## 2. Levantar el backend (MIKORI API)

```bash
cd mikori-api
cp .env.example .env          # DB_DRIVER=sqlite y APP_DEBUG=true por defecto
php database/migrate.php      # crea las tablas
./serve.sh                    # o: php -S 0.0.0.0:8000 -t public
```

- Comprueba que responde: abre `http://localhost:8000/v1/health` → debe dar
  `{"data":{"service":"MIKORI API","status":"ok", ...}}`.
- (Opcional) Ejecuta la batería de pruebas del backend: `php tests/smoke.php` → **46 OK**.

> Usé `0.0.0.0` para que un teléfono real en tu red también pueda alcanzarlo.

---

## 3. Configurar la URL del backend según el dispositivo

El valor por defecto (debug) es `http://10.0.2.2:8000/v1/`, que es "localhost del PC"
**visto desde el emulador de Android**. Ajusta según cómo pruebes:

| Escenario | Qué hacer |
|---|---|
| **Emulador** (Parent y/o Kids) | Nada: `10.0.2.2` ya apunta a tu PC. |
| **Teléfono real** en la misma Wi-Fi | En `app/build.gradle.kts` del proyecto, cambia el `API_BASE_URL` de `debug` a `http://TU_IP_LAN:8000/v1/` (p. ej. `http://192.168.1.50:8000/v1/`). Averigua tu IP con `ipconfig`/`ifconfig`. |

> Kids conviene probarla en un **teléfono real**, porque `UsageStatsManager` necesita
> apps reales en uso para generar datos (el emulador casi no registra uso).

---

## 4. Compilar y abrir las apps en Android Studio

Para **cada** app (`mikori-parent/` y `mikori-kids/`):

1. `File > Open…` y elige la carpeta del proyecto.
2. Espera la sincronización de Gradle. Si sugiere actualizar AGP/Gradle o versiones
   del `gradle/libs.versions.toml`, **acepta** el asistente.
3. (Android Studio genera el Gradle wrapper y descarga dependencias — necesita internet.)
4. Selecciona la config `app` y pulsa Run ▶ en un emulador API 26+ o tu teléfono.

> **Fuentes de marca (opcional, 1 min):** para activar M PLUS Rounded 1c + Nunito,
> `File > New > Font` y añádelas como *Downloadable Fonts*; luego cambia
> `DisplayFamily`/`BodyFamily` en `core/ui/theme/Type.kt`. Sin este paso, la app se ve
> igual de cuidada con la fuente del sistema.

---

## 5. Flujo E2E (el momento de la verdad)

1. **Parent — crear cuenta**: abre MIKORI Parent → *Crear una cuenta* → registra un
   correo y contraseña (mín. 8). Entrarás al dashboard "Mi familia".
2. **Parent — añadir hijo**: pulsa *Añadir hijo* → nombre (p. ej. "Mateo") → Guardar.
3. **Parent — generar código**: entra al hijo → *Vincular dispositivo* → verás
   `MIKORI-XXXXXX` con cuenta atrás.
4. **Kids — vincular**: abre MIKORI Kids en el otro dispositivo/emulador →
   *Continuar* en el onboarding → escribe el código → *Conectar*.
5. **Kids — permiso**: activa el **Acceso al uso** cuando la app lo pida (te lleva a
   Ajustes de Android → busca "MIKORI Kids" → actívalo → vuelve).
6. **Usar apps** un rato en el dispositivo del hijo (o el emulador) para generar tiempo.
7. **Ver el dato fluir**: en unos minutos (o reabriendo la app), Kids sube el uso;
   en **Parent** verás el tiempo de hoy, el top de apps y —si pusiste un límite— el
   tiempo restante. En el hijo, prueba también poner un límite bajo desde Parent
   (*Límites*) para ver la pantalla de **"tiempo agotado"** en Kids.

---

## 6. Comprobaciones rápidas de que V1 funciona

- [ ] `/v1/health` responde OK.
- [ ] Registro + login en Parent.
- [ ] Crear hijo y generar código.
- [ ] Kids canjea el código (mensaje de éxito) y pide Usage Access.
- [ ] Tras usar apps, el uso aparece en Parent (hoy / semana / top apps).
- [ ] Definir límite en Parent → Kids muestra restante y, al agotarse, "hora de descansar".

---

## 7. Problemas comunes

| Síntoma | Causa / solución |
|---|---|
| Las apps no compilan la 1ª vez | Deja que Android Studio actualice AGP/Gradle (acepta sugerencias). Necesita internet. |
| Kids/Parent no conecta al backend | Revisa el `API_BASE_URL` (emulador `10.0.2.2`, teléfono real → IP LAN). El backend debe correr con `0.0.0.0:8000` y el firewall permitir el puerto 8000. |
| Kids no registra uso | Falta activar **Acceso al uso** en Ajustes, o estás en emulador (usa un teléfono real). |
| "network_error" en las apps | El backend no está levantado o la URL/IP es incorrecta. |
| Quiero usar MySQL | Cambia `.env` a `DB_DRIVER=mysql` con tus credenciales e importa `mikori-database/schema.sql`. |

---

## 8. Qué NO hace todavía (es lo correcto para V1)

V1 es **monitoreo**: registra y muestra el uso, y calcula el límite/tiempo restante.
El **bloqueo real** de apps y los **horarios** llegan en **V2** (ver el análisis de
mecanismos Android en [`01-arquitectura.md`](./01-arquitectura.md) §11–§13). No se
avanza a V2 hasta validar V1.

---

*Cualquier duda al probar, anótala y la resolvemos en la siguiente sesión.* 🌱
