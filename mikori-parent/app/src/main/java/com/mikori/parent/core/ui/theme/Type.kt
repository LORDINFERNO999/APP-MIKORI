package com.mikori.parent.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía de MIKORI (docs/02-design-system.md §4).
 *
 * Marca/Display: "M PLUS Rounded 1c"   ·   UI/Cuerpo: "Nunito"
 *
 * ─────────────────────────────────────────────────────────────────────
 *  CÓMO ACTIVAR LAS FUENTES DE MARCA (un solo paso, en Android Studio):
 *
 *  Opción A — Downloadable Fonts (recomendada, sin binarios):
 *    File > New > Font > "Add font" y busca "M PLUS Rounded 1c" y "Nunito".
 *    Android Studio generará res/font/*.xml y res/values/font_certs.xml con
 *    los certificados oficiales de Google. Luego:
 *
 *      val provider = GoogleFont.Provider(
 *          "com.google.android.gms.fonts",
 *          "com.google.android.gms",
 *          R.array.com_google_android_gms_fonts_certs)
 *      val MPlusRounded = FontFamily(Font(GoogleFont("M PLUS Rounded 1c"), provider, FontWeight.Bold))
 *      val Nunito = FontFamily(Font(GoogleFont("Nunito"), provider, FontWeight.Normal), ...)
 *
 *    y reemplaza DisplayFamily/BodyFamily por MPlusRounded/Nunito.
 *
 *  Opción B — Bundled: copia los .ttf en res/font y construye los FontFamily.
 *
 *  Mientras tanto usamos la fuente por defecto del sistema para que compile
 *  sin datos externos. El resto de la identidad (color, forma, layout) ya
 *  aporta el carácter MIKORI.
 * ─────────────────────────────────────────────────────────────────────
 */
val DisplayFamily: FontFamily = FontFamily.Default // ← M PLUS Rounded 1c
val BodyFamily: FontFamily = FontFamily.Default     // ← Nunito

val MikoriTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.Bold,
        fontSize = 15.sp, lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
)
