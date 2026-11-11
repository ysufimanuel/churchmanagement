package com.growthaxys.churchmanagements.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growthaxys.churchmanagements.data.repository.AuthRepository
import com.growthaxys.churchmanagements.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init { restore() }

    private fun restore() = viewModelScope.launch {
        _state.value = AuthUiState(loading = true)
        runCatching { repository.restoreSession() }
            .onSuccess { _state.value = AuthUiState(loading = false, profile = it) }
            .onFailure { _state.value = AuthUiState(loading = false, error = it.message) }
    }

    fun signIn(identifier: String, password: String) = viewModelScope.launch {
        if (identifier.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Username/email dan password wajib diisi.")
            return@launch
        }
        if (!identifier.contains("@")) {
            _state.value = _state.value.copy(
                error = "Login username memerlukan endpoint lookup aman dari backend. Untuk sementara gunakan email akun."
            )
            return@launch
        }
        _state.value = AuthUiState(loading = true)
        runCatching { repository.signIn(identifier, password) }
            .onSuccess { _state.value = AuthUiState(loading = false, profile = it) }
            .onFailure { _state.value = AuthUiState(loading = false, error = it.message ?: "Login gagal.") }
    }

    fun signOut() = viewModelScope.launch {
        runCatching { repository.signOut() }
        _state.value = AuthUiState(loading = false)
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
