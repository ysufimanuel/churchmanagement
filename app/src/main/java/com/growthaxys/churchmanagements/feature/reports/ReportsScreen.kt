package com.growthaxys.churchmanagements.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

data class ReportsData(val members: List<Member>, val attendance: List<Attendance>, val donations: List<Donation>, val eventNames: Map<Long, String>, val memberNames: Map<Long, String>)

@HiltViewModel
class ReportsViewModel @Inject constructor(private val people: PeopleRepository, private val events: EventsRepository, private val giving: GivingRepository) : FeatureViewModel<ReportsData>() {
    override suspend fun fetch(): ReportsData {
        val members = people.members()
        val evs = events.eventLites()
        return ReportsData(members, events.attendance(), giving.donations(), evs.associate { it.id to it.nama }, members.associate { it.id to it.nama })
    }
}

@Composable
fun ReportsScreen(padding: PaddingValues, vm: ReportsViewModel = hiltViewModel()) {
    LoadGate(vm) { d ->
        ModulePage(padding) {
            item { PageHeader(t("reports"), t("reports-system")) }
            item {
                ReportCard(t("members-report"), t("members-stats"), "members-report") {
                    d.members.map { mapOf(t("exp-name") to it.nama, t("email") to it.email, t("phone") to it.telepon, t("gender") to it.jk, t("status") to it.status) }
                }
            }
            item {
                ReportCard(t("attendance-report"), t("attendance-data-report"), "attendance-report") {
                    d.attendance.map { mapOf(t("date") to it.tanggal, t("event") to d.eventNames[it.eventId], t("name") to d.memberNames[it.memberId], t("checkin-time") to it.waktu, t("status") to it.status) }
                }
            }
            item {
                ReportCard(t("donations-report"), t("manage-donations"), "donations-report") {
                    d.donations.map { mapOf(t("exp-date") to it.tanggal, t("donor") to it.donors?.nama, t("exp-type") to (it.customType ?: it.tipe), t("exp-amount") to it.jumlah, t("exp-desc") to it.keterangan) }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(title: String, subtitle: String, file: String, rows: () -> Exporter.Rows) {
    AppCard {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = Tokens.MutedForeground)
            }
            ExportButton(title, file, rows)
        }
    }
}
