package com.growthaxys.churchmanagements.data.repo

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder

/** Shared helpers for repositories. church_id comes from the signed-in profile (also enforced by RLS + DB trigger). */
abstract class BaseRepo(protected val client: SupabaseClient, private val auth: AuthRepository) {
    protected fun churchId(): String? = (auth.state.value as? SessionState.SignedIn)?.profile?.churchId
    protected fun userId(): String? = (auth.state.value as? SessionState.SignedIn)?.profile?.id
    protected fun table(name: String): PostgrestQueryBuilder = client.from(name)
}

internal val ASC = Order.ASCENDING
internal val DESC = Order.DESCENDING
internal fun raw(columns: String) = Columns.raw(columns)
@Suppress("unused") internal typealias Filter = PostgrestFilterBuilder
