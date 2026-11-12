package com.growthaxys.churchmanagements.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.People
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Group
import com.growthaxys.churchmanagements.data.model.GroupMember
import com.growthaxys.churchmanagements.data.model.NamedRef
import com.growthaxys.churchmanagements.data.repo.GroupInput
import com.growthaxys.churchmanagements.data.repo.PeopleRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class GroupsData(val groups: List<Group>, val links: List<GroupMember>, val members: List<NamedRef>)

@HiltViewModel
class GroupsViewModel @Inject constructor(private val repo: PeopleRepository) : FeatureViewModel<GroupsData>() {
    override suspend fun fetch() = GroupsData(repo.groups(), repo.groupMembers(), repo.memberNames())
    fun save(i: GroupInput, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveGroup(i) }
    fun delete(id: Long) = mutate { repo.deleteGroup(id) }
    fun addMember(g: Long, m: Long) = mutate { repo.addGroupMember(g, m) }
    fun removeMember(g: Long, m: Long) = mutate { repo.removeGroupMember(g, m) }
}

@Composable
fun GroupsScreen(role: Role, padding: PaddingValues, initialQuery: String, vm: GroupsViewModel = hiltViewModel()) {
    LoadGate(vm) { data ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        var search by remember { mutableStateOf(initialQuery) }
        var editing by remember { mutableStateOf<GroupInput?>(null) }
        var managing by remember { mutableStateOf<Long?>(null) }
        val nameOf = { id: Long? -> data.members.firstOrNull { it.id == id }?.nama ?: "-" }
        val q = search.trim().lowercase()
        val filtered = data.groups.filter { q.isEmpty() || listOf(it.nama, it.deskripsi, it.jadwal).any { x -> (x ?: "").lowercase().contains(q) } }

        ModulePage(padding) {
            item { PageHeader(t("groups"), t("manage-groups")) }
            item {
                FilterRow {
                    SearchField(search, { search = it }, t("search-groups"))
                    if (Rbac.canAdd(role)) AppButton(t("add-group"), { editing = GroupInput(null, "", "", null, "", true) }, icon = Icons.Outlined.Add)
                }
            }
            items(filtered, key = { it.id }) { g ->
                val count = data.links.count { it.groupId == g.id }
                AppCard {
                    CardHeaderRow {
                        Column(Modifier.weight(1f)) {
                            Text(g.nama, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("${t("leader")}: ${nameOf(g.leaderId)}", fontSize = 12.sp, color = Tokens.MutedForeground)
                        }
                        RowActions(Rbac.canEdit(role), Rbac.canDelete(role),
                            onEdit = { editing = GroupInput(g.id, g.nama, g.deskripsi.orEmpty(), g.leaderId, g.jadwal.orEmpty(), g.showPhone) },
                            onDelete = { vm.delete(g.id) })
                    }
                    CardBody {
                        FieldText(g.deskripsi, Tokens.MutedForeground)
                        FieldText("${t("meeting-schedule")}: ${g.jadwal ?: "-"}", Tokens.MutedForeground)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            AppBadge("$count ${t("members").lowercase()}", BadgeVariant.Secondary)
                            AppButton(t("act-manage-members"), { managing = g.id }, variant = BtnVariant.Secondary, icon = Icons.Outlined.People, height = 36.dp)
                        }
                    }
                }
            }
            if (filtered.isEmpty()) item { EmptyText() }
        }

        editing?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            AppDialog(t("add-group"), { editing = null }) {
                LabeledField(t("group-name")) { AppTextField(f.nama, { f = f.copy(nama = it) }) }
                LabeledField(t("description")) { AppTextField(f.deskripsi, { f = f.copy(deskripsi = it) }, singleLine = false, minHeight = 80.dp) }
                LabeledField(t("leader")) { AppSelect(f.leaderId?.toString().orEmpty(), listOf(Option("", t("select"))) + data.members.map { Option(it.id.toString(), it.nama) }, { f = f.copy(leaderId = it.toLongOrNull()) }) }
                LabeledField(t("meeting-schedule")) { AppTextField(f.jadwal, { f = f.copy(jadwal = it) }) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppSwitch(f.showPhone) { f = f.copy(showPhone = it) }
                    Text(t("show-phone-to-members"), fontSize = 14.sp, color = Color.White)
                }
                DialogActions({ editing = null }, { if (f.nama.isNotBlank()) vm.save(f) { editing = null } }, saving = busy)
            }
        }

        managing?.let { gid ->
            var pick by remember { mutableStateOf("") }
            val g = data.groups.firstOrNull { it.id == gid }
            val ids = data.links.filter { it.groupId == gid }.map { it.memberId }
            AppDialog(g?.nama ?: t("groups"), { managing = null }) {
                if (Rbac.canAdd(role)) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AppSelect(pick, data.members.filter { it.id !in ids }.map { Option(it.id.toString(), it.nama) }, { pick = it }, Modifier.weight(1f), placeholder = t("select-member"))
                    AppButton(t("add"), { pick.toLongOrNull()?.let { vm.addMember(gid, it); pick = "" } }, enabled = pick.isNotEmpty())
                }
                Text(t("current-members"), fontWeight = FontWeight.SemiBold, color = Color.White)
                if (ids.isEmpty()) EmptyText()
                ids.forEach { mid ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(nameOf(mid), color = Color.White, fontSize = 14.sp)
                        if (Rbac.canDelete(role)) AppIconButton(Icons.Outlined.Close, t("act-delete"), { vm.removeMember(gid, mid) }, tint = Tokens.Red400)
                    }
                }
            }
        }
    }
}
