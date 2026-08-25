package com.mikori.parent.feature.control

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.remote.dto.ScheduleDto
import com.mikori.parent.data.remote.dto.ScheduleRequest
import com.mikori.parent.data.repository.ControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SchedulesUiState(
    val loading: Boolean = true,
    val working: Boolean = false,
    val error: String? = null,
    val schedules: List<ScheduleDto> = emptyList(),
)

@HiltViewModel
class SchedulesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val control: ControlRepository,
) : ViewModel() {

    private val childId: Long = savedStateHandle.get<String>("childId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(SchedulesUiState())
    val state: StateFlow<SchedulesUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = control.schedules(childId)) {
                is MikoriResult.Success -> _state.update { it.copy(loading = false, schedules = r.data) }
                is MikoriResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }

    fun addPreset(type: String) {
        val req = when (type) {
            "night" -> ScheduleRequest("Horario nocturno", "night", "22:00", "07:00", 127)
            "school" -> ScheduleRequest("Horario escolar", "school", "08:00", "14:00", 0b0011111) // L-V
            else -> return
        }
        create(req)
    }

    fun addCustom(name: String, start: String, end: String, daysMask: Int) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "Escribe un nombre.") }
            return
        }
        create(ScheduleRequest(name.trim(), "custom", start, end, daysMask))
    }

    private fun create(req: ScheduleRequest) {
        _state.update { it.copy(working = true, error = null) }
        viewModelScope.launch {
            when (val r = control.createSchedule(childId, req)) {
                is MikoriResult.Success -> { _state.update { it.copy(working = false) }; load() }
                is MikoriResult.Error -> _state.update { it.copy(working = false, error = r.message) }
            }
        }
    }

    fun delete(sid: Long) {
        viewModelScope.launch {
            when (val r = control.deleteSchedule(childId, sid)) {
                is MikoriResult.Success -> load()
                is MikoriResult.Error -> _state.update { it.copy(error = r.message) }
            }
        }
    }
}
