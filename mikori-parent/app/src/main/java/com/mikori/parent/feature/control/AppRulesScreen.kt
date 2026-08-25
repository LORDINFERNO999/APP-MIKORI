package com.mikori.parent.feature.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.LoadingState
import com.mikori.parent.core.ui.components.MikoriButton
import com.mikori.parent.core.ui.components.MikoriCard
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors
import com.mikori.parent.core.util.TimeFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRulesScreen(
    onBack: () -> Unit,
    viewModel: AppRulesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Límites por app") },
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
        if (state.loading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        if (state.items.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(Spacing.xl)) {
                Text(
                    "Aún no hay apps registradas para este hijo. Aparecerán aquí a medida que use el dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.mikoriColors.textSecondary,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            items(state.items, key = { it.packageName }) { app ->
                MikoriCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(app.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    if (app.isBlocked) "Bloqueada"
                                    else if (app.maxMinutes > 0) "Máx. ${TimeFormat.humanizeMinutes(app.maxMinutes)}"
                                    else "Sin restricción",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.mikoriColors.textSecondary,
                                )
                            }
                            Switch(
                                checked = app.isBlocked,
                                onCheckedChange = { viewModel.setBlocked(app.packageName, it) },
                            )
                        }
                        if (!app.isBlocked) {
                            Spacer(Modifier.height(Spacing.sm))
                            Slider(
                                value = app.maxMinutes.toFloat(),
                                onValueChange = { viewModel.setLimit(app.packageName, (it / 15f).toInt() * 15) },
                                valueRange = 0f..180f,
                                steps = 11,
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(Spacing.sm))
                MikoriButton(text = "Guardar", onClick = { viewModel.save() }, loading = state.saving)
                state.saved.let { if (it) {
                    Spacer(Modifier.height(Spacing.sm))
                    Text("Guardado 🌱", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                } }
                state.error?.let {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(Spacing.huge))
            }
        }
    }
}
