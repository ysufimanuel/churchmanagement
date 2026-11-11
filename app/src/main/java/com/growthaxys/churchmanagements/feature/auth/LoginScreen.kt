package com.growthaxys.churchmanagements.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthaxys.churchmanagements.core.designsystem.*

@Composable
fun LoginScreen(
    loading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onErrorDismiss: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF14323B), Background), radius = 1000f)
        ).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 420.dp)
                .background(Color(0xE610131B), RoundedCornerShape(28.dp))
                .border(1.dp, Border, RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(64.dp).background(Cyan.copy(alpha = .12f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) { Text("✦", color = Cyan, fontSize = 30.sp) }
            Spacer(Modifier.height(20.dp))
            Text("GROWTH AXYS", color = Cyan.copy(alpha = .85f), fontSize = 10.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(6.dp))
            Text("Church Managements", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("Gereja Digital", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it; if (error != null) onErrorDismiss() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username atau email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = loginFieldColors()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; if (error != null) onErrorDismiss() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = loginFieldColors()
            )
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onLogin(identifier, password) },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Background),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Masuk", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(18.dp))
            Text("Created by Growth Axys", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Cyan,
    unfocusedBorderColor = Border,
    focusedLabelColor = Cyan,
    cursorColor = Cyan,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
