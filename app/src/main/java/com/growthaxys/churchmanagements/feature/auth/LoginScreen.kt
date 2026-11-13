package com.growthaxys.churchmanagements.feature.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.growthaxys.churchmanagements.BuildConfig
import com.growthaxys.churchmanagements.core.Lang
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.repo.AuthRepository
import com.growthaxys.churchmanagements.data.repo.LoginError
import com.growthaxys.churchmanagements.data.repo.LoginException
import com.growthaxys.churchmanagements.designsystem.AppButton
import com.growthaxys.churchmanagements.designsystem.EaseBackOut
import com.growthaxys.churchmanagements.designsystem.enterAnim
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.LaunchedEffect
import com.growthaxys.churchmanagements.designsystem.AppDialog
import com.growthaxys.churchmanagements.designsystem.AppIconButton
import com.growthaxys.churchmanagements.designsystem.AppTextField
import com.growthaxys.churchmanagements.designsystem.LabeledField
import com.growthaxys.churchmanagements.designsystem.Tokens
import com.growthaxys.churchmanagements.feature.shell.LangSwitcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUi(val loading: Boolean = false, val error: LoginError? = null)

@HiltViewModel
class LoginViewModel @Inject constructor(private val auth: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUi())
    val ui: StateFlow<LoginUi> = _ui.asStateFlow()

    fun submit(user: String, pass: String) {
        _ui.value = LoginUi(loading = true)
        viewModelScope.launch {
            _ui.value = try { auth.signIn(user, pass); LoginUi() }
            catch (e: LoginException) { LoginUi(error = e.kind) }
            catch (e: Exception) { LoginUi(error = LoginError.Unavailable) }
        }
    }
}

/** Demo accounts exist in the web repo (src/lib/demo-accounts.ts). Shown only in debug builds on Android. */
private val DEMO = listOf(
    Triple("user", "usertest@gmail.com", "user123"),
    Triple("admin", "admintest@gmail.com", "admin123"),
    Triple("superadmin", "ysufimanuel@gmail.com", "super123"),
)

@Composable
fun LoginScreen(lang: Lang, onLang: (Lang) -> Unit, vm: LoginViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var demo by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    // Web: error -> card shakes x ±7 (6 × 70ms yoyo) and the message fades in.
    val shake = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(ui.error) {
        if (ui.error != null) {
            shake.snapTo(-7f)
            repeat(6) { i -> shake.animateTo(if (i % 2 == 0) 7f else -7f, androidx.compose.animation.core.tween(70, easing = androidx.compose.animation.core.LinearEasing)) }
            shake.animateTo(0f, androidx.compose.animation.core.tween(40))
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0x2906B6D4), Color.Transparent), radius = 900f))) {
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
        ) {
            Column(
                Modifier.widthIn(max = 420.dp).fillMaxWidth()
                    .enterAnim(0, 24.dp, 700, fromScale = 0.97f)
                    .graphicsLayer { translationX = shake.value.dp.toPx() }
                    .background(Color(0x0EFFFFFF), RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(28.dp)).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.enterAnim(350, 0.dp, 550, fromScale = 0.7f, fromRotate = -12f, easing = EaseBackOut)
                        .size(56.dp).background(Color(0x1A22D3EE), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x4022D3EE), RoundedCornerShape(16.dp))
                        .clickable(enabled = BuildConfig.DEBUG) { demo = true },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Church, null, Modifier.size(28.dp), tint = Color(0xFF67E8F9)) }
                Spacer(Modifier.height(20.dp))
                Text("FLOCKLY", Modifier.enterAnim(650, 12.dp, 400), fontSize = 10.sp, letterSpacing = 2.4.sp, fontWeight = FontWeight.SemiBold, color = Color(0xCC67E8F9))
                Spacer(Modifier.height(6.dp))
                Text(t("app-title"), Modifier.enterAnim(740, 12.dp, 400), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(t("created-by"), Modifier.enterAnim(830, 12.dp, 400), fontSize = 12.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledField(t("username"), Modifier.enterAnim(1000, 14.dp, 400)) { AppTextField(username, { username = it }, placeholder = t("username-placeholder"), minHeight = 48.dp) }
                    LabeledField(t("password"), Modifier.enterAnim(1070, 14.dp, 400)) {
                        AppTextField(password, { password = it }, placeholder = t("password-placeholder"), minHeight = 48.dp, password = !show,
                            trailing = {
                                AppIconButton(if (show) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    if (show) t("hide-password") else t("show-password"), { show = !show }, tint = Color(0xFF94A3B8), size = 28.dp)
                            })
                    }
                    // Web renders "Forgot password?" as inert text (no handler) — kept identical for parity.
                    Row(Modifier.fillMaxWidth().enterAnim(1140, 14.dp, 400), horizontalArrangement = Arrangement.End) {
                        Text(t("forgot-password"), fontSize = 12.sp, color = Color(0xE667E8F9))
                    }
                    ui.error?.let { e ->
                        val key = when (e) {
                            LoginError.Required -> "login-required"; LoginError.Invalid -> "login-invalid"
                            LoginError.UsernameUnconfigured -> "login-username-unconfigured"; LoginError.Unavailable -> "login-unavailable"
                        }
                        Text(t(key), Modifier.fillMaxWidth().enterAnim(0, (-5).dp, 250).background(Color(0x1AEF4444), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x33F87171), RoundedCornerShape(12.dp)).padding(14.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFFCA5A5), textAlign = TextAlign.Center)
                    }
                    AppButton(if (ui.loading) t("loading") else t("login"), { vm.submit(username, password) },
                        Modifier.fillMaxWidth().enterAnim(1210, 14.dp, 400), icon = Icons.Outlined.Login, loading = ui.loading, height = 48.dp)
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.enterAnim(1400, 8.dp, 350), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(t("no-church-account"), fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Row(
                        Modifier.clickable { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${BuildConfig.WEB_BASE_URL}/register"))) },
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Outlined.PersonAdd, null, Modifier.size(14.dp), tint = Color(0xFF67E8F9))
                        Text(t("register-church"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF67E8F9))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.enterAnim(1480, 8.dp, 350)) { LangSwitcher(lang, onLang) }
            }
        }
    }

    if (demo) DemoDialog({ demo = false }) { e, p -> username = e; password = p; demo = false }
}

@Composable
private fun DemoDialog(onClose: () -> Unit, onUse: (String, String) -> Unit) {
    val clip = LocalClipboardManager.current
    AppDialog(t("demo-accounts"), onClose) {
        Text(t("demo-subtitle"), fontSize = 12.sp, color = Color(0xFF94A3B8))
        DEMO.forEach { (role, email, pass) ->
            Column(Modifier.fillMaxWidth().background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp)).border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t("role-$role"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA5F3FC))
                Text(t("demo-desc-$role"), fontSize = 12.sp, color = Color(0xFF94A3B8))
                listOf(t("email") to email, t("password") to pass).forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth().background(Color(0x40000000), RoundedCornerShape(8.dp)).padding(10.dp, 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(k.uppercase(), fontSize = 10.sp, color = Color(0xFF64748B))
                            Text(v, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFE2E8F0))
                        }
                        AppIconButton(Icons.Outlined.ContentCopy, t("demo-copy"), { clip.setText(AnnotatedString(v)) }, tint = Color(0xFF94A3B8), size = 32.dp)
                    }
                }
                AppButton("${t("demo-use")} ${t("role-$role")}", { onUse(email, pass) }, Modifier.fillMaxWidth(), height = 40.dp)
            }
        }
    }
}
