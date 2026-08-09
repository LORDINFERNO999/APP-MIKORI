package com.mikori.parent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DayLimitDto(
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("day_name") val dayName: String? = null,
    @SerialName("daily_limit_minutes") val dailyLimitMinutes: Int? = null,
)

@Serializable
data class LimitsData(
    @SerialName("child_id") val childId: Long,
    val days: List<DayLimitDto> = emptyList(),
)

/** Cuerpo para definir límites: mismo para todos (all) o por día (days). */
@Serializable
data class SetLimitsRequest(
    val all: Int? = null,
    val days: List<DayLimitEntry>? = null,
)

@Serializable
data class DayLimitEntry(
    @SerialName("day_of_week") val dayOfWeek: Int,
    val minutes: Int,
)
