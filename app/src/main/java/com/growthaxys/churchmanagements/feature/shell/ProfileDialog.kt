package com.growthaxys.churchmanagements.feature.shell

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.ActionResult
import com.growthaxys.churchmanagements.data.model.Profile
import com.growthaxys.churchmanagements.data.repo.AuthRepository
import com.growthaxys.churchmanagements.data.repo.ProfileRepository
import com.growthaxys.churchmanagements.designsystem.AppButton
import com.growthaxys.churchmanagements.designsystem.AppDialog
import com.growthaxys.churchmanagements.designsystem.AppTextField
import com.growthaxys.churchmanagements.designsystem.BtnVariant
import com.growthaxys.churchmanagements.designsystem.InitialsAvatar
import com.growthaxys.churchmanagements.designsystem.LabeledField
import com.growthaxys.churchmanagements.designsystem.Tokens
import com.growthaxys.churchmanagements.feature.common.LocalToaster
import com.growthaxys.churchmanagements.feature.common.ToastMsg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

private val DEMO_EMAILS = setOf("usertest@gmail.com", "admintest@gmail.com", "ysufimanuel@gmail.com")
private const val MAX_INPUT_BYTES = 5L * 1024 * 1024

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repo: ProfileRepository, private val auth: AuthRepository) : ViewModel() {
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun run(onResult: (ActionResult) -> Unit, block: suspend () -> ActionResult) {
        viewModelScope.launch { _busy.value = true; val r = block(); _busy.value = false; onResult(r) }
    }
    suspend fun save(nama: String, username: String) = repo.update(nama, username)
    suspend fun upload(bytes: ByteArray) = repo.uploadAvatar(bytes)
    suspend fun remove() = repo.removeAvatar()
    suspend fun password(email: String, cur: String, new: String) = auth.changePassword(email, cur, new)
}

/** Decodes + downsizes to <=512px and re-encodes as JPEG (web resizes client-side too). */
private suspend fun prepareImage(ctx: android.content.Context, uri: Uri): Result<ByteArray> = withContext(Dispatchers.IO) {
    runCatching {
        val mime = ctx.contentResolver.getType(uri).orEmpty()
        require(mime in setOf("image/jpeg", "image/png", "image/webp")) { "photo-invalid" }
        val size = ctx.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
        require(size <= MAX_INPUT_BYTES) { "photo-too-large" }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
        val bmp = ctx.contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }!!
        val scale = 512f / maxOf(bmp.width, bmp.height)
        val out = if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
        ByteArrayOutputStream().also { out.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
    }
}

@Composable
fun ProfileDialog(profile: Profile, role: Role, onClose: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val s = LocalStrings.current
    val toaster = LocalToaster.current
    val busy by vm.busy.collectAsStateWithLifecycle()
    var nama by remember { mutableStateOf(profile.nama) }
    var username by remember { mutableStateOf(profile.username.orEmpty()) }
    var cur by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    val locked = profile.email?.lowercase() in DEMO_EMAILS

    fun report(r: ActionResult, okKey: String = "profile-saved") {
        if (r.ok) toaster(ToastMsg(key = okKey)) else toaster(ToastMsg(raw = errText(s, r.error), isError = true))
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.run({ report(it) }) {
            val prepared = prepareImage(ctx, uri)
            prepared.fold({ vm.upload(it) }, { ActionResult.fail(errKey(it.message)) })
        }
    }

    AppDialog(t("my-profile"), onClose) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            InitialsAvatar(profile.nama, size = 64.dp, imageUrl = profile.avatarUrl, fontSize = 20)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(Rbac.roleLabel(role), fontSize = 12.sp, color = Tokens.MutedForeground)
                if (!locked) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppButton(t("change-photo"), { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, variant = BtnVariant.Secondary, height = 36.dp)
                    if (!profile.avatarUrl.isNullOrBlank()) AppButton(t("remove-photo"), { vm.run({ report(it) }) { vm.remove() } }, variant = BtnVariant.Ghost, height = 36.dp)
                }
                Text(t("photo-hint"), fontSize = 11.sp, color = Tokens.MutedForeground)
            }
        }
        if (locked) Text(t("profile-demo-locked"), fontSize = 12.sp, color = Tokens.Amber400)
        LabeledField(t("name")) { AppTextField(nama, { nama = it }, enabled = !locked) }
        LabeledField(t("username")) { AppTextField(username, { username = it }, enabled = !locked) }
        LabeledField(t("email")) { AppTextField(profile.email.orEmpty(), {}, enabled = false) }
        if (!locked) {
            AppButton(if (busy) t("saving") else t("save-changes"), { vm.run({ report(it) }) { vm.save(nama, username) } }, Modifier.fillMaxWidth(), loading = busy)
            Text(t("change-password"), fontWeight = FontWeight.SemiBold, color = Color.White)
            LabeledField(t("current-password")) { AppTextField(cur, { cur = it }, password = true) }
            LabeledField(t("new-password")) { AppTextField(next, { next = it }, password = true) }
            AppButton(t("change-password"), {
                if (next.length < 6) toaster(ToastMsg(raw = s.t("password-min"), isError = true))
                else vm.run({ r -> report(r, "profile-saved"); if (r.ok) { cur = ""; next = "" } }) { vm.password(profile.email.orEmpty(), cur, next) }
            }, Modifier.fillMaxWidth(), variant = BtnVariant.Secondary, enabled = cur.isNotEmpty() && next.isNotEmpty())
        }
    }
}

private fun errKey(m: String?) = when (m) { "photo-invalid", "photo-too-large" -> "__$m" else -> m ?: "upload-failed" }
private fun errText(s: com.growthaxys.churchmanagements.core.Strings, e: String?): String = when (e) {
    AuthRepository.PASSWORD_WRONG -> s.t("password-wrong")
    "__photo-invalid" -> s.t("photo-invalid"); "__photo-too-large" -> s.t("photo-too-large")
    ProfileRepository.INVALID_NAME, ProfileRepository.INVALID_USERNAME -> s.t("name")
    ProfileRepository.USERNAME_TAKEN -> s.t("username")
    else -> s.tm(e)
}
