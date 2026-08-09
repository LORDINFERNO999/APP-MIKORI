package com.mikori.parent.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mikori.parent.core.ui.theme.mikoriColors

/**
 * Indicador de estado del dispositivo. Nunca usa rojo alarmante.
 * El estado se comunica con punto + etiqueta (no solo color — accesibilidad).
 */
@Composable
fun StatusIndicator(
    online: Boolean,
    modifier: Modifier = Modifier,
    onlineLabel: String = "En línea",
    offlineLabel: String = "Sin conexión",
) {
    val color = if (online) MaterialTheme.mikoriColors.online else MaterialTheme.mikoriColors.offline
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = if (online) onlineLabel else offlineLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.mikoriColors.textSecondary,
        )
    }
}
