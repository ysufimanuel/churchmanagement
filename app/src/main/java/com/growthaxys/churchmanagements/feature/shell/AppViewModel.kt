package com.growthaxys.churchmanagements.feature.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growthaxys.churchmanagements.core.Lang
import com.growthaxys.churchmanagements.core.Prefs
import com.growthaxys.churchmanagements.data.repo.AuthRepository
import com.growthaxys.churchmanagements.data.repo.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val prefs: Prefs,
) : ViewModel() {
    val session: StateFlow<SessionState> = auth.state
    val lang: StateFlow<Lang> = prefs.lang.stateIn(viewModelScope, SharingStarted.Eagerly, Lang.EN)

    init { viewModelScope.launch { auth.observe() } }

    fun setLang(l: Lang) { viewModelScope.launch { prefs.setLang(l) } }
    fun logout() { viewModelScope.launch { auth.signOut() } }
}
