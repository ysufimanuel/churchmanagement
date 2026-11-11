package com.growthaxys.churchmanagements.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {
    fun create(url: String, publishableKey: String) = createSupabaseClient(
        supabaseUrl = url,
        supabaseKey = publishableKey
    ) {
        install(GoTrue)
        install(Postgrest)
    }
}
