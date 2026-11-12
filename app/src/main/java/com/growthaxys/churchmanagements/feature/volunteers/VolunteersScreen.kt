package com.growthaxys.churchmanagements.feature.volunteers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.NamedRef
import com.growthaxys.churchmanagements.data.model.Volunteer
import com.growthaxys.churchmanagements.data.model.VolunteerAssignment
import com.growthaxys.churchmanagements.data.repo.AssignmentInput
import com.growthaxys.churchmanagements.data.repo.VolunteerInput
import com.growthaxys.churchmanagements.data.repo.VolunteerRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private val AREAS = listOf("sunday-school-teacher", "singer", "worship-leader", "musician", "dancer", "collection", "intercession", "speaker", "multimedia", "lighting-sound", "usher")
private val SCHEDULES = listOf("minggu-pagi", "minggu-sore", "sabtu", "weekday")
private fun scheduleKey(s: String) = when (s) { "minggu-pagi" -> "sunday-morning"; "minggu-sore" -> "sunday-evening"; "sabtu" -> "saturday"; else -> "weekday" }

data class VolunteersData(val volunteers: List<Volunteer>, val assignments: List<VolunteerAssignment>, val members: List<NamedRef>, val events: List<NamedRef>)

@HiltViewModel
class VolunteersViewModel @Inject constructor(private val repo: VolunteerRepository) : FeatureViewModel<VolunteersData>() {
    override suspend fun fetch() = VolunteersData(repo.volunteers(), repo.assignments(), repo.memberNames(), repo.eventNames())
    fun saveVolunteer(i: VolunteerInput, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveVolunteer(i) }
    fun deleteVolunteer(id: Long) = mutate { repo.deleteVolunteer(id) }
    fun saveAssignment(i: AssignmentInput, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveAssignment(i) }
    fun deleteAssignment(id: Long) = mutate { repo.deleteAssignment(id) }
}

@Composable
fun VolunteersScreen(role: Role, padding: PaddingValues, initialQuery: String, vm: VolunteersViewModel = hiltViewModel()) {
    LoadGate(vm) { data ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        val s = LocalStrings.current
        var tab by remember { mutableStateOf("list") }
        var search by remember { mutableStateOf(initialQuery) }
        var volEdit by remember { mutableStateOf<VolunteerInput?>(null) }
        var asgEdit by remember { mutableStateOf<AssignmentInput?>(null) }
        val volName = { v: Volunteer -> if (v.memberId != null) data.members.firstOrNull { it.id == v.memberId }?.nama ?: "-" else v.externalNama ?: "-" }
        val volNameById = { id: Long -> data.volunteers.firstOrNull { it.id == id }?.let(volName) ?: "-" }
        val eventName = { id: Long? -> data.events.firstOrNull { it.id == id }?.nama ?: "-" }
        val q = search.trim().lowercase()
        val filtered = data.volunteers.filter { v -> q.isEmpty() || (listOf(volName(v), v.externalEmail, v.externalTelepon, v.area, v.status) + v.jadwal).any { (it ?: "").lowercase().contains(q) } }
        val filteredAsg = data.assignments.filter { a -> q.isEmpty() || listOf(volNameById(a.volunteerId), eventName(a.eventId), a.tugas, a.tempat, a.catatan, a.status).any { (it ?: "").lowercase().contains(q) } }

        ModulePage(padding) {
            item { PageHeader(t("volunteers"), t("manage-volunteers")) }
            item { AppTabs(listOf(TabItem("list", t("volunteer-list")), TabItem("schedule", t("service-schedule")), TabItem("assignment", t("assignments"))), tab, { tab = it }) }
            item {
                FilterRow {
                    SearchField(search, { search = it }, t("search-volunteers"))
                    if (tab == "list" && Rbac.canAdd(role)) AppButton(t("add-volunteer"), { volEdit = VolunteerInput(null, null, "", "", "", AREAS.first(), emptyList(), "aktif") }, icon = Icons.Outlined.Add)
                    if (tab == "assignment" && Rbac.canAdd(role)) AppButton(t("add-assignment"), { asgEdit = AssignmentInput(null, data.volunteers.firstOrNull()?.id ?: 0, null, Format.today(), "", "", "", "") }, icon = Icons.Outlined.Add, enabled = data.volunteers.isNotEmpty())
                }
            }

            when (tab) {
                "list" -> {
                    items(filtered, key = { it.id }) { v ->
                        DataRowCard(
                            volName(v),
                            listOf(
                                t("service-area") to { FieldText(t(v.area)) },
                                t("status") to { AppBadge(if (v.status == "aktif") t("active") else t("inactive"), if (v.status == "aktif") BadgeVariant.Success else BadgeVariant.Secondary) },
                                t("email") to { FieldText(v.externalEmail) }, t("phone") to { FieldText(v.externalTelepon) },
                                s.t("available-schedule") to { FieldText(v.jadwal.joinToString(", ") { s.t(scheduleKey(it)) }) },
                            ),
                            leading = { InitialsAvatar(volName(v)) },
                            actions = {
                                RowActions(Rbac.canEdit(role), Rbac.canDelete(role),
                                    onEdit = { volEdit = VolunteerInput(v.id, v.memberId, v.externalNama.orEmpty(), v.externalEmail.orEmpty(), v.externalTelepon.orEmpty(), v.area, v.jadwal, v.status) },
                                    onDelete = { vm.deleteVolunteer(v.id) })
                            },
                        )
                    }
                    if (filtered.isEmpty()) item { EmptyText() }
                }
                "schedule" -> items(SCHEDULES) { sch ->
                    val list = filtered.filter { sch in it.jadwal }
                    AppCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(t(scheduleKey(sch)), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            if (list.isEmpty()) EmptyText()
                            list.forEach { v ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(volName(v), color = Color.White, fontSize = 14.sp)
                                    AppBadge(t(v.area), BadgeVariant.Outline)
                                }
                            }
                        }
                    }
                }
                else -> {
                    items(filteredAsg, key = { it.id }) { a ->
                        DataRowCard(
                            volNameById(a.volunteerId),
                            listOf(
                                t("event") to { FieldText(eventName(a.eventId)) }, t("date") to { FieldText(Format.date(a.tanggal, s.lang)) },
                                t("assignment-time") to { FieldText(a.waktu?.take(5)) }, t("location") to { FieldText(a.tempat) },
                                t("task") to { FieldText(a.tugas) }, t("status") to { AppBadge(a.status, BadgeVariant.Secondary) },
                            ),
                            actions = {
                                RowActions(Rbac.canEdit(role), Rbac.canDelete(role),
                                    onEdit = { asgEdit = AssignmentInput(a.id, a.volunteerId, a.eventId, a.tanggal, a.waktu?.take(5).orEmpty(), a.tempat.orEmpty(), a.tugas.orEmpty(), a.catatan.orEmpty()) },
                                    onDelete = { vm.deleteAssignment(a.id) })
                            },
                        )
                    }
                    if (filteredAsg.isEmpty()) item { EmptyText() }
                }
            }
        }

        volEdit?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            var fromMember by remember(init) { mutableStateOf(init.memberId != null || init.id == null) }
            AppDialog(t("add-volunteer"), { volEdit = null }) {
                LabeledField(t("volunteer-source")) {
                    AppSelect(if (fromMember) "member" else "external", listOf(Option("member", t("from-member")), Option("external", t("external"))), { fromMember = it == "member" })
                }
                if (fromMember) LabeledField(t("select-member")) {
                    AppSelect(f.memberId?.toString().orEmpty(), data.members.map { Option(it.id.toString(), it.nama) }, { f = f.copy(memberId = it.toLongOrNull()) }, placeholder = t("select"))
                } else {
                    LabeledField(t("name")) { AppTextField(f.externalNama.orEmpty(), { f = f.copy(externalNama = it) }) }
                    LabeledField(t("email")) { AppTextField(f.externalEmail.orEmpty(), { f = f.copy(externalEmail = it) }) }
                    LabeledField(t("phone")) { AppTextField(f.externalTelepon.orEmpty(), { f = f.copy(externalTelepon = it) }) }
                }
                LabeledField(t("service-area")) { AppSelect(f.area, AREAS.map { Option(it, t(it)) }, { f = f.copy(area = it) }) }
                LabeledField(t("available-schedule")) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SCHEDULES.forEach { sch ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AppSwitch(sch in f.jadwal, { on -> f = f.copy(jadwal = if (on) f.jadwal + sch else f.jadwal - sch) })
                                Text(t(scheduleKey(sch)), color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
                LabeledField(t("status")) { AppSelect(f.status, listOf(Option("aktif", t("active")), Option("tidak-aktif", t("inactive"))), { f = f.copy(status = it) }) }
                DialogActions({ volEdit = null }, {
                    val ok = if (fromMember) f.memberId != null else !f.externalNama.isNullOrBlank()
                    if (ok) vm.saveVolunteer(if (fromMember) f.copy(externalNama = null, externalEmail = null, externalTelepon = null) else f.copy(memberId = null)) { volEdit = null }
                }, saving = busy)
            }
        }

        asgEdit?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            AppDialog(t("add-assignment"), { asgEdit = null }) {
                LabeledField(t("volunteer")) { AppSelect(f.volunteerId.toString(), data.volunteers.map { Option(it.id.toString(), volName(it)) }, { f = f.copy(volunteerId = it.toLongOrNull() ?: f.volunteerId) }, placeholder = t("select-volunteer")) }
                LabeledField(t("event")) { AppSelect(f.eventId?.toString().orEmpty(), listOf(Option("", t("none"))) + data.events.map { Option(it.id.toString(), it.nama) }, { f = f.copy(eventId = it.toLongOrNull()) }) }
                LabeledField(t("assignment-date")) { DateField(f.tanggal, { f = f.copy(tanggal = it) }) }
                LabeledField(t("assignment-time")) { TimeField(f.waktu, { f = f.copy(waktu = it) }) }
                LabeledField(t("location")) { AppTextField(f.tempat, { f = f.copy(tempat = it) }) }
                LabeledField(t("task")) { AppTextField(f.tugas, { f = f.copy(tugas = it) }) }
                LabeledField(t("notes")) { AppTextField(f.catatan, { f = f.copy(catatan = it) }, singleLine = false, minHeight = 80.dp) }
                DialogActions({ asgEdit = null }, { if (f.volunteerId != 0L && f.tanggal.isNotBlank()) vm.saveAssignment(f) { asgEdit = null } }, saving = busy)
            }
        }
    }
}
