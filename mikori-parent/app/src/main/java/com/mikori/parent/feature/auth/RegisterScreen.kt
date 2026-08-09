package com.mikori.parent.feature.auth

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.MikoriButton
import com.mikori.parent.core.ui.components.MikoriTextButton
import com.mikori.parent.core.ui.components.MikoriTextField
import com.mikori.parent.core.ui.theme.Spacing

@Composable
fun RegisterScreen(
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BrandHeader()
        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = "Crea tu cuenta",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(Spacing.lg))

        MikoriTextField(
            value = name,
            onValueChange = { name = it; viewModel.clearMessages() },
            label = "Tu nombre",
        )
        Spacer(Modifier.height(Spacing.md))
        MikoriTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = "Correo",
            keyboardType = KeyboardType.Email,
        )
        Spacer(Modifier.height(Spacing.md))
        MikoriTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearMessages() },
            label = "Contraseña (mín. 8)",
            isPassword = true,
            imeAction = ImeAction.Done,
        )

        state.error?.let {
            Spacer(Modifier.height(Spacing.md))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(Spacing.xl))
        MikoriButton(
            text = "Crear cuenta",
            onClick = { viewModel.register(name, email, password) },
            loading = state.loading,
        )
        Spacer(Modifier.height(Spacing.lg))
        MikoriTextButton(text = "Ya tengo cuenta", onClick = onGoToLogin)
    }
}
