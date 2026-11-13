package com.growthaxys.churchmanagements.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.growthaxys.churchmanagements.core.Format
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
    val eventCount: Int,
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
        return ReportsData(
            members = members,
            attendance = events.attendance(),
            donations = giving.donations(),
            eventNames = eventList.associate { it.id to it.nama },
            memberNames = members.associate { it.id to it.nama },
            eventCount = eventList.size,
            groupCount = people.groups().size,
            volunteerCount = volunteers.volunteers().size,
        )
    }
}

@Composable
fun ReportsScreen(padding: PaddingValues, vm: ReportsViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Cards) { d ->
        val s = LocalStrings.current
        var preview by remember { mutableStateOf<Triple<String, String, ExportRows>?>(null) }
        val membersTitle = t("members-report")
        val attendanceTitle = t("attendance-report")
        val donationsTitle = t("donations-report")
        val systemTitle = t("system-stats")

        fun memberRows(): ExportRows = d.members.map {
            mapOf(
                s.t("exp-name") to it.nama,
                s.t("email") to it.email,
                s.t("phone") to it.telepon,
                s.t("gender") to it.jk,
                s.t("status") to it.status,
                s.t("join-date") to it.joinDate,
            )
        }
        fun attendanceRows(): ExportRows = d.attendance.map {
            mapOf(
                s.t("date") to it.tanggal,
                s.t("event") to d.eventNames[it.eventId],
                s.t("name") to d.memberNames[it.memberId],
                s.t("checkin-time") to it.waktu,
                s.t("status") to it.status,
            )
        }
        fun donationRows(): ExportRows = d.donations.map {
            mapOf(
                s.t("exp-date") to it.tanggal,
                s.t("donor") to it.donors?.nama,
                s.t("exp-type") to (it.customType ?: it.tipe),
                s.t("exp-amount") to it.jumlah,
                s.t("exp-desc") to it.keterangan,
            )
        }
        fun systemRows(): ExportRows = listOf(
            mapOf(s.t("metric") to s.t("total-members"), s.t("value") to d.members.size),
            mapOf(s.t("metric") to s.t("total-events"), s.t("value") to d.eventCount),
            mapOf(s.t("metric") to s.t("total-groups"), s.t("value") to d.groupCount),
            mapOf(s.t("metric") to s.t("total-volunteers"), s.t("value") to d.volunteerCount),
            mapOf(s.t("metric") to s.t("total-attendance"), s.t("value") to d.attendance.size),
            mapOf(s.t("metric") to s.t("total-donations"), s.t("value") to Format.rupiah(d.donations.sumOf { it.jumlah })),
        )

        ModulePage(padding) {
            item { PageHeader(t("reports"), t("reports-system")) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportTile(t("members-report"), t("members-stats"), Icons.Outlined.People, Modifier.weight(1f)) {
                        preview = Triple(membersTitle, "members-report", memberRows())
                    }
                    ReportTile(t("attendance-report"), t("attendance-data-report"), Icons.Outlined.Checklist, Modifier.weight(1f)) {
                        preview = Triple(attendanceTitle, "attendance-report", attendanceRows())
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportTile(t("donations-report"), t("donations-summary"), Icons.Outlined.VolunteerActivism, Modifier.weight(1f)) {
                        preview = Triple(donationsTitle, "donations-report", donationRows())
                    }
                    ReportTile(t("system-stats"), t("system-overview"), Icons.Outlined.CalendarMonth, Modifier.weight(1f)) {
                        preview = Triple(systemTitle, "system-stats", systemRows())
                    }
                }
            }
            preview?.let { (title, filename, rows) ->
                item {
                    AppCard {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(title, Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2)
                                ExportButton(title, filename, { rows })
                                AppIconButton(Icons.Outlined.Close, t("close"), { preview = null })
                            }
                            if (rows.isEmpty()) EmptyText()
                            rows.take(100).forEach { row ->
                                val entries = row.entries.toList()
                                DataRowCard(
                                    entries.firstOrNull()?.value?.toString() ?: "-",
                                    entries.drop(1).map { entry -> entry.key to { FieldText(entry.value?.toString()) } },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportTile(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    AppCard(modifier.clickable(onClick = onClick)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                Modifier.size(36.dp).background(Tokens.Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(20.dp), tint = Tokens.Primary)
            }
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White, textAlign = TextAlign.Center, maxLines = 2)
            Text(subtitle, fontSize = 11.sp, color = Tokens.MutedForeground, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}
