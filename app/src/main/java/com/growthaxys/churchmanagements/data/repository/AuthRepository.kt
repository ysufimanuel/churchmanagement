package com.growthaxys.churchmanagements.data.repository

import com.growthaxys.churchmanagements.domain.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import javax.inject.Inject

interface AuthRepository {
    suspend fun restoreSession(): UserProfile?
    suspend fun signIn(email: String, password: String): UserProfile
    suspend fun signOut()
}

class SupabaseAuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {
    override suspend fun restoreSession(): UserProfile? {
        val user = supabase.gotrue.currentUserOrNull() ?: return null
        return loadProfile(user.id)
    }

    override suspend fun signIn(email: String, password: String): UserProfile {
        supabase.gotrue.loginWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        val user = supabase.gotrue.currentUserOrNull()
            ?: error("Sesi login tidak ditemukan. Silakan coba lagi.")
        return loadProfile(user.id)
    }

    private suspend fun loadProfile(userId: String): UserProfile {
        val dto = supabase.from("profiles").select(Columns.ALL) {
            filter { eq("id", userId) }
        }.decodeSingle<com.growthaxys.churchmanagements.data.remote.ProfileDto>()
        return UserProfile(
            id = dto.id,
            name = dto.nama,
            username = dto.username,
            email = dto.email,
            role = com.growthaxys.churchmanagements.domain.model.AppRole.from(dto.role),
            avatarUrl = dto.avatarUrl,
            churchId = dto.churchId
        )
    }

    override suspend fun signOut() { supabase.gotrue.logout() }
}
