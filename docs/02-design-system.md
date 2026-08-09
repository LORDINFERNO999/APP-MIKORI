# 🌸 MIKORI — Design System

> **Crece, juega y descubre.**
>
> Sistema visual de MIKORI. Estilo propio: **"Modern Japanese Kids"**.
> Este documento define la identidad y los tokens antes de programar la UI.
> Principio rector de toda decisión: **"Less control, more guidance."**

---

## Índice
1. [Principios de diseño](#1-principios-de-diseño)
2. [Estilo "Modern Japanese Kids"](#2-estilo-modern-japanese-kids)
3. [Paleta de color (light + dark)](#3-paleta-de-color)
4. [Tipografía](#4-tipografía)
5. [Logo (concepto)](#5-logo-concepto)
6. [Mascota: "Kori" (concepto)](#6-mascota-kori-concepto)
7. [Iconografía](#7-iconografía)
8. [Forma, espaciado y elevación](#8-forma-espaciado-y-elevación)
9. [Animaciones](#9-animaciones)
10. [Sistema de componentes](#10-sistema-de-componentes)
11. [Navegación](#11-navegación)
12. [Pantallas — MIKORI Parent](#12-pantallas--mikori-parent)
13. [Pantallas — MIKORI Kids](#13-pantallas--mikori-kids)
14. [Dark Mode](#14-dark-mode)
15. [Ilustraciones](#15-ilustraciones)
16. [Accesibilidad](#16-accesibilidad)

---

## 1. Principios de diseño

MIKORI acompaña, no vigila. Cada pantalla responde a dos preguntas:

> ¿Esto ayuda al padre a **entender** mejor el uso del dispositivo?
> ¿Esto hace que el niño se sienta **acompañado** y no vigilado?

Personalidad que debe transmitir la interfaz:

🌱 Crecimiento · 🌸 Cuidado · ☀️ Alegría · 🧸 Infancia · ✨ Descubrimiento · 🏡 Familia · 🛡️ Seguridad · 📱 Tecnología

Reglas de oro:
- **Calma sobre alarma.** Nada de rojos agresivos ni lenguaje punitivo.
- **Espacio para respirar.** Mucho aire, pocas cosas por pantalla.
- **Lenguaje positivo.** "Tu tiempo de hoy terminó" en vez de "Has sido bloqueado".
- **Consistencia.** Mismo lenguaje visual en Parent y Kids; Parent más sobrio, Kids más cálido.

---

## 2. Estilo "Modern Japanese Kids"

Combinación de: minimalismo japonés + diseño infantil moderno + Material 3.

La inspiración japonesa se expresa en **actitud, no en clichés**:
- ✅ Minimalismo, equilibrio (*ma*, el espacio negativo), naturaleza, formas suaves, calma, detalles sutiles.
- ❌ NADA de samuráis, geishas, templos, kanji decorativo, dragones ni estética anime.

Se siente como una **startup japonesa moderna** de bienestar familiar (piensa en la calma de una app de meditación, con la calidez de una marca infantil premium).

---

## 3. Paleta de color

Inspiración: naturaleza japonesa. Tonos suaves, poco saturados, elegantes. El color principal — **verde matcha** — transmite tranquilidad.

### 3.1 Colores base de marca (referencia)

| Nombre | Hex | Uso |
|---|---|---|
| 🍵 Matcha | `#4F7A5B` | Primary — calma, crecimiento |
| 🍵 Matcha claro | `#CDE7CE` | Fondos suaves, contenedores primary |
| 🌸 Sakura | `#B06E7E` | Secondary — cuidado, cercanía |
| 🌸 Sakura claro | `#F6DCE2` | Contenedores secondary |
| ☁️ Cielo | `#6B84B0` | Tertiary — tecnología, confianza |
| 💜 Lavanda | `#8E88C4` | Acento nocturno (dark mode) |
| 🍶 Crema/washi | `#FBF7F0` | Background (light) |
| 🌾 Beige cálido | `#EFE7DA` | Surface variant |
| 🌱 Éxito | `#3F8A5B` | Success |
| 🍁 Terracota | `#C8863E` | Warning (suave, no alarmante) |
| 🫖 Gris cálido | `#6E6A61` | Texto secundario |

### 3.2 Tokens Material 3 — Light

```
primary               #4F7A5B    onPrimary              #FFFFFF
primaryContainer      #CDE7CE    onPrimaryContainer     #0C2914
secondary             #B06E7E    onSecondary            #FFFFFF
secondaryContainer    #F6DCE2    onSecondaryContainer   #3A1721
tertiary              #6B84B0    onTertiary             #FFFFFF
tertiaryContainer     #DCE4F6    onTertiaryContainer    #131F33
background            #FBF7F0    onBackground           #2B2A27
surface               #FFFDF9    onSurface              #2B2A27
surfaceVariant        #EFE7DA    onSurfaceVariant       #4E4A42
outline               #C9BFAE    outlineVariant         #E5DCCE
error                 #B3261E    onError                #FFFFFF
errorContainer        #F9DEDC    onErrorContainer       #410E0B
```
Roles semánticos extra (no estándar en M3, definidos por MIKORI):
```
success               #3F8A5B    onSuccess              #FFFFFF   successContainer #CDEBD5
warning               #C8863E    onWarning              #FFFFFF   warningContainer #F6E2C7
textPrimary           #2B2A27    textSecondary          #6E6A61
```

### 3.3 Tokens Material 3 — Dark (noche japonesa + lavanda)

```
primary               #A6CBA0    onPrimary              #10321A
primaryContainer      #35563B    onPrimaryContainer     #C3E7BE
secondary             #E6B4C0    onSecondary            #43222C
secondaryContainer    #5C3A45    onSecondaryContainer   #F6DCE2
tertiary              #B7C2E8    onTertiary             #212C45
tertiaryContainer     #3A466A    onTertiaryContainer    #DCE4F6
background            #14141A    onBackground           #E7E3DA
surface               #1C1C24    onSurface              #E7E3DA
surfaceVariant        #2A2A33    onSurfaceVariant       #C9C4BC
outline               #47444E    outlineVariant         #33313A
error                 #F2B8B5    onError                #601410
success               #8FCFA0    onSuccess              #0C3018   successContainer #35563B
warning               #E6B673    onWarning              #3A2609   warningContainer #4A3517
```

### 3.4 Color.kt (Compose-ready)

```kotlin
// ui/theme/Color.kt
package com.mikori.core.ui.theme

import androidx.compose.ui.graphics.Color

// Marca
val Matcha        = Color(0xFF4F7A5B)
val MatchaLight   = Color(0xFFCDE7CE)
val Sakura        = Color(0xFFB06E7E)
val SakuraLight   = Color(0xFFF6DCE2)
val Sky           = Color(0xFF6B84B0)
val Lavender      = Color(0xFF8E88C4)
val Washi         = Color(0xFFFBF7F0)
val BeigeWarm     = Color(0xFFEFE7DA)

// Light
val md_light_primary            = Color(0xFF4F7A5B)
val md_light_onPrimary          = Color(0xFFFFFFFF)
val md_light_primaryContainer   = Color(0xFFCDE7CE)
val md_light_onPrimaryContainer = Color(0xFF0C2914)
val md_light_secondary          = Color(0xFFB06E7E)
val md_light_tertiary           = Color(0xFF6B84B0)
val md_light_background         = Color(0xFFFBF7F0)
val md_light_surface            = Color(0xFFFFFDF9)
val md_light_surfaceVariant     = Color(0xFFEFE7DA)
val md_light_onSurface          = Color(0xFF2B2A27)
val md_light_onSurfaceVariant   = Color(0xFF4E4A42)
val md_light_outline            = Color(0xFFC9BFAE)
val md_light_error              = Color(0xFFB3261E)

// Dark
val md_dark_primary             = Color(0xFFA6CBA0)
val md_dark_onPrimary           = Color(0xFF10321A)
val md_dark_primaryContainer    = Color(0xFF35563B)
val md_dark_secondary           = Color(0xFFE6B4C0)
val md_dark_tertiary            = Color(0xFFB7C2E8)
val md_dark_background          = Color(0xFF14141A)
val md_dark_surface             = Color(0xFF1C1C24)
val md_dark_surfaceVariant      = Color(0xFF2A2A33)
val md_dark_onSurface           = Color(0xFFE7E3DA)
val md_dark_outline             = Color(0xFF47444E)
val md_dark_error               = Color(0xFFF2B8B5)
```

---

## 4. Tipografía

Prioridad absoluta: **legibilidad**. Tipos redondeados, modernos y amigables.

| Rol | Fuente | Notas |
|---|---|---|
| **Marca / Display** | **M PLUS Rounded 1c** | Tipografía japonesa redondeada (encaja con la identidad). Para el wordmark MIKORI y números grandes de tiempo. |
| **UI / Cuerpo** | **Nunito** | Redondeada, altísima legibilidad, muchos pesos. Todo el texto de interfaz. |

Ambas son gratuitas (Google Fonts) y empaquetables como recursos en Compose (`res/font`). Alternativa a M PLUS Rounded 1c: **Baloo 2**.

### Escala tipográfica (Material 3)

| Rol M3 | Fuente | Tamaño / Line height | Peso |
|---|---|---|---|
| displayLarge | M PLUS Rounded 1c | 40 / 48 | 700 |
| headlineMedium | M PLUS Rounded 1c | 28 / 34 | 700 |
| titleLarge | Nunito | 22 / 28 | 700 |
| titleMedium | Nunito | 17 / 24 | 600 |
| bodyLarge | Nunito | 16 / 24 | 400 |
| bodyMedium | Nunito | 14 / 20 | 400 |
| labelLarge (botones) | Nunito | 15 / 20 | 700 |
| labelSmall | Nunito | 12 / 16 | 600 |

El **wordmark "mikori"** se escribe en minúsculas, M PLUS Rounded 1c 700, con tracking ligeramente reducido; la flor 🌸 o un pequeño brote puede sustituir el punto/acento. El eslogan va en Nunito 400, `textSecondary`.

---

## 5. Logo (concepto)

Objetivo: simple, memorable, infantil-pero-no-demasiado, reconocible como ícono de app.

**Composición:**
- **Símbolo (app icon):** la carita de la mascota **Kori** con un pequeño **brote/hoja** sobre la cabeza, dentro de un **squircle** (cuadrado superredondeado estilo iOS/Material) con fondo **matcha** o degradado matcha→crema muy sutil.
- **Wordmark:** `mikori` en minúsculas, redondeado. Variante donde la **"o"** es una carita/brote de Kori.
- **Lockups:** (a) símbolo + wordmark horizontal; (b) símbolo solo (ícono); (c) wordmark solo.

**Reglas:**
- Zona de seguridad = altura de la "o" alrededor del logo.
- Tamaño mínimo del wordmark: 96 px de ancho.
- No rotar, no aplicar sombras duras, no cambiar los colores de marca.
- Ícono adaptativo Android: foreground = Kori + brote; background = matcha liso.

```
  App icon (squircle)        Lockup horizontal
 ┌───────────────┐
 │    ╭─────╮    │            ╭──╮
 │   (  ^ ^  )🌱 │           ( ^ ^)🌱  mikori
 │    ╰─────╯    │            ╰──╯      crece, juega y descubre
 │   matcha bg   │
 └───────────────┘
```

---

## 6. Mascota: "Kori" (concepto)

**Kori** es una criatura de bosque **original** (no copia de personajes existentes). Nace del propio nombre mi·**KORI**.

**Rasgos:**
- Cuerpo pequeño, redondo, tipo "gota suave"; color crema/washi (`#FBF7F0`) con mejillas sakura muy sutiles.
- Orejas redondeadas (híbrido conejo/osito).
- Un **brote de hoja** matcha sobre la cabeza → símbolo de *crecer*. El brote puede crecer/animarse en momentos positivos.
- Ojos: dos puntos simples; boca mínima. Cero detalles innecesarios → fácil de animar.
- Estilo de línea: sin contorno duro; formas rellenas con sombra muy suave.

**Estados/expresiones (para animación):**
| Estado | Expresión | Dónde aparece |
|---|---|---|
| Saludo | ojos felices, brote erguido | Onboarding, Kids home |
| Éxito | ojos ↑ + brillo ✨, saltito | Vinculación exitosa, logros |
| Descanso | ojos cerrados, luna 🌙, brote inclinado | Pantalla de tiempo agotado |
| Pausa | dedo/hoja en la boca "shh" tranquilo | App en descanso (restringida) |
| Vacío | mirando alrededor, curioso | Estados vacíos |
| Consejo | inclina la cabeza, burbuja 💬 | Tips |

**Regla de uso:** Kori NO aparece en todas las pantallas. Se reserva para momentos emocionales (primera vez, éxito, límite, vacío, consejo). En Parent aparece con moderación (más sobrio); en Kids es más protagonista.

---

## 7. Iconografía

- Estilo: **redondeado, lineal, trazo uniforme** (~2 dp), esquinas redondas, sin relleno pesado.
- Set base: Material Symbols **Rounded** (peso 400, opsz 24) como punto de partida, ajustando a la personalidad MIKORI.
- Iconos propios para conceptos MIKORI: brote 🌱, luna de descanso 🌙, flor 🌸, escudo suave 🛡️, reloj de arena amable.
- Categorías de apps con ícono + color suave (video, social, juegos, navegador, educación).
- Evitar iconos corporativos/afilados o metáforas de vigilancia (ojos, cámaras, candados agresivos). El "bloqueo" se representa como **descanso** (luna/hoja), no como candado rojo.

---

## 8. Forma, espaciado y elevación

**Grid base: 4 dp.** Espaciados: `4, 8, 12, 16, 20, 24, 32, 40`.
Márgenes de pantalla: **20 dp** laterales. Separación entre tarjetas: **16 dp**.

**Radios (Shapes M3):**
```
extraSmall  8 dp     (chips, campos pequeños)
small      12 dp
medium     20 dp     (tarjetas)  ← forma insignia de MIKORI
large      28 dp     (hojas/sheets, diálogos)
extraLarge 36 dp     (contenedores hero, tarjeta de hijo)
full       100%      (botones tipo "pill", avatares)
```

**Elevación:** sombras **muy sutiles**. Preferir superficies con `surface`/`surfaceVariant` y bordes tenues (`outlineVariant`) sobre sombras marcadas.
```
nivel 0  sin sombra (fondo)
nivel 1  y=1, blur=3, negro 6%   (tarjetas en reposo)
nivel 2  y=2, blur=8, negro 8%   (elementos activos / FAB)
```

**Shape.kt (Compose):**
```kotlin
val MikoriShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(20.dp),
    large      = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)
```

---

## 9. Animaciones

Microanimaciones **suaves y rápidas**; la app debe sentirse ágil. Nada excesivo.

| Momento | Animación | Duración / curva |
|---|---|---|
| Aparición de tarjetas | fade + slide-up 8 dp, en cascada (stagger 40 ms) | 220 ms · EaseOutCubic |
| Barra de progreso de tiempo | llenado animado | 600 ms · EaseInOut |
| Cambio de estado (online/offline) | crossfade del punto + pulso suave | 300 ms |
| Sincronización | brote girando lento / puntos | loop sutil |
| Vinculación exitosa | Kori saltito + ✨ + check | 500 ms, una vez |
| Mascota idle | respiración (escala 1.0↔1.02) | 3 s loop |
| Pull-to-refresh | brote que crece | proporcional al gesto |

Respetar "reducir movimiento" del sistema (accesibilidad): degradar a fades simples.

---

## 10. Sistema de componentes

Todos comparten la identidad MIKORI (radios `medium`, sombras sutiles, tipografía Nunito).

| Componente | Especificación |
|---|---|
| **Button (primary)** | pill (`full`), matcha, `onPrimary` blanco, label 15/700, alto 52 dp, ripple suave. |
| **Button (secondary)** | tonal: `primaryContainer` + `onPrimaryContainer`. |
| **Button (text)** | sin fondo, matcha, para acciones terciarias. |
| **Card** | `surface`, radio `medium` (20 dp), padding 16–20 dp, elevación nivel 1. |
| **ChildCard (hero)** | radio `extraLarge` (36 dp), avatar + nombre + estado + barra de tiempo. Fondo `primaryContainer` muy claro. |
| **Chip** | pill, `surfaceVariant`, para categorías/filtros; seleccionado = `secondaryContainer`. |
| **ProgressBar (tiempo)** | barra redondeada 10 dp alto, track `surfaceVariant`, fill matcha; cambia a terracota (warning) al acercarse al límite y a lavanda/descanso al alcanzarlo (nunca rojo). |
| **UsageChart (semana)** | barras redondeadas por día, altura proporcional; día actual resaltado. Minimalista, sin ejes pesados. |
| **Avatar** | círculo; foto o color de acento + inicial/emoji; borde 2 dp `surface`. |
| **AppCard (uso por app)** | ícono categoría + nombre + tiempo alineado a la derecha; fila 56 dp. |
| **StatusIndicator** | punto 8 dp + etiqueta. 🟢 matcha "En línea" · ⚪ gris cálido "Sin conexión". Nunca rojo alarmante. |
| **BottomNavigation** | 5 destinos, íconos rounded, indicador de píldora `secondaryContainer`, fondo `surface`. |
| **Dialog / Sheet** | radio `large` (28 dp), Kori opcional arriba, botones pill. |
| **Alert (inline)** | banner tonal (success/warning) con ícono; texto positivo. |
| **EmptyState** | Kori (estado "vacío/curioso") + título breve + acción. |
| **LoadingState** | brote animado o skeletons con shimmer suave sobre `surfaceVariant`. |

---

## 11. Navegación

**MIKORI Parent** — bottom navigation de 5 destinos (simple, sin exceso):

```
🏠 Inicio   👨‍👩‍👧 Familia   📊 Actividad   🌱 Reglas   ⚙️ Ajustes
```

**MIKORI Kids** — navegación mínima (1–2 pantallas): Home + "Mi tiempo". Sin menús complejos.

---

## 12. Pantallas — MIKORI Parent

Tono: **más elegante y profesional que infantil** — moderna + tranquila + familiar.

### 12.1 Dashboard (Inicio)
```
────────────────────────────────
 mikori 🌸

 Buenos días, mamá 🌱

 Tu familia
 ┌────────────────────────────┐
 │  👦  Mateo        🟢 En línea │
 │                              │
 │  Tiempo de hoy               │
 │  1h 32min  /  2h             │
 │  ██████████████░░░░░  76%    │
 │                              │
 │  Quedan 28 min               │
 └────────────────────────────┘
 ┌────────────────────────────┐
 │  👧  Sofía        🟢 En línea │
 │  2h 10min / 3h  ███████░░    │
 └────────────────────────────┘

 Acciones rápidas
 [ + Añadir hijo ]   [ Ver actividad ]
────────────────────────────────
 🏠   👨‍👩‍👧   📊   🌱   ⚙️
```
Tarjetas suaves, barras de progreso, saludo cálido. Sin alarmas.

### 12.2 Perfil del hijo
```
────────────────────────────────
 ←  🌸 Mateo

        1h 32min
      de 2h permitidas
   ██████████████░░░░░

 Uso de aplicaciones (hoy)
 ┌────────────────────────────┐
 │ ▶️  YouTube          42 min  │
 │ 🎮  Juegos           31 min  │
 │ 💬  WhatsApp         19 min  │
 │ 🌐  Chrome           15 min  │
 └────────────────────────────┘

 [ Ver semana ]      [ Editar límites ]
────────────────────────────────
```

### 12.3 Estadísticas (Actividad)
```
────────────────────────────────
 Actividad — Mateo        [Hoy ▾]

 Esta semana
  L   M   X   J   V   S   D
  ▁   ▃   ▅   ▂   ▆   █   ▇      ← barras redondeadas
  matcha; día actual resaltado

 Promedio diario: 1h 48min
 Total semana: 12h 36min

 Apps más usadas (semana)
 ▶️ YouTube   4h 50m  ████████
 🎮 Juegos    3h 12m  █████
 💬 WhatsApp  2h 05m  ███
────────────────────────────────
```

### 12.4 Configuración de límites (Reglas)
```
────────────────────────────────
 ←  Límites diarios — Mateo

 [ Aplicar el mismo límite a todos ]
    ◉ 2h 00min   ── slider ──

 O por día:
  Lunes      [ 2h 00 ]
  Martes     [ 2h 00 ]
  Miércoles  [ 2h 00 ]
  Jueves     [ 2h 00 ]
  Viernes    [ 3h 00 ]
  Sábado     [ 4h 00 ]
  Domingo    [ 4h 00 ]

           [  Guardar  ]
────────────────────────────────
```

### 12.5 Vinculación
```
────────────────────────────────
 ←  Vincular dispositivo de Mateo

        ( ^ ^ )🌱
       Kori te ayuda

 En el teléfono de Mateo, abre
 MIKORI Kids e introduce este código:

     ┌─────────────────────┐
     │    MIKORI-483921     │
     └─────────────────────┘

 ⏳ Expira en 14:32

 Esperando al dispositivo…  ◜◝◞◟
────────────────────────────────
```
Al vincular: Kori da un saltito ✨ y aparece "¡Listo! Mateo está conectado."

---

## 13. Pantallas — MIKORI Kids

Tono: **más divertido**, misma identidad. Ilustraciones, Kori, mensajes positivos. **Transparente**: siempre indica que MIKORI y el tutor administran el dispositivo.

### 13.1 Home
```
────────────────────────────────
        ( ^ ^ )🌱
   ¡Hola, Mateo!

   Hoy llevas
        1h 32min
   de tiempo de pantalla.

   ¡Todavía tienes
    28 minutos para
     descubrir! ✨

   ██████████████░░░░░

 · Administrado por MIKORI y tu familia ·
────────────────────────────────
```

### 13.2 Tiempo agotado (límite alcanzado)
```
────────────────────────────────

            🌙
      ( -  - )   Kori descansa

   ¡Hora de descansar!

   Ya usaste tu tiempo
   de pantalla de hoy.

   Mañana podrás volver
   a jugar y descubrir. 🌱

            mikori
────────────────────────────────
```
Fondo con tono nocturno suave (lavanda/matcha oscuro). Cero sensación de castigo.

### 13.3 App en descanso (restringida — V2)
```
────────────────────────────────

            🌸
       ( ˘ ᵕ ˘ )  "shh"

   Esta aplicación está
   descansando por ahora.

   Puedes volver a usarla
   más tarde.

          ✨ mikori
────────────────────────────────
```
Nunca "Has sido bloqueado". Siempre lenguaje amable.

---

## 14. Dark Mode

Inspiración: **noche japonesa** — cielo oscuro azul-violeta + acentos **lavanda**.

- Fondos profundos y cálidos (`#14141A` / `#1C1C24`), no negro puro.
- Matcha se aclara (`#A6CBA0`) para mantener contraste; sakura y lavanda como acentos.
- Sensación **nocturna y tranquila**; ideal para la pantalla de "hora de descansar".
- Mantener contraste AA y la identidad MIKORI (mismos radios, misma calidez).
- Elevación se expresa con superficies más claras, no con sombras.

```
 Dashboard (dark)
────────────────────────────────
 mikori 🌸        (surface #1C1C24)
 Buenas noches, mamá 🌙
 ┌────────────────────────────┐
 │ 👦 Mateo        🟢 En línea  │  ← primaryContainer #35563B
 │ 1h 32min / 2h                │
 │ ██████████████░░░  (matcha claro)
 └────────────────────────────┘
────────────────────────────────
```

---

## 15. Ilustraciones

Lenguaje consistente, **minimalista** (no convertir la app en dibujo animado):
- Motivos de naturaleza: hojas, flores sakura, luna, estrellas, nubes, montañas suaves, pequeños animales del bosque.
- Trazo suave, formas rellenas, paleta MIKORI, mucho espacio negativo.
- Uso: onboarding, estados vacíos, pantallas emocionales (tiempo agotado). No decorar cada pantalla.
- Kori es el hilo conductor de todas las ilustraciones.

---

## 16. Accesibilidad

- Contraste **AA** mínimo en texto y componentes (verificar cada par color/onColor de §3).
- Tamaños de toque ≥ 48×48 dp.
- Soporte de **fuentes escalables** (sp) y layouts que no rompen con texto grande.
- Respetar **modo oscuro** y **reducir movimiento** del sistema.
- No comunicar estado **solo por color** (añadir ícono/etiqueta: "En línea", "Sin conexión").
- Etiquetas de contenido (contentDescription) en íconos e ilustraciones informativas.

---

## Diseñado para sentirse como una marca real

> # mikori 🌸
> ### Crece, juega y descubre.
>
> Una app japonesa moderna: cálida, tecnológica y familiar.

*Documento vivo. Los tokens aquí definidos son la fuente para `ui/theme/` en MIKORI Parent y Kids.*
