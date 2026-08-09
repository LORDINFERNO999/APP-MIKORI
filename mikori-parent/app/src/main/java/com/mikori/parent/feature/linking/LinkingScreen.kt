package com.mikori.parent.feature.linking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.KoriMascot
import com.mikori.parent.core.ui.components.KoriMood
import com.mikori.parent.core.ui.components.LoadingState
import com.mikori.parent.core.ui.components.MikoriButton
import com.mikori.parent.core.ui.components.MikoriCard
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkingScreen(
    onBack: () -> Unit,
    viewModel: LinkingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Vincular dispositivo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        if (state.generating) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            when {
                state.linked -> LinkedContent(onBack)
                state.expired -> ExpiredContent(onRegenerate = viewModel::generate)
                else -> PendingContent(
                    code = state.code ?: "",
                    secondsLeft = state.secondsLeft,
                    error = state.error,
                )
            }
        }
    }
}

@Composable
private fun PendingContent(code: String, secondsLeft: Int, error: String?) {
    Spacer(Modifier.height(Spacing.lg))
    KoriMascot(size = 110.dp, mood = KoriMood.HAPPY)
    Spacer(Modifier.height(Spacing.lg))
    Text(
        "En el teléfono del hijo, abre MIKORI Kids e introduce este código:",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.mikoriColors.textSecondary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(Spacing.xl))
    MikoriCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
        Text(
            text = code,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Spacer(Modifier.height(Spacing.lg))
    Text(
        text = "Expira en ${formatMmSs(secondsLeft)}",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.mikoriColors.textSecondary,
    )
    Spacer(Modifier.height(Spacing.xl))
    Text(
        "Esperando al dispositivo…",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.mikoriColors.textSecondary,
    )
    error?.let {
        Spacer(Modifier.height(Spacing.md))
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LinkedContent(onBack: () -> Unit) {
    Spacer(Modifier.height(Spacing.xxxl))
    KoriMascot(size = 130.dp, mood = KoriMood.HAPPY)
    Spacer(Modifier.height(Spacing.xl))
    Text(
        "¡Listo! Dispositivo vinculado ✨",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(Spacing.sm))
    Text(
        "Ya puedes acompañar su tiempo de pantalla.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.mikoriColors.textSecondary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(Spacing.xxxl))
    MikoriButton(text = "Continuar", onClick = onBack)
}

@Composable
private fun ExpiredContent(onRegenerate: () -> Unit) {
    Spacer(Modifier.height(Spacing.xxxl))
    KoriMascot(size = 120.dp, mood = KoriMood.RESTING)
    Spacer(Modifier.height(Spacing.xl))
    Text(
        "El código expiró",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(Spacing.sm))
    Text(
        "Genera uno nuevo para continuar con la vinculación.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.mikoriColors.textSecondary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(Spacing.xxxl))
    MikoriButton(text = "Generar nuevo código", onClick = onRegenerate)
}

private fun formatMmSs(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
