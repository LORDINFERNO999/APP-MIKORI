package com.mikori.parent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUsageDto(
    @SerialName("package_name") val packageName: String,
    @SerialName("app_label") val appLabel: String? = null,
    val category: String? = null,
    val seconds: Int,
)

@Serializable
data class StatsTodayData(
    val date: String,
    @SerialName("total_seconds") val totalSeconds: Int,
    @SerialName("limit_minutes") val limitMinutes: Int? = null,
    @SerialName("remaining_seconds") val remainingSeconds: Int? = null,
    @SerialName("limit_reached") val limitReached: Boolean = false,
    @SerialName("top_apps") val topApps: List<AppUsageDto> = emptyList(),
)

@Serializable
data class DayTotalDto(
    val date: String,
    @SerialName("total_seconds") val totalSeconds: Int,
)

@Serializable
data class StatsWeekData(
    val from: String,
    val to: String,
    val days: List<DayTotalDto> = emptyList(),
)

@Serializable
data class StatsAppsData(
    val from: String,
    val to: String,
    val apps: List<AppUsageDto> = emptyList(),
)
