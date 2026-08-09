package com.mikori.parent.feature.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.remote.dto.AppUsageDto
import com.mikori.parent.data.repository.FamilyRepository
import com.mikori.parent.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildDetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val name: String = "",
    val online: Boolean = false,
    val usedSeconds: Int = 0,
    val limitMinutes: Int? = null,
    val remainingSeconds: Int? = null,
    val limitReached: Boolean = false,
    val topApps: List<AppUsageDto> = emptyList(),
    val deleted: Boolean = false,
)

@HiltViewModel
class ChildDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val familyRepository: FamilyRepository,
    private val statsRepository: StatsRepository,
) : ViewModel() {

    val childId: Long = savedStateHandle.get<String>("childId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(ChildDetailUiState())
    val state: StateFlow<ChildDetailUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val detail = familyRepository.child(childId)
            val stats = statsRepository.today(childId)

            if (detail is MikoriResult.Error) {
                _state.update { it.copy(loading = false, error = detail.message) }
                return@launch
            }
            val child = (detail as MikoriResult.Success).data
            val online = child.devices?.any { it.status == "online" } ?: false

            val today = (stats as? MikoriResult.Success)?.data
            _state.update {
                it.copy(
                    loading = false,
                    name = child.name,
                    online = online,
                    usedSeconds = today?.totalSeconds ?: 0,
                    limitMinutes = today?.limitMinutes,
                    remainingSeconds = today?.remainingSeconds,
                    limitReached = today?.limitReached ?: false,
                    topApps = today?.topApps ?: emptyList(),
                )
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (val r = familyRepository.deleteChild(childId)) {
                is MikoriResult.Success -> _state.update { it.copy(deleted = true) }
                is MikoriResult.Error -> _state.update { it.copy(error = r.message) }
            }
        }
    }
}
