package com.mikori.parent.feature.child

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.AppUsageRow
import com.mikori.parent.core.ui.components.EmptyState
import com.mikori.parent.core.ui.components.KoriMood
import com.mikori.parent.core.ui.components.LoadingState
import com.mikori.parent.core.ui.components.MikoriCard
import com.mikori.parent.core.ui.components.MikoriTonalButton
import com.mikori.parent.core.ui.components.StatusIndicator
import com.mikori.parent.core.ui.components.TimeProgressBar
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors
import com.mikori.parent.core.util.TimeFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailScreen(
    onBack: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenLimits: () -> Unit,
    onOpenLinking: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ChildDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) { if (state.deleted) onDeleted() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.name.isBlank()) "Detalle" else state.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Eliminar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))
            state.error != null -> EmptyState(
                title = "No pudimos cargar el detalle",
                subtitle = state.error ?: "",
                mood = KoriMood.CURIOUS,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screenHorizontal),
            ) {
                // Hero de tiempo
                Spacer(Modifier.height(Spacing.sm))
                StatusIndicator(online = state.online)
                Spacer(Modifier.height(Spacing.lg))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = TimeFormat.humanize(state.usedSeconds),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.limitMinutes?.let { "de ${TimeFormat.humanizeMinutes(it)} permitidas" }
                            ?: "sin límite configurado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.mikoriColors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                if (state.limitMinutes != null) {
                    Spacer(Modifier.height(Spacing.md))
                    val progress = if (state.limitMinutes!! > 0)
                        state.usedSeconds.toFloat() / (state.limitMinutes!! * 60f) else 0f
                    TimeProgressBar(progress = progress, limitReached = state.limitReached)
                }

                Spacer(Modifier.height(Spacing.xl))
                Text(
                    text = "Uso de aplicaciones (hoy)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(Spacing.sm))
                MikoriCard {
                    if (state.topApps.isEmpty()) {
                        Text(
                            "Todavía no hay actividad registrada hoy.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.mikoriColors.textSecondary,
                        )
                    } else {
                        Column {
                            state.topApps.forEach { app ->
                                AppUsageRow(
                                    label = app.appLabel ?: app.packageName,
                                    category = app.category,
                                    seconds = app.seconds,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xl))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    MikoriTonalButton(text = "Ver semana", onClick = onOpenStats, modifier = Modifier.weight(1f))
                    MikoriTonalButton(text = "Límites", onClick = onOpenLimits, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(Spacing.md))
                MikoriTonalButton(
                    text = "Vincular dispositivo",
                    onClick = onOpenLinking,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.huge))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("¿Eliminar a ${state.name}?") },
            text = { Text("Se borrará su perfil y su historial. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { showDelete = false; viewModel.delete() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } },
        )
    }
}
