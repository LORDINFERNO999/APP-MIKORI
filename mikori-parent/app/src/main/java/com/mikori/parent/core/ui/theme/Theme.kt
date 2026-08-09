package com.mikori.parent.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colores semánticos de MIKORI que no existen en Material 3
 * (success / warning / acentos de marca). Se exponen vía CompositionLocal
 * y se acceden con MaterialTheme.mikoriColors.
 */
@Immutable
data class MikoriExtraColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val textSecondary: Color,
    val online: Color,
    val offline: Color,
)

private val LightExtraColors = MikoriExtraColors(
    success = LightSuccess,
    onSuccess = LightOnSuccess,
    successContainer = LightSuccessContainer,
    warning = LightWarning,
    onWarning = LightOnWarning,
    warningContainer = LightWarningContainer,
    textSecondary = LightTextSecondary,
    online = LightSuccess,
    offline = Color(0xFF9C968B),
)

private val DarkExtraColors = MikoriExtraColors(
    success = DarkSuccess,
    onSuccess = DarkOnSuccess,
    successContainer = DarkSuccessContainer,
    warning = DarkWarning,
    onWarning = DarkOnWarning,
    warningContainer = DarkWarningContainer,
    textSecondary = DarkTextSecondary,
    online = DarkSuccess,
    offline = Color(0xFF7A756D),
)

val LocalMikoriExtraColors = staticCompositionLocalOf { LightExtraColors }

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

/**
 * Tema raíz de MIKORI. No usa Dynamic Color a propósito: la identidad de
 * marca (matcha/sakura) debe mantenerse consistente en todos los dispositivos.
 */
@Composable
fun MikoriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(LocalMikoriExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MikoriTypography,
            shapes = MikoriShapes,
            content = content,
        )
    }
}

/** Acceso rápido a los colores extra: MaterialTheme.mikoriColors.success */
val MaterialTheme.mikoriColors: MikoriExtraColors
    @Composable
    get() = LocalMikoriExtraColors.current
