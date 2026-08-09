package com.mikori.parent.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikori.parent.core.ui.theme.Lavender
import com.mikori.parent.core.ui.theme.mikoriColors

/**
 * Barra de progreso de tiempo (docs/02-design-system.md §10).
 * El color comunica sin alarmar: matcha (normal) → terracota (cerca del límite)
 * → lavanda/descanso (límite alcanzado). Nunca rojo.
 */
@Composable
fun TimeProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    limitReached: Boolean = false,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(targetValue = clamped, label = "timeProgress")

    val targetColor = when {
        limitReached -> Lavender
        clamped >= 0.85f -> MaterialTheme.mikoriColors.warning
        else -> MaterialTheme.colorScheme.primary
    }
    val fillColor by animateColorAsState(targetValue = targetColor, label = "timeProgressColor")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(fillColor),
        )
    }
}
