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
    val eventCount: Int,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val people: PeopleRepository,
    private val events: EventsRepository,
    private val giving: GivingRepository,
) : FeatureViewModel<ReportsData>() {
    override suspend fun fetch(): ReportsData {
        val members = people.members()
        val evs = events.eventLites()
        return ReportsData(
            members = members,
            attendance = events.attendance(),
            donations = giving.donations(),
            eventNames = evs.associate { it.id to it.nama },
            memberNames = members.associate { it.id to it.nama },
            eventCount = evs.size,
        )
    }
}

@Composable
fun ReportsScreen(padding: PaddingValues, vm: ReportsViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Cards) { d ->
        val s = LocalStrings.current
        val memberGender = d.members.groupingBy { it.jk?.takeIf(String::isNotBlank) ?: t("other") }
            .eachCount().map { it.key to it.value.toDouble() }
        val attendanceByMonth = d.attendance.groupingBy { it.tanggal.take(7) }
            .eachCount().toSortedMap().toList().takeLast(6).map { it.first to it.second }
        val donationByMonth = d.donations.groupBy { it.tanggal.take(7) }
            .mapValues { (_, rows) -> rows.sumOf { it.jumlah } }.toSortedMap().toList().takeLast(6)
        val donationByType = d.donations.groupBy { it.customType?.takeIf(String::isNotBlank) ?: it.tipe }
            .map { it.key to it.value.sumOf { row -> row.jumlah } }

        ModulePage(padding) {
            item { PageHeader(t("reports"), t("reports-system")) }
            item {
                ReportCard(t("members-report"), t("members-stats"), "members-report", {
                    DonutChartView(memberGender, Modifier.padding(horizontal = 16.dp), 176.dp, t("members"))
                }) {
                    d.members.map { mapOf(s.t("exp-name") to it.nama, s.t("email") to it.email, s.t("phone") to it.telepon, s.t("gender") to it.jk, s.t("status") to it.status) }
                }
            }
            item {
                ReportCard(t("attendance-report"), t("attendance-data-report"), "attendance-report", {
                    BarChartView(attendanceByMonth.map { it.first to it.second }, Modifier.padding(horizontal = 16.dp), 176.dp)
                }) {
                    d.attendance.map { mapOf(s.t("date") to it.tanggal, s.t("event") to d.eventNames[it.eventId], s.t("name") to d.memberNames[it.memberId], s.t("checkin-time") to it.waktu, s.t("status") to it.status) }
                }
            }
            item {
                ReportCard(t("donations-report"), t("manage-donations"), "donations-report", {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LineChartView(
                            donationByMonth.map { it.first },
                            listOf(Triple(t("donations"), donationByMonth.map { it.second }, Tokens.Emerald400)),
                            height = 176.dp,
                            area = true,
                        )
                        DonutChartView(donationByType, height = 164.dp)
                    }
                }) {
                    d.donations.map { mapOf(s.t("exp-date") to it.tanggal, s.t("donor") to it.donors?.nama, s.t("exp-type") to (it.customType ?: it.tipe), s.t("exp-amount") to it.jumlah, s.t("exp-desc") to it.keterangan) }
                }
            }
            item {
                ReportCard(t("system-stats"), t("system-overview"), "system-report", {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(t("total-members"), d.members.size.toString())
                        StatCard(t("active-events"), d.eventCount.toString())
                        StatCard(t("attendance-this-week"), d.attendance.size.toString())
                        StatCard(t("donations-this-month"), Format.rupiah(d.donations.sumOf { it.jumlah }))
                    }
                }) {
                    listOf(
                        mapOf("Metrik" to t("total-members"), "Nilai" to d.members.size),
                        mapOf("Metrik" to t("active-events"), "Nilai" to d.eventCount),
                        mapOf("Metrik" to t("attendance-this-week"), "Nilai" to d.attendance.size),
                        mapOf("Metrik" to t("donations"), "Nilai" to d.donations.sumOf { it.jumlah }),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    subtitle: String,
    file: String,
    chart: @Composable () -> Unit,
    rows: () -> ExportRows,
) {
    var expanded by remember(title) { mutableStateOf(false) }
    AppCard {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = Tokens.MutedForeground)
                Text(if (expanded) "Sembunyikan grafik" else "Ketuk untuk melihat grafik", fontSize = 11.sp, color = Tokens.Primary)
            }
            ExportButton(title, file, rows)
        }
        if (expanded) {
            chart()
            val data = rows()
            if (data.isNotEmpty()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pratinjau data · ${data.size} baris", fontSize = 12.sp, color = Tokens.MutedForeground)
                    data.take(5).forEach { row ->
                        Text(row.values.joinToString("  ·  ") { it?.toString().orEmpty().ifBlank { "-" } }, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
