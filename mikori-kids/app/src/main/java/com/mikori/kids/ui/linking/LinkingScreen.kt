package com.mikori.kids.ui.linking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.kids.core.ui.components.KoriMascot
import com.mikori.kids.core.ui.components.KoriMood
import com.mikori.kids.core.ui.components.MikoriButton
import com.mikori.kids.core.ui.components.MikoriTextField
import com.mikori.kids.core.ui.theme.Spacing
import com.mikori.kids.core.ui.theme.textSecondary

@Composable
fun LinkingScreen(
    onBack: () -> Unit,
    viewModel: LinkingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var code by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        KoriMascot(size = 110.dp, mood = KoriMood.CURIOUS)
        Spacer(Modifier.height(Spacing.lg))
        Text(
            "Conecta con tu familia",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            "Pide a tu madre, padre o tutor el código que aparece en su app MIKORI y escríbelo aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.textSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Spacing.xl))
        MikoriTextField(
            value = code,
            onValueChange = { code = it; viewModel.clearError() },
            label = "Código (MIKORI-XXXXXX)",
            errorText = state.error,
        )

        Spacer(Modifier.height(Spacing.xl))
        MikoriButton(
            text = "Conectar",
            onClick = { viewModel.redeem(code) },
            loading = state.loading,
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            "· Administrado por MIKORI y tu familia ·",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.textSecondary,
        )
    }
}
