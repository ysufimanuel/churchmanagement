package com.growthaxys.churchmanagements.feature.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Profile
import com.growthaxys.churchmanagements.data.repo.MobileAdminRepository
import com.growthaxys.churchmanagements.data.repo.MobileUserInput
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(private val api: MobileAdminRepository) : FeatureViewModel<List<Profile>>() {
    override suspend fun fetch() = api.users()
    fun save(input: MobileUserInput, onOk: () -> Unit) = mutate("saved-ok", onOk) { api.saveUser(input) }
    fun delete(id: String, onOk: () -> Unit) = mutate("deleted-ok", onOk) { api.deleteUser(id) }
}

@Composable
fun UsersScreen(padding: PaddingValues, selfId: String, vm: UsersViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Table) { users ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        var search by remember { mutableStateOf("") }
        var draft by remember { mutableStateOf<UserDraft?>(null) }
        var deleteTarget by remember { mutableStateOf<Profile?>(null) }
        val q = search.trim().lowercase()
        val list = users.filter { q.isEmpty() || listOf(it.nama, it.username, it.email, it.role).any { value -> (value ?: "").lowercase().contains(q) } }

        ModulePage(padding) {
            item { PageHeader(t("user-management"), t("manage-users")) }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchField(search, { search = it }, t("search-users"), Modifier.weight(1f))
                    AppButton(t("add-user"), { draft = UserDraft() }, icon = Icons.Outlined.Add)
                }
            }
            items(list, key = { it.id }) { user ->
                AppCard {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InitialsAvatar(user.nama, imageUrl = user.avatarUrl, size = 46.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(user.nama, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("@${user.username.orEmpty()} · ${user.email.orEmpty()}", color = Tokens.MutedForeground, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                AppBadge(Rbac.roleLabel(Role.from(user.role)), if (user.role == "superadmin") BadgeVariant.Default else BadgeVariant.Secondary)
                                AppBadge(if (user.status == "aktif") t("active") else t("inactive"), if (user.status == "aktif") BadgeVariant.Success else BadgeVariant.Warning)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AppIconButton(Icons.Outlined.Edit, t("act-edit"), { draft = UserDraft.from(user) })
                            if (user.id != selfId) AppIconButton(Icons.Outlined.Delete, t("act-delete"), { deleteTarget = user }, tint = Tokens.Red400)
                        }
                    }
                }
            }
            if (list.isEmpty()) item { EmptyText() }
        }

        draft?.let { initial ->
            var nama by remember(initial) { mutableStateOf(initial.nama) }
            var username by remember(initial) { mutableStateOf(initial.username) }
            var email by remember(initial) { mutableStateOf(initial.email) }
            var password by remember(initial) { mutableStateOf("") }
            var role by remember(initial) { mutableStateOf(initial.role) }
            var status by remember(initial) { mutableStateOf(initial.status) }
            AppDialog(if (initial.id == null) t("add-user") else "Edit User", { draft = null }, wide = true) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledField(t("full-name")) { AppTextField(nama, { nama = it }) }
                    LabeledField(t("username")) { AppTextField(username, { username = it }) }
                    LabeledField(t("email")) { AppTextField(email, { email = it }, keyboardType = KeyboardType.Email) }
                    LabeledField(t("password") + if (initial.id != null) " (opsional)" else "") {
                        AppTextField(password, { password = it }, password = true, placeholder = if (initial.id != null) "Kosongkan jika tidak diubah" else "Minimal 6 karakter")
                    }
                    LabeledField("Role") {
                        AppSelect(role, listOf(
                            Option("user", t("user-view-only")),
                            Option("admin", t("admin")),
                            Option("superadmin", t("superadmin")),
                        ), { role = it })
                    }
                    LabeledField(t("status")) {
                        AppSelect(status, listOf(Option("aktif", t("active")), Option("nonaktif", t("inactive"))), { status = it })
                    }
                    DialogActions(
                        onCancel = { draft = null },
                        onSave = {
                            vm.save(MobileUserInput(initial.id, nama.trim(), username.trim(), email.trim(), password, role, status)) { draft = null }
                        },
                        saving = busy,
                    )
                }
            }
        }

        deleteTarget?.let { target ->
            AppDialog(t("confirmation"), { deleteTarget = null }) {
                Text("Hapus akun ${target.nama}? Akun ini tidak dapat digunakan lagi.", color = Color.White, fontSize = 13.sp)
                DialogActions(
                    onCancel = { deleteTarget = null },
                    onSave = { vm.delete(target.id) { deleteTarget = null } },
                    saving = busy,
                    saveLabel = t("act-delete"),
                )
            }
        }
    }
}

private data class UserDraft(
    val id: String? = null,
    val nama: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "user",
    val status: String = "aktif",
) {
    companion object {
        fun from(user: Profile) = UserDraft(
            id = user.id, nama = user.nama, username = user.username.orEmpty(),
            email = user.email.orEmpty(), role = user.role, status = user.status,
        )
    }
}
