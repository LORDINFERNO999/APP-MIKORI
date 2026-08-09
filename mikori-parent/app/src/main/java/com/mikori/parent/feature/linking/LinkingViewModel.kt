package com.mikori.parent.feature.linking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkingUiState(
    val generating: Boolean = true,
    val error: String? = null,
    val code: String? = null,
    val secondsLeft: Int = 0,
    val linked: Boolean = false,
    val expired: Boolean = false,
)

@HiltViewModel
class LinkingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val familyRepository: FamilyRepository,
) : ViewModel() {

    private val childId: Long = savedStateHandle.get<String>("childId")?.toLongOrNull() ?: 0L
    private var pollJob: Job? = null

    private val _state = MutableStateFlow(LinkingUiState())
    val state: StateFlow<LinkingUiState> = _state.asStateFlow()

    init { generate() }

    fun generate() {
        pollJob?.cancel()
        _state.update { it.copy(generating = true, error = null, linked = false, expired = false, code = null) }
        viewModelScope.launch {
            when (val r = familyRepository.generateLinkCode(childId)) {
                is MikoriResult.Error -> _state.update { it.copy(generating = false, error = r.message) }
                is MikoriResult.Success -> {
                    _state.update {
                        it.copy(generating = false, code = r.data.code, secondsLeft = r.data.expiresIn.toInt())
                    }
                    startPolling()
                }
            }
        }
    }

    private fun startPolling() {
        pollJob = viewModelScope.launch {
            var tick = 0
            while (isActive) {
                delay(1_000)
                tick++
                val remaining = (_state.value.secondsLeft - 1).coerceAtLeast(0)
                _state.update { it.copy(secondsLeft = remaining) }

                if (remaining <= 0) {
                    _state.update { it.copy(expired = true) }
                    break
                }
                // Consulta el estado cada 3 segundos.
                if (tick % 3 == 0) {
                    when (val r = familyRepository.linkStatus(childId)) {
                        is MikoriResult.Success -> {
                            when (r.data.status) {
                                "linked" -> {
                                    _state.update { it.copy(linked = true) }
                                    return@launch
                                }
                                "expired" -> {
                                    _state.update { it.copy(expired = true) }
                                    return@launch
                                }
                            }
                        }
                        is MikoriResult.Error -> { /* reintenta en el siguiente ciclo */ }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
