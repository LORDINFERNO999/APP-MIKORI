package com.mikori.parent.feature.limits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.remote.dto.DayLimitEntry
import com.mikori.parent.data.repository.LimitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LimitsUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val sameForAll: Boolean = true,
    val allMinutes: Int = 120,
    val perDayMinutes: Map<Int, Int> = (1..7).associateWith { 120 },
)

@HiltViewModel
class LimitsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val limitsRepository: LimitsRepository,
) : ViewModel() {

    private val childId: Long = savedStateHandle.get<String>("childId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(LimitsUiState())
    val state: StateFlow<LimitsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = limitsRepository.limits(childId)) {
                is MikoriResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
                is MikoriResult.Success -> {
                    val map = (1..7).associateWith { dow ->
                        r.data.days.firstOrNull { it.dayOfWeek == dow }?.dailyLimitMinutes ?: 120
                    }
                    val distinct = map.values.distinct()
                    _state.update {
                        it.copy(
                            loading = false,
                            perDayMinutes = map,
                            allMinutes = distinct.firstOrNull() ?: 120,
                            sameForAll = distinct.size == 1,
                        )
                    }
                }
            }
        }
    }

    fun setSameForAll(value: Boolean) = _state.update { it.copy(sameForAll = value, saved = false) }

    fun setAllMinutes(minutes: Int) = _state.update { it.copy(allMinutes = minutes.coerceIn(0, 1440), saved = false) }

    fun setDayMinutes(day: Int, minutes: Int) = _state.update {
        it.copy(perDayMinutes = it.perDayMinutes.toMutableMap().apply { put(day, minutes.coerceIn(0, 1440)) }, saved = false)
    }

    fun save() {
        val s = _state.value
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = if (s.sameForAll) {
                limitsRepository.setSameForAll(childId, s.allMinutes)
            } else {
                limitsRepository.setPerDay(childId, s.perDayMinutes.map { DayLimitEntry(it.key, it.value) })
            }
            when (result) {
                is MikoriResult.Success -> _state.update { it.copy(saving = false, saved = true) }
                is MikoriResult.Error -> _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }
}
