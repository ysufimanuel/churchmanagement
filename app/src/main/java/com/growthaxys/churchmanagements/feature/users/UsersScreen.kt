package com.growthaxys.churchmanagements.feature.users

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Profile
import com.growthaxys.churchmanagements.data.repo.ProfileRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(private val repo: ProfileRepository) : FeatureViewModel<List<Profile>>() {
    override suspend fun fetch() = repo.users()
}

/** Read-only list (superadmin). Creating/deleting users needs the service-role key and stays web-only — see WEB_PARITY_MATRIX. */
@Composable
fun UsersScreen(padding: PaddingValues, vm: UsersViewModel = hiltViewModel()) {
    LoadGate(vm) { users ->
        val s = LocalStrings.current
        var search by remember { mutableStateOf("") }
        val q = search.trim().lowercase()
        val list = users.filter { q.isEmpty() || listOf(it.nama, it.username, it.email, it.role).any { x -> (x ?: "").lowercase().contains(q) } }
        ModulePage(padding) {
            item { PageHeader(t("user-management"), t("manage-users")) }
            item { SearchField(search, { search = it }, t("search-users")) }
            items(list, key = { it.id }) { u ->
                DataRowCard(
                    u.nama,
                    listOf(
                        t("username") to { FieldText(u.username) }, t("email") to { FieldText(u.email) },
                        t("role") to { AppBadge(Rbac.roleLabel(Role.from(u.role)), BadgeVariant.Default) },
                        t("status") to { AppBadge(if (u.status == "aktif") t("active") else t("inactive"), if (u.status == "aktif") BadgeVariant.Success else BadgeVariant.Secondary) },
                    ),
                    leading = { InitialsAvatar(u.nama, imageUrl = u.avatarUrl) },
                )
            }
            if (list.isEmpty()) item { EmptyText() }
        }
    }
}
