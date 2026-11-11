package com.growthaxys.churchmanagements.feature.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.growthaxys.churchmanagements.core.designsystem.Background
import com.growthaxys.churchmanagements.core.designsystem.Cyan
import com.growthaxys.churchmanagements.feature.auth.AuthViewModel
import com.growthaxys.churchmanagements.feature.auth.LoginScreen
import com.growthaxys.churchmanagements.feature.dashboard.DashboardScreen

@Composable
fun ChurchManagementRoot(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Box(Modifier.fillMaxSize().background(Background)) {
        when {
            state.loading -> Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Cyan)
                Spacer(Modifier.height(12.dp))
                Text("Memuat Church Managements")
            }
            state.profile == null -> LoginScreen(
                loading = false,
                error = state.error,
                onLogin = viewModel::signIn,
                onErrorDismiss = viewModel::clearError
            )
            else -> DashboardScreen(profile = state.profile!!, onSignOut = viewModel::signOut)
        }
    }
}
