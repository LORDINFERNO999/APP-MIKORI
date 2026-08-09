package com.mikori.parent.feature.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.core.ui.components.DayBar
import com.mikori.parent.data.remote.dto.AppUsageDto
import com.mikori.parent.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val week: List<DayBar> = emptyList(),
    val totalWeekSeconds: Int = 0,
    val averageDailySeconds: Int = 0,
    val apps: List<AppUsageDto> = emptyList(),
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val statsRepository: StatsRepository,
) : ViewModel() {

    private val childId: Long = savedStateHandle.get<String>("childId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val week = statsRepository.week(childId)
            val apps = statsRepository.apps(childId)

            if (week is MikoriResult.Error) {
                _state.update { it.copy(loading = false, error = week.message) }
                return@launch
            }

            val weekData = (week as MikoriResult.Success).data
            val today = LocalDate.now().toString()
            val bars = weekData.days.map { d ->
                DayBar(
                    label = weekdayLabel(d.date),
                    seconds = d.totalSeconds,
                    highlighted = d.date == today,
                )
            }
            val total = weekData.days.sumOf { it.totalSeconds }
            val avg = if (weekData.days.isNotEmpty()) total / weekData.days.size else 0

            _state.update {
                it.copy(
                    loading = false,
                    week = bars,
                    totalWeekSeconds = total,
                    averageDailySeconds = avg,
                    apps = (apps as? MikoriResult.Success)?.data?.apps ?: emptyList(),
                )
            }
        }
    }

    private fun weekdayLabel(date: String): String = try {
        when (LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).dayOfWeek.value) {
            1 -> "L"; 2 -> "M"; 3 -> "X"; 4 -> "J"; 5 -> "V"; 6 -> "S"; else -> "D"
        }
    } catch (_: Exception) {
        "?"
    }
}
