package com.mikori.parent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkCodeData(
    val code: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("expires_in") val expiresIn: Long,
)

@Serializable
data class LinkStatusData(
    val status: String, // none | pending | linked | expired
    val code: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("linked_device_id") val linkedDeviceId: Long? = null,
    @SerialName("linked_at") val linkedAt: String? = null,
)
