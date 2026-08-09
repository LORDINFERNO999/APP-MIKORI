package com.mikori.kids.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikori.kids.core.ui.components.KoriMascot
import com.mikori.kids.core.ui.components.KoriMood
import com.mikori.kids.core.ui.components.MikoriButton
import com.mikori.kids.core.ui.theme.Spacing
import com.mikori.kids.core.ui.theme.textSecondary
import com.mikori.kids.usage.UsageAccess

/**
 * Onboarding transparente: explica qué hace MIKORI y solicita el acceso al uso.
 * Nada oculto: el niño ve claramente que el dispositivo está acompañado por su familia.
 */
@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        KoriMascot(size = 130.dp, mood = KoriMood.HAPPY)
        Spacer(Modifier.height(Spacing.lg))
        Text(
            "¡Hola! Soy Kori 🌱",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            "Este dispositivo usa MIKORI para acompañar tu tiempo de pantalla junto a tu familia.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            "MIKORI solo mide cuánto tiempo usas cada app. No lee tus mensajes, ni tus fotos, ni nada privado.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.textSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.xxxl))
        MikoriButton(
            text = "Activar acceso al uso",
            onClick = { context.startActivity(UsageAccess.settingsIntent()) },
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            "Se abrirá Ajustes de Android. Activa MIKORI Kids en la lista y vuelve.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.textSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.xl))
        MikoriButton(text = "Continuar", onClick = onContinue)
    }
}
