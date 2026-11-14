package com.growthaxys.churchmanagements.feature.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.designsystem.Glass
import com.growthaxys.churchmanagements.designsystem.Tokens
import kotlinx.coroutines.delay

/** Replaces web `sonner` toasts. */
@Composable
fun ToastHost(msg: ToastMsg?, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    LaunchedEffect(msg) { if (msg != null) { delay(3000); onDismiss() } }
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(msg != null, enter = fadeIn() + slideInVertically { -it }, exit = fadeOut() + slideOutVertically { -it }) {
            if (msg != null) Glass(strong = true) {
                Text(
                    if (msg.key != null) s.t(msg.key) else s.tm(msg.raw),
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp), fontSize = 14.sp,
                    color = if (msg.isError) Tokens.Red400 else Color.White,
                )
            }
        }
    }
}
