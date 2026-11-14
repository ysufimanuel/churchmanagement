package com.growthaxys.churchmanagements.feature.events

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Event
import com.growthaxys.churchmanagements.data.model.EventParticipant
import com.growthaxys.churchmanagements.data.model.NamedRef
import com.growthaxys.churchmanagements.data.repo.EventInput
import com.growthaxys.churchmanagements.data.repo.EventsRepository
import com.growthaxys.churchmanagements.data.repo.PeopleRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private val TYPES = listOf("ibadah", "seminar", "retret", "sosial", "lainnya")
private fun typeKey(ty: String) = when (ty) { "ibadah" -> "worship"; "sosial" -> "social-activity"; "lainnya" -> "other"; else -> ty }

data class EventsData(val events: List<Event>, val participants: List<EventParticipant>, val members: List<NamedRef>)

@HiltViewModel
class EventsViewModel @Inject constructor(private val repo: EventsRepository, private val people: PeopleRepository) : FeatureViewModel<EventsData>() {
    override suspend fun fetch() = EventsData(repo.events(), repo.participants(), people.memberNames())
    fun save(i: EventInput, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveEvent(i) }
    fun delete(id: Long) = mutate { repo.deleteEvent(id) }
    fun addParticipant(e: Long, m: Long?, manual: String?) = mutate { repo.addParticipant(e, m, manual) }
    fun removeParticipant(id: Long) = mutate { repo.removeParticipant(id) }
}

private fun statusVariant(s: String) = when (s) { "ongoing" -> BadgeVariant.Success; "completed" -> BadgeVariant.Secondary; else -> BadgeVariant.Default }

/** ISO instant -> "yyyy-MM-ddTHH:mm" in device zone (datetime-local value). */
private fun toLocalInput(iso: String?): String = runCatching {
    java.time.OffsetDateTime.parse(iso).atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime().withSecond(0).withNano(0).toString()
}.getOrDefault("")

@Composable
fun EventsScreen(role: Role, padding: PaddingValues, initialQuery: String, vm: EventsViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Cards) { data ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        val s = LocalStrings.current
        var search by remember { mutableStateOf(initialQuery) }
        var status by remember { mutableStateOf("") }
        var editing by remember { mutableStateOf<EventInput?>(null) }
        var parts by remember { mutableStateOf<Long?>(null) }
        val q = search.trim().lowercase()
        val filtered = data.events.filter { e ->
            (q.isEmpty() || listOf(e.nama, e.tipe, e.customType, e.lokasi, e.deskripsi).any { (it ?: "").lowercase().contains(q) }) && (status.isEmpty() || e.status == status)
        }

        ModulePage(padding) {
            item { PageHeader(t("events"), t("manage-events")) }
            item {
                FilterRow {
                    SearchField(search, { search = it }, t("search-events"))
                    AppSelect(status, listOf(Option("", t("all-status")), Option("upcoming", t("upcoming")), Option("ongoing", t("ongoing")), Option("completed", t("completed"))), { status = it })
                    if (Rbac.canAdd(role)) AppButton(t("add-event"), { editing = EventInput(null, "", "ibadah", "", "", "", "", "", null, "upcoming") }, icon = Icons.Outlined.Add)
                }
            }
            items(filtered, key = { it.id }) { e ->
                val count = data.participants.count { it.eventId == e.id }
                AppCard {
                    CardHeaderRow {
                        Column(Modifier.weight(1f)) {
                            Text(e.nama, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                AppBadge(t(e.status), statusVariant(e.status))
                                AppBadge(e.customType ?: t(typeKey(e.tipe)), BadgeVariant.Outline)
                            }
                        }
                        RowActions(Rbac.canEdit(role), Rbac.canDelete(role),
                            onEdit = { editing = EventInput(e.id, e.nama, e.tipe, e.customType.orEmpty(), toLocalInput(e.startAt), toLocalInput(e.endAt), e.lokasi.orEmpty(), e.deskripsi.orEmpty(), e.kapasitas, e.status) },
                            onDelete = { vm.delete(e.id) })
                    }
                    CardBody {
                        FieldText(Format.dateTime(e.startAt, s.lang), Tokens.MutedForeground)
                        FieldText(e.lokasi, Tokens.MutedForeground)
                        e.deskripsi?.takeIf { it.isNotBlank() }?.let { FieldText(it, Tokens.MutedForeground) }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            AppBadge("$count${e.kapasitas?.let { " / $it" } ?: ""}", BadgeVariant.Secondary)
                            AppButton(t("act-participants"), { parts = e.id }, variant = BtnVariant.Secondary, icon = Icons.Outlined.People, height = 36.dp)
                        }
                    }
                }
            }
            if (filtered.isEmpty()) item { EmptyText() }
        }

        editing?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            AppDialog(t("add-event"), { editing = null }) {
                LabeledField(t("event-name")) { AppTextField(f.nama, { f = f.copy(nama = it) }) }
                LabeledField(t("event-type")) { AppSelect(f.tipe, TYPES.map { Option(it, t(typeKey(it))) }, { f = f.copy(tipe = it) }) }
                if (f.tipe == "lainnya") LabeledField(t("custom-type")) { AppTextField(f.customType, { f = f.copy(customType = it) }) }
                LabeledField(t("start-date")) { DateTimeField(f.startAt, { f = f.copy(startAt = it) }) }
                LabeledField(t("end-date")) { DateTimeField(f.endAt, { f = f.copy(endAt = it) }) }
                LabeledField(t("location")) { AppTextField(f.lokasi, { f = f.copy(lokasi = it) }) }
                LabeledField(t("capacity")) { AppTextField(f.kapasitas?.toString().orEmpty(), { f = f.copy(kapasitas = it.toIntOrNull()) }, keyboardType = KeyboardType.Number) }
                LabeledField(t("event-status")) { AppSelect(f.status, listOf(Option("upcoming", t("upcoming")), Option("ongoing", t("ongoing")), Option("completed", t("completed"))), { f = f.copy(status = it) }) }
                LabeledField(t("description")) { AppTextField(f.deskripsi, { f = f.copy(deskripsi = it) }, singleLine = false, minHeight = 80.dp) }
                DialogActions({ editing = null }, { if (f.nama.isNotBlank() && f.startAt.isNotBlank()) vm.save(f) { editing = null } }, saving = busy)
            }
        }

        parts?.let { eid ->
            var member by remember { mutableStateOf("") }
            var manual by remember { mutableStateOf("") }
            val list = data.participants.filter { it.eventId == eid }
            AppDialog(t("event-participants"), { parts = null }) {
                if (Rbac.canAdd(role)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppSelect(member, data.members.map { Option(it.id.toString(), it.nama) }, { member = it }, Modifier.weight(1f), placeholder = t("select-member"))
                        AppButton(t("add"), { member.toLongOrNull()?.let { vm.addParticipant(eid, it, null); member = "" } }, enabled = member.isNotEmpty())
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppTextField(manual, { manual = it }, Modifier.weight(1f), placeholder = t("add-manual-participant"))
                        AppButton(t("add"), { vm.addParticipant(eid, null, manual.trim()); manual = "" }, enabled = manual.isNotBlank())
                    }
                }
                if (list.isEmpty()) EmptyText()
                list.forEach { p ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.namaManual ?: data.members.firstOrNull { it.id == p.memberId }?.nama ?: "-", color = Color.White, fontSize = 14.sp)
                        if (Rbac.canDelete(role)) AppIconButton(Icons.Outlined.Close, t("act-delete"), { vm.removeParticipant(p.id) }, tint = Tokens.Red400)
                    }
                }
            }
        }
    }
}
