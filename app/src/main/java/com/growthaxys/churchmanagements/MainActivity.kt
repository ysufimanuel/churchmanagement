package com.growthaxys.churchmanagements

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Strings
import com.growthaxys.churchmanagements.data.repo.SessionState
import com.growthaxys.churchmanagements.designsystem.AppBackground
import com.growthaxys.churchmanagements.designsystem.ChurchTheme
import com.growthaxys.churchmanagements.designsystem.LoadingBox
import com.growthaxys.churchmanagements.feature.auth.LoginScreen
import com.growthaxys.churchmanagements.feature.auth.RegisterScreen
import com.growthaxys.churchmanagements.feature.common.LocalToaster
import com.growthaxys.churchmanagements.feature.common.ToastHost
import com.growthaxys.churchmanagements.feature.common.ToastMsg
import com.growthaxys.churchmanagements.feature.shell.AppViewModel
import com.growthaxys.churchmanagements.feature.splash.SplashScreen
import androidx.compose.runtime.saveable.rememberSaveable
import com.growthaxys.churchmanagements.feature.shell.MainShell
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot(app: AppViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val session by app.session.collectAsStateWithLifecycle()
    val lang by app.lang.collectAsStateWithLifecycle()
    val strings = remember(lang) { Strings.load(ctx, lang) }
    var toast by remember { mutableStateOf<ToastMsg?>(null) }
    var registerOpen by rememberSaveable { mutableStateOf(false) }

    CompositionLocalProvider(LocalStrings provides strings, LocalToaster provides { toast = it }) {
        ChurchTheme {
            // Splash video first (web APK parity); the session resolves underneath, then the app fades in.
            var splashDone by rememberSaveable { mutableStateOf(false) }
            androidx.compose.animation.Crossfade(splashDone, animationSpec = androidx.compose.animation.core.tween(450), label = "splash") { done ->
                if (!done) SplashScreen { splashDone = true }
                else AppBackground {
                    when (val s = session) {
                        is SessionState.Loading -> LoadingBox()
                        is SessionState.SignedOut -> if (registerOpen) RegisterScreen(onBack = { registerOpen = false }) else LoginScreen(lang = lang, onLang = app::setLang, onRegister = { registerOpen = true })
                        is SessionState.SignedIn -> MainShell(profile = s.profile, lang = lang, onLang = app::setLang, onLogout = app::logout)
                    }
                    ToastHost(toast) { toast = null }
                }
            }
        }
    }
}
