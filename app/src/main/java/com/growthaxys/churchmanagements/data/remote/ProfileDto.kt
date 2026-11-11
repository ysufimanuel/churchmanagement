package com.growthaxys.churchmanagements.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val nama: String,
    val username: String,
    val email: String? = null,
    val role: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val status: String,
    @SerialName("church_id") val churchId: String
)
