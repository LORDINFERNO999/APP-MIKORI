package com.mikori.parent.feature.control

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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.LoadingState
import com.mikori.parent.core.ui.components.MikoriButton
import com.mikori.parent.core.ui.components.MikoriCard
import com.mikori.parent.core.ui.components.MikoriTextField
import com.mikori.parent.core.ui.components.MikoriTonalButton
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors

private val DAY_LABELS = listOf("L", "M", "X", "J", "V", "S", "D")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    onBack: () -> Unit,
    viewModel: SchedulesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Horarios") },
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
            if (state.schedules.isEmpty()) {
                Text(
                    "Aún no hay horarios. Añade uno abajo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.mikoriColors.textSecondary,
                )
            } else {
                state.schedules.forEach { s ->
                    MikoriCard(modifier = Modifier.padding(bottom = Spacing.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(s.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "${s.startTime} – ${s.endTime}  ·  ${daysLabel(s.daysMask)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.mikoriColors.textSecondary,
                                )
                            }
                            IconButton(onClick = { viewModel.delete(s.id) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text("Añadir rápido", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                MikoriTonalButton(text = "Nocturno", onClick = { viewModel.addPreset("night") }, modifier = Modifier.weight(1f))
                MikoriTonalButton(text = "Escolar", onClick = { viewModel.addPreset("school") }, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(Spacing.xl))
            Text("Personalizado", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(Spacing.sm))
            CustomScheduleForm(
                working = state.working,
                onCreate = { name, start, end, mask -> viewModel.addCustom(name, start, end, mask) },
            )

            state.error?.let {
                Spacer(Modifier.height(Spacing.md))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(Spacing.huge))
        }
    }
}

@Composable
private fun CustomScheduleForm(
    working: Boolean,
    onCreate: (String, String, String, Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("21:30") }
    var end by remember { mutableStateOf("07:00") }
    var mask by remember { mutableIntStateOf(127) }

    MikoriCard {
        Column {
            MikoriTextField(value = name, onValueChange = { name = it }, label = "Nombre")
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                MikoriTextField(value = start, onValueChange = { start = it }, label = "Inicio (HH:MM)", modifier = Modifier.weight(1f))
                MikoriTextField(value = end, onValueChange = { end = it }, label = "Fin (HH:MM)", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(Spacing.md))
            Text("Días", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.mikoriColors.textSecondary)
            Spacer(Modifier.height(Spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DAY_LABELS.forEachIndexed { index, label ->
                    val selected = (mask and (1 shl index)) != 0
                    FilterChip(
                        selected = selected,
                        onClick = { mask = mask xor (1 shl index) },
                        label = { Text(label) },
                    )
                }
            }
            Spacer(Modifier.height(Spacing.md))
            MikoriButton(
                text = "Añadir horario",
                onClick = { onCreate(name, start, end, mask) },
                loading = working,
            )
        }
    }
}

private fun daysLabel(mask: Int): String {
    if (mask == 127) return "Todos los días"
    val days = DAY_LABELS.filterIndexed { i, _ -> (mask and (1 shl i)) != 0 }
    return if (days.isEmpty()) "—" else days.joinToString(" ")
}
