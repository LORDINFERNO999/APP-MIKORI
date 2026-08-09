package com.mikori.parent.feature.child

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikori.parent.core.ui.components.KoriMascot
import com.mikori.parent.core.ui.components.KoriMood
import com.mikori.parent.core.ui.components.MikoriButton
import com.mikori.parent.core.ui.components.MikoriTextField
import com.mikori.parent.core.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: AddChildViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var birthdate by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.done) { if (state.done) onCreated() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Añadir hijo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(Modifier.height(Spacing.lg))
            KoriMascot(size = 96.dp, mood = KoriMood.HAPPY)
            Spacer(Modifier.height(Spacing.xl))

            MikoriTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre del hijo",
            )
            Spacer(Modifier.height(Spacing.md))
            MikoriTextField(
                value = birthdate,
                onValueChange = { birthdate = it },
                label = "Fecha de nacimiento (opcional, AAAA-MM-DD)",
            )

            state.error?.let {
                Spacer(Modifier.height(Spacing.md))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(Spacing.xl))
            MikoriButton(
                text = "Guardar",
                onClick = { viewModel.create(name, birthdate) },
                loading = state.loading,
            )
        }
    }
}
