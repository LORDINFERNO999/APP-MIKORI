package com.mikori.parent.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikori.parent.core.ui.components.KoriMascot
import com.mikori.parent.core.ui.components.KoriMood
import com.mikori.parent.core.ui.theme.mikoriColors

/**
 * Encabezado de marca: Kori + wordmark + eslogan.
 */
@Composable
fun BrandHeader(
    modifier: Modifier = Modifier,
    mood: KoriMood = KoriMood.HAPPY,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KoriMascot(size = 96.dp, mood = mood)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "mikori",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Crece, juega y descubre.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.mikoriColors.textSecondary,
        )
    }
}
