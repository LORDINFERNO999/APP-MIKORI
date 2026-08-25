package com.mikori.kids.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.kids.core.MikoriResult
import com.mikori.kids.data.remote.dto.AppUsageDto
import com.mikori.kids.data.repository.UsageRepository
import com.mikori.kids.guard.GuardService
import com.mikori.kids.guard.OverlayPermission
import com.mikori.kids.usage.UsageAccess
import com.mikori.kids.work.WorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val usageAccessGranted: Boolean = true,
    val overlayGranted: Boolean = true,
    val childName: String? = null,
    val usedSeconds: Int = 0,
    val limitMinutes: Int? = null,
    val remainingSeconds: Int? = null,
    val limitReached: Boolean = false,
    val topApps: List<AppUsageDto> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageRepository: UsageRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        WorkScheduler.schedulePeriodic(context)
        refresh()
    }

    /** Re-verifica permisos, fuerza recolección, arranca el guardián y recarga el resumen. */
    fun refresh() {
        val granted = UsageAccess.isGranted(context)
        val overlay = OverlayPermission.isGranted(context)
        _state.update { it.copy(loading = true, usageAccessGranted = granted, overlayGranted = overlay) }
        if (granted) WorkScheduler.runOnce(context)
        // Con acceso al uso + overlay, arranca el servicio de vigilancia (V2).
        if (granted && overlay) GuardService.start(context)

        viewModelScope.launch {
            when (val r = usageRepository.today()) {
                is MikoriResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        childName = r.data.childName,
                        usedSeconds = r.data.totalSeconds,
                        limitMinutes = r.data.limitMinutes,
                        remainingSeconds = r.data.remainingSeconds,
                        limitReached = r.data.limitReached,
                        topApps = r.data.topApps,
                    )
                }
                is MikoriResult.Error -> _state.update { it.copy(loading = false, error = r.message) }
            }
        }
    }
}
