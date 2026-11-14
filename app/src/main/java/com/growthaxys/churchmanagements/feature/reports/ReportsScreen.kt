package com.growthaxys.churchmanagements.feature.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Attendance
import com.growthaxys.churchmanagements.data.model.Donation
import com.growthaxys.churchmanagements.data.model.Member
import com.growthaxys.churchmanagements.data.repo.EventsRepository
import com.growthaxys.churchmanagements.data.repo.GivingRepository
import com.growthaxys.churchmanagements.data.repo.PeopleRepository
import com.growthaxys.churchmanagements.data.repo.VolunteerRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ReportsData(
    val members: List<Member>,
    val attendance: List<Attendance>,
    val donations: List<Donation>,
    val eventNames: Map<Long, String>,
    val memberNames: Map<Long, String>,
    val groupCount: Int,
    val volunteerCount: Int,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val people: PeopleRepository,
    private val events: EventsRepository,
    private val giving: GivingRepository,
    private val volunteers: VolunteerRepository,
) : FeatureViewModel<ReportsData>() {
    override suspend fun fetch(): ReportsData {
        val members = people.members()
        val eventList = events.eventLites()
        val attendance = events.attendance()
        val donations = giving.donations()
        val groups = people.groups()
        val volunteerList = volunteers.volunteers()
        return ReportsData(
            members, attendance, donations,
            eventList.associate { it.id to it.nama },
            members.associate { it.id to it.nama },
            groups.size, volunteerList.size,
        )
    }
}

@Composable
fun ReportsScreen(padding: PaddingValues, vm: ReportsViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Cards) { d ->
        val s = LocalStrings.current
        var selected by remember { mutableStateOf<Pair<String, ExportRows>?>(null) }
        val reportCards = listOf(
            Triple(t("members-report"), t("members-stats"), "members-report") to {
                d.members.map { mapOf(s.t("exp-name") to it.nama, s.t("email") to it.email, s.t("phone") to it.telepon, s.t("gender") to it.jk, s.t("status") to it.status) }
            },
            Triple(t("attendance-report"), t("attendance-data-report"), "attendance-report") to {
                d.attendance.map { mapOf(s.t("date") to it.tanggal, s.t("event") to d.eventNames[it.eventId], s.t("name") to d.memberNames[it.memberId], s.t("checkin-time") to it.waktu, s.t("status") to it.status) }
            },
            Triple(t("donations-report"), t("donations-summary"), "donations-report") to {
                d.donations.map { mapOf(s.t("exp-date") to it.tanggal, s.t("donor") to it.donors?.nama, s.t("exp-type") to (it.customType ?: it.tipe), s.t("exp-amount") to it.jumlah, s.t("exp-desc") to it.keterangan) }
            },
            Triple(t("system-stats"), t("system-overview"), "system-stats") to {
                listOf(
                    mapOf("Metrik" to "Total Jemaat", "Nilai" to d.members.size),
                    mapOf("Metrik" to "Total Events", "Nilai" to d.eventNames.size),
                    mapOf("Metrik" to "Total Groups", "Nilai" to d.groupCount),
                    mapOf("Metrik" to "Total Relawan", "Nilai" to d.volunteerCount),
                    mapOf("Metrik" to "Total Kehadiran", "Nilai" to d.attendance.size),
                    mapOf("Metrik" to "Total Donasi", "Nilai" to d.donations.sumOf { it.jumlah }),
                )
            },
        )

        ModulePage(padding) {
            item { PageHeader(t("reports"), t("reports-system")) }
            reportCards.forEach { (meta, rows) ->
                item {
                    ReportCard(meta.first, meta.second, meta.third, rows) {
                        selected = meta.first to rows()
                    }
                }
            }
            selected?.let { (title, rows) ->
                item {
                    AppCard {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                AppButton(t("close"), { selected = null }, variant = BtnVariant.Ghost, height = 34.dp)
                            }
                            if (rows.isEmpty()) {
                                EmptyText()
                            } else {
                                val headers = rows.first().keys.toList()
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    headers.forEach { Text(it, Modifier.weight(1f), fontSize = 11.sp, color = Tokens.Primary, fontWeight = FontWeight.SemiBold) }
                                }
                                rows.take(100).forEach { row ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        headers.forEach { key -> Text(row[key]?.toString() ?: "-", Modifier.weight(1f), fontSize = 12.sp, color = Color.White) }
                                    }
                                }
                                if (rows.size > 100) Text("Menampilkan 100 dari ${rows.size} baris", fontSize = 11.sp, color = Tokens.MutedForeground)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(title: String, subtitle: String, file: String, rows: () -> ExportRows, onOpen: () -> Unit) {
    AppCard {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = Tokens.MutedForeground)
            }
            ExportButton(title, file, rows)
        }
    }
}
