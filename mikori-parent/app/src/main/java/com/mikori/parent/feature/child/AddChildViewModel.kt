package com.mikori.parent.feature.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddChildUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class AddChildViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddChildUiState())
    val state: StateFlow<AddChildUiState> = _state.asStateFlow()

    fun create(name: String, birthdate: String?) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "Escribe el nombre.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val bd = birthdate?.takeIf { it.isNotBlank() }
            when (val r = familyRepository.createChild(name.trim(), bd, null)) {
                is MikoriResult.Success -> _state.update { it.copy(loading = false, done = true) }
                is MikoriResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }
}
