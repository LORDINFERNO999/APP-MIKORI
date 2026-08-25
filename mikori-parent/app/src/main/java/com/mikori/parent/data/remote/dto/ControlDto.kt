package com.mikori.parent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Catálogo de apps del hijo ──
@Serializable
data class AppCatalogDto(
    @SerialName("package_name") val packageName: String,
    @SerialName("app_label") val appLabel: String? = null,
    val category: String? = null,
)

// ── Reglas por app ──
@Serializable
data class AppRuleDto(
    @SerialName("package_name") val packageName: String,
    @SerialName("app_label") val appLabel: String? = null,
    val category: String? = null,
    @SerialName("max_minutes") val maxMinutes: Int? = null,
    @SerialName("is_blocked") val isBlocked: Boolean = false,
)

@Serializable
data class AppRuleEntry(
    val `package`: String,
    val label: String? = null,
    @SerialName("max_minutes") val maxMinutes: Int? = null,
    @SerialName("is_blocked") val isBlocked: Boolean = false,
)

@Serializable
data class SetAppRulesRequest(
    val rules: List<AppRuleEntry>,
)

// ── Horarios ──
@Serializable
data class ScheduleDto(
    val id: Long,
    val name: String,
    val type: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("days_mask") val daysMask: Int = 127,
    val active: Boolean = true,
)

@Serializable
data class ScheduleRequest(
    val name: String,
    val type: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("days_mask") val daysMask: Int = 127,
    val active: Boolean = true,
)

// ── Pausa ──
@Serializable
data class PauseRequest(
    val minutes: Int? = null,
    val until: String? = null,
)

@Serializable
data class PauseData(
    @SerialName("pause_until") val pauseUntil: String? = null,
)
