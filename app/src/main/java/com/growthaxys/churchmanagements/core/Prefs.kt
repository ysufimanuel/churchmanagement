package com.growthaxys.churchmanagements.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.store by preferencesDataStore("prefs")

/** Equivalent of web localStorage `cmsLanguage`. Only non-sensitive UI preferences are stored here. */
@Singleton
class Prefs @Inject constructor(@ApplicationContext private val context: Context) {
    private val langKey = stringPreferencesKey("cmsLanguage")
    val lang: Flow<Lang> = context.store.data.map { Lang.from(it[langKey]) }
    suspend fun setLang(l: Lang) { context.store.edit { it[langKey] = l.code } }
}
