package com.growthaxys.churchmanagements.data

import com.growthaxys.churchmanagements.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

/**
 * Single Supabase client. Only the publishable/anon key is embedded — authorization is enforced by RLS
 * (multi-tenant via church_id, see docs/PERMISSION_MATRIX.md). NEVER add a service-role key here.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    @Provides
    @Singleton
    fun supabase(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL.ifBlank { "https://invalid.supabase.co" },
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY.ifBlank { "missing-anon-key" },
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}

/** Wraps repository calls; returns the same ActionResult shape as the web server actions. */
data class ActionResult(val ok: Boolean, val error: String? = null) {
    companion object {
        val OK = ActionResult(true)
        fun fail(message: String?) = ActionResult(false, message ?: "Unknown server error")
    }
}

suspend inline fun action(crossinline block: suspend () -> Unit): ActionResult =
    try { block(); ActionResult.OK } catch (e: kotlinx.coroutines.CancellationException) { throw e }
    catch (e: Throwable) { ActionResult.fail(e.message) }
