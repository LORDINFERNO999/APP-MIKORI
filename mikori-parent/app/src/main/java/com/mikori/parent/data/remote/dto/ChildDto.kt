package com.mikori.parent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChildDto(
    val id: Long,
    val name: String,
    val birthdate: String? = null,
    val avatar: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val devices: List<DeviceDto>? = null,
)

@Serializable
data class DeviceDto(
    val id: Long,
    val model: String? = null,
    @SerialName("android_version") val androidVersion: String? = null,
    val status: String = "offline",
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
)

@Serializable
data class CreateChildRequest(
    val name: String,
    val birthdate: String? = null,
    val avatar: String? = null,
)
