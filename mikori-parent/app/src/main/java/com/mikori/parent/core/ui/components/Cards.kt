package com.mikori.parent.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikori.parent.core.ui.theme.Spacing

/**
 * Tarjeta base de MIKORI: surface, forma medium (20dp), elevación sutil.
 */
@Composable
fun MikoriCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: Dp = Spacing.lg,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        shadowElevation = 1.dp,
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

/**
 * Avatar circular con emoji/inicial sobre un color de acento.
 */
@Composable
fun Avatar(
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    background: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Surface(
        modifier = modifier.size(size),
        color = background,
        shape = CircleShape,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.take(2),
                color = content,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
