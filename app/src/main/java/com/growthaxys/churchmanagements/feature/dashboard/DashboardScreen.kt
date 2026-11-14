package com.growthaxys.churchmanagements.feature.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/** Mobile layout follows the web dashboard: one-column stat cards, then independent content cards. */
@Composable
fun DashboardScreen(role: Role, userName: String, padding: PaddingValues, onOpen: (String) -> Unit, vm: DashboardViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Dashboard) { d ->
        val s = LocalStrings.current
        val tiles = listOf(
            Tile4(t("total-members"), d.totalMembers.toDouble(), false, Icons.Outlined.People, Color(0xFFFB923C), "members"),
            Tile4(t("attendance-this-week"), d.attendanceWeek.toDouble(), false, Icons.Outlined.Checklist, Color(0xFF60A5FA), "attendance"),
            Tile4(t("donations-this-month"), d.donationsMonth, true, Icons.Outlined.VolunteerActivism, Color(0xFF34D399), "donations"),
            Tile4(t("active-events"), d.activeEvents.toDouble(), false, Icons.Outlined.CalendarMonth, Color(0xFFC084FC), "events"),
            Tile4(t("total-groups"), d.totalGroups.toDouble(), false, Icons.Outlined.ViewInAr, Color(0xFFF87171), "groups"),
            Tile4(t("active-volunteers"), d.activeVolunteers.toDouble(), false, Icons.Outlined.Handshake, Color(0xFFFBBF24), "volunteers"),
        )
        val recentDonations = d.recentDonations.map {
            MRow(it.donors?.nama ?: "-", "${it.tipe} · ${Format.date(it.tanggal, s.lang)}", Format.rupiahCompact(it.jumlah, s.lang), Tokens.Emerald400, true)
        }
        val upcomingEvents = d.upcomingEvents.map {
            MRow(it.nama, it.lokasi ?: "", Format.date(it.startAt, s.lang), Tokens.Primary, false)
        }
        val recentActivity = d.activities.map {
            MRow(it.action ?: "", it.detail ?: "", Format.timeAgo(it.createdAt, s.lang), Tokens.MutedForeground, false)
        }

        ModulePage(padding) {
            item {
                Column(Modifier.enterAnim(0)) {
                    Text(t("dashboard"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${t("welcome-back")}, $userName!", fontSize = 14.sp, color = Tokens.MutedForeground)
                }
            }
            tiles.forEachIndexed { index, tile ->
                item(key = "dashboard-stat-${tile.route}") {
                    StatTile(tile, s.lang, Modifier.fillMaxWidth().enterAnim(delayMs = 70 + index * 45, dx = (-10).dp, dy = 14.dp, durationMs = 420)) {
                        onOpen(tile.route)
                    }
                }
            }
            item {
                DashboardListCard(t("recent-activity"), recentActivity, "reports", onOpen)
            }
            item {
                DashboardListCard(t("upcoming-events"), upcomingEvents, "events", onOpen)
            }
            item {
                ChartCard(t("attendance-stats"), Modifier.fillMaxWidth().enterAnim(delayMs = 120, dx = (-8).dp, dy = 10.dp)) {
                    BarChartView(d.attendanceChart, Modifier.padding(top = 8.dp), 190.dp)
                }
            }
            item {
                ChartCard(t("member-distribution"), Modifier.fillMaxWidth().enterAnim(delayMs = 160, dx = (-8).dp, dy = 10.dp)) {
                    DonutChartView(d.memberChart.map { it.first to it.second.toDouble() }, Modifier.padding(top = 8.dp), 160.dp, centerLabel = t("members"))
                }
            }
            item {
                ChartCard(t("donation-trend"), Modifier.fillMaxWidth().enterAnim(delayMs = 200, dx = (-8).dp, dy = 10.dp), Icons.Outlined.TrendingUp) {
                    LineChartView(
                        d.donationTrend.map { Format.monthShort(it.first, s.lang) },
                        listOf(Triple(t("donations"), d.donationTrend.map { it.second }, Tokens.Emerald400)),
                        Modifier.padding(top = 8.dp), 190.dp, area = true,
                    )
                }
            }
            item {
                DashboardListCard(t("recent-donations"), recentDonations, "donations", onOpen)
            }
        }
    }
}

private data class Tile4(val label: String, val value: Double, val rupiah: Boolean, val icon: ImageVector, val color: Color, val route: String)
private data class MRow(val title: String, val sub: String, val right: String, val rightColor: Color, val bold: Boolean)

@Composable
private fun DashboardListCard(title: String, rows: List<MRow>, route: String, onOpen: (String) -> Unit) {
    AppCard(Modifier.fillMaxWidth().enterAnim(dx = (-8).dp, dy = 10.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(t("view-all"), fontSize = 12.sp, color = Tokens.Primary, modifier = Modifier.clickable { onOpen(route) }.padding(4.dp))
            }
            if (rows.isEmpty()) {
                Text(t("none"), fontSize = 14.sp, color = Tokens.MutedForeground, modifier = Modifier.padding(vertical = 12.dp))
            } else {
                rows.take(4).forEachIndexed { index, row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(row.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (row.sub.isNotBlank()) Text(row.sub, fontSize = 12.sp, color = Tokens.MutedForeground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(row.right, fontSize = 12.sp, color = row.rightColor, fontWeight = if (row.bold) FontWeight.SemiBold else FontWeight.Normal)
                    }
                    if (index < minOf(rows.size, 4) - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .1f)))
                }
            }
        }
    }
}

@Composable
private fun StatTile(tile: Tile4, lang: com.growthaxys.churchmanagements.core.Lang, modifier: Modifier, onClick: () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(tile.value) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(900, 250, androidx.compose.animation.core.FastOutSlowInEasing))
    }
    val value = tile.value * anim.value
    val text = if (tile.rupiah) Format.rupiahCompact(Math.round(value).toDouble(), lang) else Math.round(value).toString()
    AppCard(modifier.clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(tile.color.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                Icon(tile.icon, null, tint = tile.color, modifier = Modifier.size(24.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(tile.label, fontSize = 13.sp, color = Tokens.MutedForeground, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, modifier: Modifier = Modifier, icon: ImageVector? = null, content: @Composable ColumnScope.() -> Unit) = AppCard(modifier) {
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, null, tint = Tokens.Emerald400, modifier = Modifier.size(16.dp))
            Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        content()
    }
}
