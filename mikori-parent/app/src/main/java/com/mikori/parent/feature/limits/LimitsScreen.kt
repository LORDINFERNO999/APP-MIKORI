package com.mikori.parent.feature.limits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.LoadingState
import com.mikori.parent.core.ui.components.MikoriButton
import com.mikori.parent.core.ui.components.MikoriCard
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors
import com.mikori.parent.core.util.TimeFormat

private val DAY_NAMES = mapOf(
    1 to "Lunes", 2 to "Martes", 3 to "Miércoles", 4 to "Jueves",
    5 to "Viernes", 6 to "Sábado", 7 to "Domingo",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitsScreen(
    onBack: () -> Unit,
    viewModel: LimitsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Límites diarios") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal),
        ) {
            Spacer(Modifier.height(Spacing.sm))
            MikoriCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Mismo límite todos los días",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Desactívalo para configurar cada día",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.mikoriColors.textSecondary,
                        )
                    }
                    Switch(
                        checked = state.sameForAll,
                        onCheckedChange = { viewModel.setSameForAll(it) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            if (state.sameForAll) {
                MikoriCard {
                    LimitSlider(
                        label = "Todos los días",
                        minutes = state.allMinutes,
                        onChange = { viewModel.setAllMinutes(it) },
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    (1..7).forEach { dow ->
                        MikoriCard {
                            LimitSlider(
                                label = DAY_NAMES[dow] ?: "",
                                minutes = state.perDayMinutes[dow] ?: 120,
                                onChange = { viewModel.setDayMinutes(dow, it) },
                            )
                        }
                    }
                }
            }

            state.error?.let {
                Spacer(Modifier.height(Spacing.md))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            if (state.saved) {
                Spacer(Modifier.height(Spacing.md))
                Text("Límites guardados 🌱", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(Spacing.xl))
            MikoriButton(text = "Guardar", onClick = { viewModel.save() }, loading = state.saving)
            Spacer(Modifier.height(Spacing.huge))
        }
    }
}

@Composable
private fun LimitSlider(label: String, minutes: Int, onChange: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                TimeFormat.humanizeMinutes(minutes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        // Rango 0..6h en pasos de 15 min (24 pasos).
        Slider(
            value = minutes.toFloat(),
            onValueChange = { onChange((it / 15f).toInt() * 15) },
            valueRange = 0f..360f,
            steps = 23,
        )
    }
}
