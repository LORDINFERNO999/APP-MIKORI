package com.mikori.parent.feature.control

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikori.parent.core.MikoriResult
import com.mikori.parent.data.remote.dto.AppRuleEntry
import com.mikori.parent.data.repository.ControlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppRuleUi(
    val packageName: String,
    val label: String,
    val category: String?,
    val isBlocked: Boolean,
    val maxMinutes: Int, // 0 = sin límite
)

data class AppRulesUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val items: List<AppRuleUi> = emptyList(),
)

@HiltViewModel
class AppRulesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val control: ControlRepository,
) : ViewModel() {

    private val childId: Long = savedStateHandle.get<String>("childId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(AppRulesUiState())
    val state: StateFlow<AppRulesUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val apps = control.childApps(childId)
            val rules = control.appRules(childId)

            if (apps is MikoriResult.Error) {
                _state.update { it.copy(loading = false, error = apps.message) }
                return@launch
            }
            val catalog = (apps as MikoriResult.Success).data
            val ruleList = (rules as? MikoriResult.Success)?.data ?: emptyList()
            val ruleByPkg = ruleList.associateBy { it.packageName }

            val merged = LinkedHashMap<String, AppRuleUi>()
            catalog.forEach { a ->
                val r = ruleByPkg[a.packageName]
                merged[a.packageName] = AppRuleUi(
                    packageName = a.packageName,
                    label = a.appLabel ?: a.packageName,
                    category = a.category,
                    isBlocked = r?.isBlocked ?: false,
                    maxMinutes = r?.maxMinutes ?: 0,
                )
            }
            // Reglas de apps que aún no aparecen en el catálogo (p. ej. bloqueadas sin uso)
            ruleList.forEach { r ->
                if (!merged.containsKey(r.packageName)) {
                    merged[r.packageName] = AppRuleUi(
                        packageName = r.packageName,
                        label = r.appLabel ?: r.packageName,
                        category = r.category,
                        isBlocked = r.isBlocked,
                        maxMinutes = r.maxMinutes ?: 0,
                    )
                }
            }

            _state.update { it.copy(loading = false, items = merged.values.toList()) }
        }
    }

    fun setBlocked(pkg: String, blocked: Boolean) = update(pkg) { it.copy(isBlocked = blocked) }
    fun setLimit(pkg: String, minutes: Int) = update(pkg) { it.copy(maxMinutes = minutes.coerceIn(0, 1440)) }

    private fun update(pkg: String, transform: (AppRuleUi) -> AppRuleUi) {
        _state.update { s ->
            s.copy(saved = false, items = s.items.map { if (it.packageName == pkg) transform(it) else it })
        }
    }

    fun save() {
        val entries = _state.value.items
            .filter { it.isBlocked || it.maxMinutes > 0 }
            .map {
                AppRuleEntry(
                    `package` = it.packageName,
                    label = it.label,
                    maxMinutes = if (it.maxMinutes > 0) it.maxMinutes else null,
                    isBlocked = it.isBlocked,
                )
            }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val r = control.setAppRules(childId, entries)) {
                is MikoriResult.Success -> _state.update { it.copy(saving = false, saved = true) }
                is MikoriResult.Error -> _state.update { it.copy(saving = false, error = r.message) }
            }
        }
    }
}
