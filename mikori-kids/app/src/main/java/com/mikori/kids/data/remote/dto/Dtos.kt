package com.mikori.kids.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Envelope<T>(
    val data: T? = null,
    val error: ApiError? = null,
)

@Serializable
data class ApiError(val code: String, val message: String)

// ── Vinculación ──
@Serializable
data class RedeemRequest(
    val code: String,
    @SerialName("device_uid") val deviceUid: String,
    val model: String? = null,
    @SerialName("android_version") val androidVersion: String? = null,
    @SerialName("fcm_token") val fcmToken: String? = null,
)

@Serializable
data class RedeemData(
    @SerialName("device_id") val deviceId: Long,
    @SerialName("child_id") val childId: Long,
    @SerialName("device_token") val deviceToken: String,
)

// ── Ingesta de uso ──
@Serializable
data class UsageItem(
    val `package`: String,
    val label: String? = null,
    val category: String? = null,
    val date: String,
    val seconds: Int,
)

@Serializable
data class UsageUploadRequest(
    val items: List<UsageItem>,
)

@Serializable
data class AcceptedData(val accepted: Int)

// ── Resumen de hoy (device-auth) ──
@Serializable
data class AppUsageDto(
    @SerialName("package_name") val packageName: String,
    @SerialName("app_label") val appLabel: String? = null,
    val category: String? = null,
    val seconds: Int,
)

@Serializable
data class TodaySummaryData(
    val date: String,
    @SerialName("total_seconds") val totalSeconds: Int,
    @SerialName("limit_minutes") val limitMinutes: Int? = null,
    @SerialName("remaining_seconds") val remainingSeconds: Int? = null,
    @SerialName("limit_reached") val limitReached: Boolean = false,
    @SerialName("child_name") val childName: String? = null,
    @SerialName("top_apps") val topApps: List<AppUsageDto> = emptyList(),
)

@Serializable
data class MessageData(val message: String)

// ── V2: política de enforcement ──
@Serializable
data class AppLimitDto(
    val `package`: String,
    @SerialName("max_minutes") val maxMinutes: Int,
    @SerialName("used_seconds") val usedSeconds: Int,
    val exceeded: Boolean,
)

@Serializable
data class ActiveScheduleDto(
    val name: String,
    val type: String,
)

@Serializable
data class PolicyData(
    @SerialName("daily_limit_reached") val dailyLimitReached: Boolean = false,
    @SerialName("remaining_seconds") val remainingSeconds: Int? = null,
    @SerialName("blocked_packages") val blockedPackages: List<String> = emptyList(),
    @SerialName("app_limits") val appLimits: List<AppLimitDto> = emptyList(),
    @SerialName("active_schedule") val activeSchedule: ActiveScheduleDto? = null,
    @SerialName("pause_until") val pauseUntil: String? = null,
    @SerialName("block_all") val blockAll: Boolean = false,
)
