package com.growthaxys.churchmanagements.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.repo.DashboardData
import com.growthaxys.churchmanagements.data.repo.DashboardRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private val repo: DashboardRepository) : FeatureViewModel<DashboardData>() {
    override suspend fun fetch() = repo.load()
}

/** `onOpen(route)` navigates to a module (stat tiles are tappable like the web dashboard links). */
@Composable
fun DashboardScreen(role: Role, padding: PaddingValues, onOpen: (String) -> Unit, vm: DashboardViewModel = hiltViewModel()) {
    LoadGate(vm) { d ->
        val s = LocalStrings.current
        @Composable fun Tile(label: String, value: String, route: String, mod: Modifier, color: Color = Color.White) =
            StatCard(label, value, mod.clickable { onOpen(route) }, color)

        ModulePage(padding) {
            item { PageHeader(t("dashboard"), t("welcome-back")) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tile(t("total-members"), d.totalMembers.toString(), "members", Modifier.weight(1f))
                    Tile(t("attendance-this-week"), d.attendanceWeek.toString(), "attendance", Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tile(t("donations-this-month"), Format.rupiahCompact(d.donationsMonth, s.lang), "donations", Modifier.weight(1f), Tokens.Emerald400)
                    Tile(t("active-events"), d.activeEvents.toString(), "events", Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tile(t("total-groups"), d.totalGroups.toString(), "groups", Modifier.weight(1f))
                    Tile(t("active-volunteers"), d.activeVolunteers.toString(), "volunteers", Modifier.weight(1f))
                }
            }
            item { ChartCard(t("attendance-this-week")) { BarChartView(d.attendanceChart, Modifier.padding(top = 12.dp), 180.dp) } }
            item { ChartCard(t("member-distribution")) { DonutChartView(d.memberChart.map { it.first to it.second.toDouble() }, Modifier.padding(top = 12.dp), 180.dp) } }
            item {
                ChartCard(t("donation-trend")) {
                    LineChartView(d.donationTrend.map { Format.monthShort(it.first, s.lang) }, listOf(Triple(t("donations"), d.donationTrend.map { it.second }, Tokens.Primary)), Modifier.padding(top = 12.dp), 180.dp, area = true)
                }
            }
            item {
                ChartCard(t("recent-activity")) {
                    if (d.activities.isEmpty()) EmptyText()
                    d.activities.forEach { a ->
                        Column(Modifier.padding(top = 10.dp)) {
                            Text(listOf(a.action, a.detail).filter { !it.isNullOrBlank() }.joinToString(" · "), color = Color.White, fontSize = 14.sp)
                            Text(Format.timeAgo(a.createdAt, s.lang), color = Tokens.MutedForeground, fontSize = 11.sp)
                        }
                    }
                }
            }
            item {
                ChartCard(t("upcoming-events")) {
                    if (d.upcomingEvents.isEmpty()) EmptyText()
                    d.upcomingEvents.forEach { e ->
                        Column(Modifier.padding(top = 10.dp).clickable { onOpen("events") }) {
                            Text(e.nama, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(Format.dateTime(e.startAt, s.lang) + (e.lokasi?.let { " · $it" } ?: ""), color = Tokens.MutedForeground, fontSize = 12.sp)
                        }
                    }
                }
            }
            item {
                ChartCard(t("recent-donations")) {
                    if (d.recentDonations.isEmpty()) EmptyText()
                    d.recentDonations.forEach { x ->
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(x.donors?.nama ?: "-", color = Color.White, fontSize = 14.sp)
                                Text(Format.date(x.tanggal, s.lang), color = Tokens.MutedForeground, fontSize = 11.sp)
                            }
                            Text(Format.rupiah(x.jumlah), color = Tokens.Emerald400, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable ColumnScope.() -> Unit) = AppCard {
    Column(Modifier.padding(16.dp)) {
        Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        content()
    }
}
