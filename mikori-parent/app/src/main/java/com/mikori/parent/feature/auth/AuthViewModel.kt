package com.mikori.parent.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun clearMessages() = _state.update { it.copy(error = null, info = null) }

    fun login(email: String, password: String) {
        if (!validate(email, password)) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = authRepository.login(email.trim(), password)) {
                is MikoriResult.Success -> _state.update { it.copy(loading = false) }
                is MikoriResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "Escribe tu nombre.") }
            return
        }
        if (!validate(email, password)) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = authRepository.register(name.trim(), email.trim(), password)) {
                is MikoriResult.Success -> _state.update { it.copy(loading = false) }
                is MikoriResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _state.update { it.copy(error = "Escribe tu correo para recuperar la contraseña.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = authRepository.forgotPassword(email.trim())) {
                is MikoriResult.Success -> _state.update { it.copy(loading = false, info = r.data) }
                is MikoriResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }

    private fun validate(email: String, password: String): Boolean {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _state.update { it.copy(error = "Correo no válido.") }
            return false
        }
        if (password.length < 8) {
            _state.update { it.copy(error = "La contraseña debe tener al menos 8 caracteres.") }
            return false
        }
        return true
    }
}
