package com.growthaxys.churchmanagements.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.data.repo.MobileAdminRepository
import com.growthaxys.churchmanagements.designsystem.AppButton
import com.growthaxys.churchmanagements.designsystem.AppTextField
import com.growthaxys.churchmanagements.designsystem.LabeledField
import com.growthaxys.churchmanagements.designsystem.Tokens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(val loading: Boolean = false, val error: String? = null, val success: Boolean = false)

@HiltViewModel
class RegisterViewModel @Inject constructor(private val api: MobileAdminRepository) : ViewModel() {
    private val _ui = MutableStateFlow(RegisterUiState())
    val ui: StateFlow<RegisterUiState> = _ui.asStateFlow()

    fun submit(
        churchName: String, address: String, city: String, phone: String, churchEmail: String,
        adminName: String, username: String, email: String, password: String,
    ) {
        if (churchName.isBlank() || adminName.isBlank() || username.isBlank() || email.isBlank() || password.length < 6) {
            _ui.value = RegisterUiState(error = "Lengkapi data wajib. Password minimal 6 karakter.")
            return
        }
        _ui.value = RegisterUiState(loading = true)
        viewModelScope.launch {
            val result = api.registerChurch(churchName, address, city, phone, churchEmail, adminName, username, email, password)
            _ui.value = if (result.ok) RegisterUiState(success = true) else RegisterUiState(error = result.error ?: "Registrasi gagal.")
        }
    }
}

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    vm: RegisterViewModel = hiltViewModel(),
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var churchName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var churchEmail by remember { mutableStateOf("") }
    var adminName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.fillMaxWidth().background(Color(0x0EFFFFFF), RoundedCornerShape(28.dp))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(28.dp)).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppButton("Kembali ke Login", onBack, variant = com.growthaxys.churchmanagements.designsystem.BtnVariant.Ghost,
                icon = Icons.Outlined.ArrowBack, height = 40.dp)
            Icon(Icons.Outlined.Church, null, Modifier.align(Alignment.CenterHorizontally).height(42.dp), tint = Tokens.Primary)
            Text("Daftarkan Gereja", Modifier.fillMaxWidth(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
            Text("Buat tenant gereja dan akun Superadmin", Modifier.fillMaxWidth(), fontSize = 12.sp, color = Tokens.MutedForeground, textAlign = TextAlign.Center)

            Text("DATA GEREJA", fontSize = 11.sp, color = Tokens.Primary, fontWeight = FontWeight.Bold)
            LabeledField("Nama Gereja") { AppTextField(churchName, { churchName = it }, placeholder = "Gereja Kristen Indonesia") }
            LabeledField("Alamat") { AppTextField(address, { address = it }, placeholder = "Alamat gereja") }
            LabeledField("Kota") { AppTextField(city, { city = it }, placeholder = "Kota") }
            LabeledField("Telepon") { AppTextField(phone, { phone = it }, placeholder = "Nomor telepon") }
            LabeledField("Email Gereja") { AppTextField(churchEmail, { churchEmail = it }, placeholder = "info@gereja.com") }

            Spacer(Modifier.height(4.dp))
            Text("AKUN SUPERADMIN", fontSize = 11.sp, color = Tokens.Primary, fontWeight = FontWeight.Bold)
            LabeledField("Nama Admin") { AppTextField(adminName, { adminName = it }, placeholder = "Nama lengkap") }
            LabeledField("Username") { AppTextField(username, { username = it }, placeholder = "username_admin") }
            LabeledField("Email Login") { AppTextField(email, { email = it }, placeholder = "admin@gereja.com") }
            LabeledField("Password") { AppTextField(password, { password = it }, placeholder = "Minimal 6 karakter", password = true) }

            ui.error?.let {
                Text(it, Modifier.fillMaxWidth().background(Color(0x26EF4444), RoundedCornerShape(12.dp)).padding(12.dp), color = Color(0xFFFCA5A5), fontSize = 12.sp)
            }
            if (ui.success) {
                Text("Gereja berhasil didaftarkan. Silakan masuk menggunakan akun Superadmin yang baru dibuat.", Modifier.fillMaxWidth().background(Color(0x1A10B981), RoundedCornerShape(12.dp)).padding(12.dp), color = Color(0xFF6EE7B7), fontSize = 12.sp)
                AppButton("Kembali ke Login", onBack, Modifier.fillMaxWidth(), icon = Icons.Outlined.ArrowBack)
            } else {
                AppButton("Daftarkan Gereja", {
                    vm.submit(churchName, address, city, phone, churchEmail, adminName, username, email, password)
                }, Modifier.fillMaxWidth(), icon = Icons.Outlined.PersonAdd, loading = ui.loading, enabled = !ui.loading)
            }
        }
    }
}
