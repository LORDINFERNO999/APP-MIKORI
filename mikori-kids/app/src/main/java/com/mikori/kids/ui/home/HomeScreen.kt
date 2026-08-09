package com.mikori.kids.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.kids.core.ui.components.KoriMascot
import com.mikori.kids.core.ui.components.KoriMood
import com.mikori.kids.core.ui.components.MikoriButton
import com.mikori.kids.core.ui.components.TimeProgressBar
import com.mikori.kids.core.ui.theme.Spacing
import com.mikori.kids.core.ui.theme.textSecondary
import com.mikori.kids.core.util.TimeFormat
import com.mikori.kids.usage.UsageAccess

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Al volver de Ajustes (acceso al uso), re-verifica y recarga.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    when {
        state.limitReached -> TimeUpScreen(state.childName)
        else -> HomeContent(state)
    }
}

@Composable
private fun HomeContent(state: HomeUiState) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KoriMascot(size = 120.dp, mood = KoriMood.HAPPY)
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = state.childName?.let { "¡Hola, $it!" } ?: "¡Hola!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (!state.usageAccessGranted) {
            Spacer(Modifier.height(Spacing.xl))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(Spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Falta activar el acceso al uso",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "MIKORI necesita este permiso para medir tu tiempo de pantalla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    MikoriButton(
                        text = "Activar",
                        onClick = { context.startActivity(UsageAccess.settingsIntent()) },
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.xxxl))
        Text("Hoy llevas", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.textSecondary)
        Text(
            text = TimeFormat.humanize(state.usedSeconds),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        val limit = state.limitMinutes
        if (limit != null) {
            Spacer(Modifier.height(Spacing.lg))
            val progress = if (limit > 0)
                state.usedSeconds.toFloat() / (limit * 60f) else 0f
            TimeProgressBar(progress = progress)
            Spacer(Modifier.height(Spacing.md))
            val remaining = state.remainingSeconds ?: 0
            Text(
                text = "¡Todavía tienes ${TimeFormat.humanize(remaining)} para descubrir! ✨",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(Spacing.huge))
        Text(
            "· Administrado por MIKORI y tu familia ·",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.textSecondary,
        )
    }
}

/** Estado "tiempo agotado": tono nocturno, Kori descansando, mensaje amable. */
@Composable
private fun TimeUpScreen(childName: String?) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🌙", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(Spacing.md))
            KoriMascot(size = 140.dp, mood = KoriMood.RESTING)
            Spacer(Modifier.height(Spacing.xl))
            Text(
                text = "¡Hora de descansar!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = "Ya usaste tu tiempo de pantalla de hoy. Mañana podrás volver a jugar y descubrir. 🌱",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.huge))
            Text(
                "mikori",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
