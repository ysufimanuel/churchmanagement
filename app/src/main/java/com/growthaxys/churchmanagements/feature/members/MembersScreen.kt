package com.growthaxys.churchmanagements.feature.members

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Member
import com.growthaxys.churchmanagements.data.model.NamedRef
import com.growthaxys.churchmanagements.data.repo.MemberInput
import com.growthaxys.churchmanagements.data.repo.PeopleRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class MembersData(val members: List<Member>, val groups: List<NamedRef>)

@HiltViewModel
class MembersViewModel @Inject constructor(private val repo: PeopleRepository) : FeatureViewModel<MembersData>() {
    override suspend fun fetch() = MembersData(repo.members(), repo.groupNames())
    fun save(i: MemberInput, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveMember(i) }
    fun delete(id: Long) = mutate { repo.deleteMember(id) }
}

@Composable
fun MembersScreen(role: Role, padding: PaddingValues, initialQuery: String, vm: MembersViewModel = hiltViewModel()) {
    LoadGate(vm) { data ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        val s = LocalStrings.current
        var search by remember { mutableStateOf(initialQuery) }
        var status by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("") }
        var editing by remember { mutableStateOf<MemberInput?>(null) }
        var detail by remember { mutableStateOf<Member?>(null) }
        val groupName = { id: Long? -> data.groups.firstOrNull { it.id == id }?.nama ?: "-" }
        val q = search.trim().lowercase()
        val filtered = data.members.filter { m ->
            (q.isEmpty() || listOf(m.nama, m.email, m.telepon, m.alamat, m.kota).any { (it ?: "").lowercase().contains(q) }) &&
                (status.isEmpty() || m.status == status) && (gender.isEmpty() || m.jk == gender)
        }

        ModulePage(padding) {
            item { PageHeader(t("members"), t("manage-members")) }
            item {
                FilterRow {
                    SearchField(search, { search = it }, t("search-members"))
                    AppSelect(status, listOf(Option("", t("all-status")), Option("aktif", t("active")), Option("tidak-aktif", t("inactive"))), { status = it })
                    AppSelect(gender, listOf(Option("", t("all-gender")), Option("Laki-laki", t("male")), Option("Perempuan", t("female"))), { gender = it })
                    ExportButton(t("members-report"), "members", {
                        filtered.map { mapOf(s.t("exp-name") to it.nama, s.t("email") to it.email, s.t("phone") to it.telepon, s.t("exp-gender") to it.jk, s.t("status") to it.status, s.t("group") to groupName(it.groupId)) }
                    })
                    if (Rbac.canAdd(role)) AppButton(t("add-member"), { editing = MemberInput(nama = "", joinDate = Format.today()) }, icon = Icons.Outlined.Add)
                }
            }
            items(filtered, key = { it.id }) { m ->
                DataRowCard(
                    m.nama,
                    listOf(
                        t("email") to { FieldText(m.email) }, t("phone") to { FieldText(m.telepon) },
                        t("gender") to { FieldText(m.jk) }, t("group") to { FieldText(groupName(m.groupId)) },
                        t("status") to { AppBadge(if (m.status == "aktif") t("active") else t("inactive"), if (m.status == "aktif") BadgeVariant.Success else BadgeVariant.Secondary) },
                    ),
                    leading = { InitialsAvatar(m.nama) },
                    actions = {
                        RowActions(Rbac.canEdit(role), Rbac.canDelete(role),
                            onEdit = { editing = MemberInput(m.id, m.nama, m.email.orEmpty(), m.telepon.orEmpty(), m.jk, m.tempatLahir.orEmpty(), m.tglLahir.orEmpty(), m.alamat.orEmpty(), m.kota.orEmpty(), m.kodepos.orEmpty(), m.status, m.groupId, m.joinDate.orEmpty()) },
                            onDelete = { vm.delete(m.id) }, onDetail = { detail = m })
                    },
                )
            }
            if (filtered.isEmpty()) item { EmptyText() }
        }

        editing?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            AppDialog(if (init.id != null) t("act-edit") else t("add-new-member"), { editing = null }, wide = true) {
                LabeledField(t("full-name")) { AppTextField(f.nama, { f = f.copy(nama = it) }) }
                LabeledField(t("email")) { AppTextField(f.email, { f = f.copy(email = it) }, keyboardType = KeyboardType.Email) }
                LabeledField(t("phone")) { AppTextField(f.telepon, { f = f.copy(telepon = it) }, keyboardType = KeyboardType.Phone) }
                LabeledField(t("gender")) { AppSelect(f.jk.orEmpty(), listOf(Option("Laki-laki", t("male")), Option("Perempuan", t("female"))), { f = f.copy(jk = it) }, placeholder = t("select")) }
                LabeledField(t("birth-place")) { AppTextField(f.tempatLahir, { f = f.copy(tempatLahir = it) }) }
                LabeledField(t("birth-date")) { DateField(f.tglLahir, { f = f.copy(tglLahir = it) }) }
                LabeledField(t("full-address")) { AppTextField(f.alamat, { f = f.copy(alamat = it) }, singleLine = false, minHeight = 80.dp) }
                LabeledField(t("city")) { AppTextField(f.kota, { f = f.copy(kota = it) }) }
                LabeledField(t("postal-code")) { AppTextField(f.kodepos, { f = f.copy(kodepos = it) }, keyboardType = KeyboardType.Number) }
                LabeledField(t("status")) { AppSelect(f.status, listOf(Option("aktif", t("active")), Option("tidak-aktif", t("inactive"))), { f = f.copy(status = it) }) }
                LabeledField(t("group-ministry")) { AppSelect(f.groupId?.toString().orEmpty(), listOf(Option("", t("none"))) + data.groups.map { Option(it.id.toString(), it.nama) }, { f = f.copy(groupId = it.toLongOrNull()) }) }
                LabeledField(t("join-date")) { DateField(f.joinDate, { f = f.copy(joinDate = it) }) }
                DialogActions({ editing = null }, { if (f.nama.isNotBlank()) vm.save(f) { editing = null } }, saving = busy)
            }
        }
        detail?.let { m ->
            AppDialog(m.nama, { detail = null }) {
                listOf(
                    t("email") to m.email, t("phone") to m.telepon, t("gender") to m.jk,
                    t("birth-place") to m.tempatLahir, t("birth-date") to "${Format.date(m.tglLahir, s.lang)} (${Format.age(m.tglLahir)})",
                    t("full-address") to m.alamat, t("city") to m.kota, t("postal-code") to m.kodepos,
                    t("group") to groupName(m.groupId), t("join-date") to Format.date(m.joinDate, s.lang),
                ).forEach { (k, v) -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(k, fontSize = 12.sp, color = Tokens.MutedForeground); FieldText(v) } }
            }
        }
    }
}
