package com.mikori.parent.feature.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Schedule
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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.MikoriButton
import com.mikori.parent.core.ui.components.MikoriCard
import com.mikori.parent.core.ui.components.MikoriTonalButton
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    onBack: () -> Unit,
    onOpenAppRules: () -> Unit,
    onOpenSchedules: () -> Unit,
    viewModel: ControlViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Control") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.screenHorizontal),
        ) {
            Spacer(Modifier.height(Spacing.sm))

            // Pausas
            Text("Pausa", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(Spacing.sm))
            MikoriCard {
                Column {
                    Text(
                        "Bloquea el dispositivo temporalmente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.mikoriColors.textSecondary,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        MikoriTonalButton(text = "30 min", onClick = { viewModel.startPause(30) }, modifier = Modifier.weight(1f))
                        MikoriTonalButton(text = "1 hora", onClick = { viewModel.startPause(60) }, modifier = Modifier.weight(1f))
                        MikoriTonalButton(text = "2 horas", onClick = { viewModel.startPause(120) }, modifier = Modifier.weight(1f))
                    }
                    if (state.pauseUntil != null) {
                        Spacer(Modifier.height(Spacing.sm))
                        MikoriButton(text = "Cancelar pausa", onClick = { viewModel.cancelPause() })
                    }
                    state.message?.let {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    state.error?.let {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))
            Text("Reglas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(Spacing.sm))
            MikoriCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(Spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text("Límites por app", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Bloquear apps o poner un máximo diario", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.mikoriColors.textSecondary)
                    }
                    MikoriTonalButton(text = "Abrir", onClick = onOpenAppRules)
                }
            }
            Spacer(Modifier.height(Spacing.md))
            MikoriCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(Spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text("Horarios", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("Escolar, nocturno o personalizado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.mikoriColors.textSecondary)
                    }
                    MikoriTonalButton(text = "Abrir", onClick = onOpenSchedules)
                }
            }
        }
    }
}
