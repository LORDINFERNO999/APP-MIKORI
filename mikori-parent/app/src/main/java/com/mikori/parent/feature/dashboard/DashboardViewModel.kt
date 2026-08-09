package com.mikori.parent.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.repository.AuthRepository
import com.mikori.parent.data.repository.FamilyRepository
import com.mikori.parent.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardChildUi(
    val id: Long,
    val name: String,
    val online: Boolean,
    val usedSeconds: Int,
    val limitMinutes: Int?,
    val remainingSeconds: Int?,
)

data class DashboardUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val children: List<DashboardChildUi> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val statsRepository: StatsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        observeUser()
        load()
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.userName.collect { name ->
                _state.update { it.copy(userName = name) }
            }
        }
    }

    fun load(isRefresh: Boolean = false) {
        _state.update { it.copy(loading = !isRefresh, refreshing = isRefresh, error = null) }
        viewModelScope.launch {
            when (val childrenResult = familyRepository.children()) {
                is MikoriResult.Error ->
                    _state.update { it.copy(loading = false, refreshing = false, error = childrenResult.message) }

                is MikoriResult.Success -> {
                    val uiChildren = coroutineScope {
                        childrenResult.data.map { child ->
                            async { buildChildUi(child.id, child.name) }
                        }.awaitAll()
                    }
                    _state.update {
                        it.copy(loading = false, refreshing = false, children = uiChildren)
                    }
                }
            }
        }
    }

    private suspend fun buildChildUi(id: Long, name: String): DashboardChildUi {
        val detail = familyRepository.child(id)
        val stats = statsRepository.today(id)

        val online = (detail as? MikoriResult.Success)?.data?.devices?.any { it.status == "online" } ?: false
        val used = (stats as? MikoriResult.Success)?.data?.totalSeconds ?: 0
        val limit = (stats as? MikoriResult.Success)?.data?.limitMinutes
        val remaining = (stats as? MikoriResult.Success)?.data?.remainingSeconds

        return DashboardChildUi(
            id = id,
            name = name,
            online = online,
            usedSeconds = used,
            limitMinutes = limit,
            remainingSeconds = remaining,
        )
    }
}
