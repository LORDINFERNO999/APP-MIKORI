package com.mikori.kids.ui.linking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.kids.core.MikoriResult
import com.mikori.kids.data.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkingUiState(
    val loading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LinkingViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LinkingUiState())
    val state: StateFlow<LinkingUiState> = _state.asStateFlow()

    fun clearError() = _state.update { it.copy(error = null) }

    fun redeem(code: String) {
        if (code.isBlank()) {
            _state.update { it.copy(error = "Escribe el código.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = linkRepository.redeem(code)) {
                is MikoriResult.Success -> _state.update { it.copy(loading = false) }
                is MikoriResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }
}
