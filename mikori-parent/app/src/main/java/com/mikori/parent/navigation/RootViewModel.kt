package com.mikori.parent.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    /** null = cargando, true = sesión activa, false = sin sesión. */
    val isLoggedIn: StateFlow<Boolean?> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
