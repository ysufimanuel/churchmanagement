package com.growthaxys.churchmanagements.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val email: String?,
    val role: AppRole,
    val avatarUrl: String?,
    val churchId: String
)

enum class AppRole { USER, ADMIN, SUPERADMIN;
    companion object {
        fun from(value: String): AppRole = entries.firstOrNull { it.name.equals(value, true) } ?: USER
    }
}
