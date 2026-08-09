package com.mikori.parent.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors
import com.mikori.parent.core.util.TimeFormat

/**
 * Tarjeta "hero" de un hijo en el dashboard "Mi familia".
 * Forma extraLarge (36dp), fondo primaryContainer suave.
 */
@Composable
fun ChildCard(
    name: String,
    online: Boolean,
    usedSeconds: Int,
    limitMinutes: Int?,
    remainingSeconds: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    MikoriCard(
        modifier = modifier.clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = Spacing.xl,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(
                    label = name,
                    background = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    StatusIndicator(online = online)
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "Tiempo de hoy",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.mikoriColors.textSecondary,
                    )
                    Text(
                        text = buildString {
                            append(TimeFormat.humanize(usedSeconds))
                            if (limitMinutes != null) append(" / ${TimeFormat.humanizeMinutes(limitMinutes)}")
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (limitMinutes != null) {
                Spacer(Modifier.height(Spacing.md))
                val limitReached = remainingSeconds != null && remainingSeconds <= 0
                val progress = if (limitMinutes > 0) usedSeconds.toFloat() / (limitMinutes * 60f) else 0f
                TimeProgressBar(progress = progress, limitReached = limitReached)
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = when {
                        limitReached -> "Tiempo de hoy completado 🌙"
                        remainingSeconds != null -> "Quedan ${TimeFormat.humanize(remainingSeconds)}"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "Sin límite configurado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.mikoriColors.textSecondary,
                )
            }
        }
    }
}
