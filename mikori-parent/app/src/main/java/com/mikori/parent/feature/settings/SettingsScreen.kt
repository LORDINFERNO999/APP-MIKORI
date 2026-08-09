package com.mikori.parent.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.Avatar
import com.mikori.parent.core.ui.components.MikoriCard
import com.mikori.parent.core.ui.components.MikoriTonalButton
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.xxxl))
        Avatar(label = userName?.take(2)?.uppercase() ?: "🙂", size = 72.dp)
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = userName ?: "Mi cuenta",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(Spacing.xxxl))
        MikoriCard {
            Column {
                Text("MIKORI", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Crece, juega y descubre.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.mikoriColors.textSecondary,
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "Versión 1.0.0 — Monitoreo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.mikoriColors.textSecondary,
                )
            }
        }

        Spacer(Modifier.height(Spacing.xl))
        MikoriTonalButton(
            text = "Cerrar sesión",
            onClick = { viewModel.logout() },
        )
    }
}
