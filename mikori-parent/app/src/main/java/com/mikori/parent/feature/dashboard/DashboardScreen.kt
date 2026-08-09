package com.mikori.parent.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.ChildCard
import com.mikori.parent.core.ui.components.EmptyState
import com.mikori.parent.core.ui.components.KoriMood
import com.mikori.parent.core.ui.components.LoadingState
import com.mikori.parent.core.ui.components.MikoriButton
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors
import java.util.Calendar

@Composable
fun DashboardScreen(
    onChildClick: (Long) -> Unit,
    onAddChild: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (state.children.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddChild,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Añadir hijo") },
                )
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingState(Modifier.padding(padding))

            state.children.isEmpty() -> EmptyState(
                title = "Aún no hay nadie en tu familia",
                subtitle = "Añade a tu primer hijo para empezar a acompañar su tiempo de pantalla.",
                mood = KoriMood.CURIOUS,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                action = { MikoriButton(text = "Añadir hijo", onClick = onAddChild) },
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = Spacing.screenHorizontal,
                    end = Spacing.screenHorizontal,
                    top = Spacing.lg,
                    bottom = Spacing.huge,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.cardGap),
            ) {
                item {
                    Column {
                        Text(
                            text = greeting(state.userName),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Tu familia",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.mikoriColors.textSecondary,
                        )
                        Spacer(Modifier.height(Spacing.xs))
                    }
                }
                items(state.children, key = { it.id }) { child ->
                    ChildCard(
                        name = child.name,
                        online = child.online,
                        usedSeconds = child.usedSeconds,
                        limitMinutes = child.limitMinutes,
                        remainingSeconds = child.remainingSeconds,
                        onClick = { onChildClick(child.id) },
                    )
                }
            }
        }
    }
}

private fun greeting(name: String?): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val part = when (hour) {
        in 5..11 -> "Buenos días"
        in 12..19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    return if (!name.isNullOrBlank()) "$part, ${name.substringBefore(' ')} 🌱" else "$part 🌱"
}
