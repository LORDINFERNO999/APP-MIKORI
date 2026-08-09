package com.mikori.parent.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikori.parent.core.ui.theme.Spacing
import com.mikori.parent.core.ui.theme.mikoriColors

data class DayBar(val label: String, val seconds: Int, val highlighted: Boolean = false)

/**
 * Gráfico semanal minimalista: barras redondeadas, día actual resaltado.
 */
@Composable
fun WeekBarChart(
    days: List<DayBar>,
    modifier: Modifier = Modifier,
    barMaxHeight: androidx.compose.ui.unit.Dp = 120.dp,
) {
    val maxSeconds = (days.maxOfOrNull { it.seconds } ?: 0).coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { day ->
            val fraction = day.seconds.toFloat() / maxSeconds
            val animated by animateFloatAsState(targetValue = fraction, label = "bar-${day.label}")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(32.dp),
            ) {
                Box(
                    modifier = Modifier
                        .height(barMaxHeight)
                        .width(18.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animated.coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(
                                if (day.highlighted) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.highlighted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.mikoriColors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
