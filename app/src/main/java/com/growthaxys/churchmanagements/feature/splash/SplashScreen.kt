package com.growthaxys.churchmanagements.feature.splash

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.growthaxys.churchmanagements.R
import kotlinx.coroutines.delay

/** Same colour the web APK uses (`capacitor.config.ts` backgroundColor). */
val SplashBg = Color(0xFF040710)

/**
 * Plays the web app's splash (`assets/GetChurchly_Splash_4.8s.mp4`, 720×1280, 4.8s) full-bleed (cover),
 * then calls [onDone]. Falls back to [onDone] on playback error or after a 6.5s safety timeout.
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val done by rememberUpdatedState(onDone)
    LaunchedEffect(Unit) { delay(6500); done() }

    BoxWithConstraints(Modifier.fillMaxSize().background(SplashBg), contentAlignment = Alignment.Center) {
        val videoAspect = 720f / 1280f
        // cover: scale the 9:16 frame so it fills both dimensions, cropping the overflow
        val h = if (maxWidth / maxHeight > videoAspect) maxWidth / videoAspect else maxHeight
        val w = h * videoAspect
        AndroidView(
            modifier = Modifier.requiredSize(w, h),
            factory = {
                VideoView(it).apply {
                    setVideoURI(Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash}"))
                    setOnPreparedListener { mp: MediaPlayer -> mp.setVolume(0f, 0f); mp.isLooping = false }
                    setOnCompletionListener { done() }
                    setOnErrorListener { _, _, _ -> done(); true }
                    start()
                }
            },
        )
    }
}
