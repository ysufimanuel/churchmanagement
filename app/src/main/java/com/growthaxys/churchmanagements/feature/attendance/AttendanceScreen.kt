package com.growthaxys.churchmanagements.feature.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Attendance
import com.growthaxys.churchmanagements.data.model.EventLite
import com.growthaxys.churchmanagements.data.model.NamedRef
import com.growthaxys.churchmanagements.data.repo.EventsRepository
import com.growthaxys.churchmanagements.data.repo.PeopleRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class AttendanceData(val attendance: List<Attendance>, val events: List<EventLite>, val members: List<NamedRef>)

@HiltViewModel
class AttendanceViewModel @Inject constructor(private val repo: EventsRepository, private val people: PeopleRepository) : FeatureViewModel<AttendanceData>() {
    override suspend fun fetch() = AttendanceData(repo.attendance(), repo.eventLites(), people.memberNames())
    fun checkIn(e: Long, m: Long, onOk: () -> Unit) = mutate("checkin-ok", onOk) { repo.checkIn(e, m) }
}

@Composable
fun AttendanceScreen(role: Role, padding: PaddingValues, vm: AttendanceViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Tabs) { data ->
        val s = LocalStrings.current
        var tab by remember { mutableStateOf("checkin") }
        var eventId by remember { mutableStateOf("") }
        var memberSearch by remember { mutableStateOf("") }
        var selected by remember { mutableStateOf<Long?>(null) }
        var filterEvent by remember { mutableStateOf("") }
        var filterDate by remember { mutableStateOf("") }
        val eventName = { id: Long -> data.events.firstOrNull { it.id == id }?.nama ?: "-" }
        val memberName = { id: Long -> data.members.firstOrNull { it.id == id }?.nama ?: "-" }
        val filtered = data.attendance.filter { (filterEvent.isEmpty() || it.eventId.toString() == filterEvent) && (filterDate.isEmpty() || it.tanggal == filterDate) }
        val results = if (memberSearch.isNotEmpty() && selected == null) data.members.filter { it.nama.contains(memberSearch, true) }.take(6) else emptyList()
        val perDate = data.attendance.groupingBy { it.tanggal }.eachCount().toSortedMap().entries.toList().takeLast(14).map { it.key.substring(5) to it.value }
        val perEvent = data.attendance.groupingBy { eventName(it.eventId) }.eachCount()
        val topEvent = perEvent.maxByOrNull { it.value }?.key ?: "-"

        ModulePage(padding) {
            item { PageHeader(t("attendance"), t("attendance-system")) }
            item { AppTabs(listOf(TabItem("checkin", t("checkin")), TabItem("data", t("attendance-data")), TabItem("report", t("report"))), tab, { tab = it }) }

            when (tab) {
                "checkin" -> item {
                    AppCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(t("checkin-attendance"), fontSize = 16.sp, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            LabeledField(t("select-event")) {
                                AppSelect(eventId, data.events.filter { it.status != "completed" }.map { Option(it.id.toString(), it.nama) }, { eventId = it }, placeholder = t("select"))
                            }
                            LabeledField(t("search-member")) {
                                AppTextField(memberSearch, { memberSearch = it; selected = null }, placeholder = t("search-member"))
                            }
                            results.forEach { m ->
                                Text(m.nama, Modifier.fillMaxWidth().clickable { selected = m.id; memberSearch = m.nama }.padding(vertical = 8.dp, horizontal = 4.dp), color = Color.White, fontSize = 14.sp)
                            }
                            if (Rbac.canAdd(role)) AppButton(t("checkin"), {
                                vm.checkIn(eventId.toLong(), selected!!) { memberSearch = ""; selected = null }
                            }, Modifier.fillMaxWidth(), enabled = eventId.isNotEmpty() && selected != null)
                            else Text(t("view-only"), fontSize = 13.sp, color = Tokens.MutedForeground)
                            Text("${t("today-attendance")}: " + data.attendance.count { it.tanggal == java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toLocalDate().toString() }, fontSize = 13.sp, color = Tokens.MutedForeground)
                        }
                    }
                }
                "data" -> {
                    item {
                        FilterRow {
                            AppSelect(filterEvent, listOf(Option("", t("all-events"))) + data.events.map { Option(it.id.toString(), it.nama) }, { filterEvent = it })
                            DateField(filterDate, { filterDate = it }, placeholder = t("date"))
                            ExportButton(t("attendance-report"), "attendance", {
                                filtered.map { mapOf(s.t("date") to it.tanggal, s.t("event") to eventName(it.eventId), s.t("name") to memberName(it.memberId), s.t("checkin-time") to it.waktu, s.t("status") to it.status) }
                            })
                        }
                    }
                    items(filtered, key = { it.id }) { a ->
                        DataRowCard(memberName(a.memberId), listOf(
                            t("date") to { FieldText(Format.date(a.tanggal, s.lang)) }, t("event") to { FieldText(eventName(a.eventId)) },
                            t("checkin-time") to { FieldText(a.waktu?.take(5)) }, t("status") to { FieldText(a.status) },
                        ))
                    }
                    if (filtered.isEmpty()) item { EmptyText() }
                }
                else -> {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard(t("total-attendance"), data.attendance.size.toString(), Modifier.weight(1f))
                            StatCard(t("average"), (if (perDate.isNotEmpty()) Math.round(data.attendance.size.toDouble() / perDate.size) else 0).toString(), Modifier.weight(1f))
                            StatCard(t("top-event"), topEvent, Modifier.weight(1.4f))
                        }
                    }
                    item { AppCard { Column(Modifier.padding(16.dp)) { Text(t("attendance-stats"), fontSize = 16.sp, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold); BarChartView(perDate, Modifier.padding(top = 12.dp), 220.dp) } } }
                }
            }
        }
    }
}
