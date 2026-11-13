package com.growthaxys.churchmanagements.feature.families

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Family
import com.growthaxys.churchmanagements.data.model.FamilyMember
import com.growthaxys.churchmanagements.data.model.NamedRef
import com.growthaxys.churchmanagements.data.repo.FamilyInput
import com.growthaxys.churchmanagements.data.repo.PeopleRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class FamiliesData(val families: List<Family>, val links: List<FamilyMember>, val members: List<NamedRef>)

@HiltViewModel
class FamiliesViewModel @Inject constructor(private val repo: PeopleRepository) : FeatureViewModel<FamiliesData>() {
    override suspend fun fetch() = FamiliesData(repo.families(), repo.familyMembers(), repo.memberNames())
    fun save(i: FamilyInput, superadmin: Boolean, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveFamily(i, superadmin) }
    fun delete(id: Long) = mutate { repo.deleteFamily(id) }
}

@Composable
fun FamiliesScreen(role: Role, padding: PaddingValues, initialQuery: String, vm: FamiliesViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Cards) { data ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        var search by remember { mutableStateOf(initialQuery) }
        var editing by remember { mutableStateOf<FamilyInput?>(null) }
        val nameOf = { id: Long? -> data.members.firstOrNull { it.id == id }?.nama ?: "-" }
        val q = search.trim().lowercase()
        val filtered = data.families.filter { q.isEmpty() || listOf(it.nama, it.alamat, it.kota).any { x -> (x ?: "").lowercase().contains(q) } }
        val isSuper = role == Role.SUPERADMIN

        ModulePage(padding) {
            item { PageHeader(t("families"), t("manage-families")) }
            item {
                FilterRow {
                    SearchField(search, { search = it }, t("search-families"))
                    if (Rbac.canAdd(role)) AppButton(t("add-family"), { editing = FamilyInput(null, "", null, "", "", emptyList()) }, icon = Icons.Outlined.Add)
                }
            }
            items(filtered, key = { it.id }) { fam ->
                val memberIds = data.links.filter { it.familyId == fam.id }.map { it.memberId }
                AppCard {
                    CardHeaderRow {
                        androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.weight(1f)) {
                            Text(fam.nama, fontSize = 16.sp, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            Text("${t("head-family")}: ${nameOf(fam.kepalaId)}", fontSize = 12.sp, color = Tokens.MutedForeground)
                        }
                        RowActions(Rbac.canEdit(role), Rbac.canDelete(role),
                            onEdit = { editing = FamilyInput(fam.id, fam.nama, fam.kepalaId, fam.alamat.orEmpty(), fam.kota.orEmpty(), memberIds) },
                            onDelete = { vm.delete(fam.id) })
                    }
                    CardBody {
                        FieldText(listOfNotNull(fam.alamat, fam.kota).joinToString(", "), Tokens.MutedForeground)
                        Text("${t("family-members")}: " + (memberIds.joinToString(", ") { nameOf(it) }.ifEmpty { "-" }), fontSize = 13.sp, color = Color.White)
                    }
                }
            }
            if (filtered.isEmpty()) item { EmptyText() }
        }

        editing?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            AppDialog(t("add-family"), { editing = null }) {
                LabeledField(t("family-name")) { AppTextField(f.nama, { f = f.copy(nama = it) }) }
                LabeledField(t("head-family")) { AppSelect(f.kepalaId?.toString().orEmpty(), listOf(Option("", t("select"))) + data.members.map { Option(it.id.toString(), it.nama) }, { f = f.copy(kepalaId = it.toLongOrNull()) }) }
                LabeledField(t("address")) { AppTextField(f.alamat, { f = f.copy(alamat = it) }, singleLine = false, minHeight = 80.dp) }
                LabeledField(t("city")) { AppTextField(f.kota, { f = f.copy(kota = it) }) }
                LabeledField(t("family-members")) {
                    androidx.compose.foundation.layout.Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
                        data.members.forEach { m ->
                            val on = m.id in f.anggota
                            // Admins can only ADD relations; removing is superadmin-only (same as web / RLS).
                            val locked = on && !isSuper && init.anggota.contains(m.id)
                            androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                                AppSwitch(on) { v -> if (!locked) f = f.copy(anggota = if (v) f.anggota + m.id else f.anggota - m.id) }
                                Text(m.nama, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
                DialogActions({ editing = null }, { if (f.nama.isNotBlank()) vm.save(f, isSuper) { editing = null } }, saving = busy)
            }
        }
    }
}
