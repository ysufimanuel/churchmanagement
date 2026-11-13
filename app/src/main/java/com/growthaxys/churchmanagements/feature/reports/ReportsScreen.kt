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
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Cards) { d ->
        val s = LocalStrings.current
        ModulePage(padding) {
            item { PageHeader(t("reports"), t("reports-system")) }
            item {
                ReportCard(t("members-report"), t("members-stats"), "members-report") {
                    d.members.map { mapOf(s.t("exp-name") to it.nama, s.t("email") to it.email, s.t("phone") to it.telepon, s.t("gender") to it.jk, s.t("status") to it.status) }
                }
            }
            item {
                ReportCard(t("attendance-report"), t("attendance-data-report"), "attendance-report") {
                    d.attendance.map { mapOf(s.t("date") to it.tanggal, s.t("event") to d.eventNames[it.eventId], s.t("name") to d.memberNames[it.memberId], s.t("checkin-time") to it.waktu, s.t("status") to it.status) }
                }
            }
            item {
                ReportCard(t("donations-report"), t("manage-donations"), "donations-report") {
                    d.donations.map { mapOf(s.t("exp-date") to it.tanggal, s.t("donor") to it.donors?.nama, s.t("exp-type") to (it.customType ?: it.tipe), s.t("exp-amount") to it.jumlah, s.t("exp-desc") to it.keterangan) }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(title: String, subtitle: String, file: String, rows: () -> ExportRows) {
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
