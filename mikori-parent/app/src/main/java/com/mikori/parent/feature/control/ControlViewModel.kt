package com.mikori.parent.feature.control

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.repository.ControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ControlUiState(
    val working: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val pauseUntil: String? = null,
)

@HiltViewModel
class ControlViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val control: ControlRepository,
) : ViewModel() {

    val childId: Long = savedStateHandle.get<String>("childId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(ControlUiState())
    val state: StateFlow<ControlUiState> = _state.asStateFlow()

    fun startPause(minutes: Int) {
        _state.update { it.copy(working = true, error = null, message = null) }
        viewModelScope.launch {
            when (val r = control.startPause(childId, minutes)) {
                is MikoriResult.Success -> _state.update {
                    it.copy(working = false, pauseUntil = r.data.pauseUntil, message = "Pausa activada.")
                }
                is MikoriResult.Error -> _state.update { it.copy(working = false, error = r.message) }
            }
        }
    }

    fun cancelPause() {
        _state.update { it.copy(working = true, error = null, message = null) }
        viewModelScope.launch {
            when (val r = control.cancelPause(childId)) {
                is MikoriResult.Success -> _state.update { it.copy(working = false, pauseUntil = null, message = "Pausa cancelada.") }
                is MikoriResult.Error -> _state.update { it.copy(working = false, error = r.message) }
            }
        }
    }
}
