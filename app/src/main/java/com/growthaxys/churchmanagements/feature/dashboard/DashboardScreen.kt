package com.growthaxys.churchmanagements.feature.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(role: Role, userName: String, padding: PaddingValues, onOpen: (String) -> Unit, vm: DashboardViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Dashboard) { d ->
        val s = LocalStrings.current
        val none = t("none")
        val tiles = listOf(
            Tile4(t("total-members"), d.totalMembers.toDouble(), false, Icons.Outlined.People, Color(0xFFFB923C), "members"),
            Tile4(t("attendance-this-week"), d.attendanceWeek.toDouble(), false, Icons.Outlined.Checklist, Color(0xFF60A5FA), "attendance"),
            Tile4(t("donations-this-month"), d.donationsMonth, true, Icons.Outlined.VolunteerActivism, Color(0xFF34D399), "donations"),
            Tile4(t("active-events"), d.activeEvents.toDouble(), false, Icons.Outlined.CalendarMonth, Color(0xFFC084FC), "events"),
            Tile4(t("total-groups"), d.totalGroups.toDouble(), false, Icons.Outlined.ViewInAr, Color(0xFFF87171), "groups"),
            Tile4(t("active-volunteers"), d.activeVolunteers.toDouble(), false, Icons.Outlined.Handshake, Color(0xFFFBBF24), "volunteers"),
        )
        val tabs = listOf(
            MobileTab(t("recent-donations"), "donations", d.recentDonations.map {
                MRow(it.donors?.nama ?: "-", "${it.tipe} · ${Format.date(it.tanggal, s.lang)}", Format.rupiahCompact(it.jumlah, s.lang), Tokens.Emerald400, true)
            }),
            MobileTab(t("upcoming-events"), "events", d.upcomingEvents.map {
                MRow(it.nama, it.lokasi ?: "", Format.date(it.startAt, s.lang), Tokens.Primary, false)
            }),
            MobileTab(t("recent-activity"), "reports", d.activities.map {
                MRow(it.action ?: "", it.detail ?: "", Format.timeAgo(it.createdAt, s.lang), Tokens.MutedForeground, false)
            }),
        )
        val listState = rememberLazyListState()

        ModulePage(padding) {
            item {
                Column(Modifier.enterAnim(0)) {
                    Text(t("dashboard"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${t("welcome-back")}, $userName!", fontSize = 14.sp, color = Tokens.MutedForeground)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tiles.chunked(3).forEachIndexed { r, row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEachIndexed { c, tile ->
                                StatTile(tile, s.lang, Modifier.weight(1f).enterAnim(delayMs = 70 + (r * 3 + c) * 45, dx = (-10).dp, dy = 14.dp, durationMs = 420)) { onOpen(tile.route) }
                            }
                        }
                    }
                }
            }
            item {
                LazyRow(
                    state = listState, flingBehavior = rememberSnapFlingBehavior(listState),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth(),
                ) {
                    item {
                        ChartCard(t("attendance-stats"), Modifier.fillParentMaxWidth(0.88f).height(206.dp)) {
                            BarChartView(d.attendanceChart, Modifier.padding(top = 8.dp), 124.dp)
                        }
                    }
                    item {
                        ChartCard(t("donation-trend"), Modifier.fillParentMaxWidth(0.88f).height(206.dp), Icons.Outlined.TrendingUp) {
                            LineChartView(d.donationTrend.map { Format.monthShort(it.first, s.lang) }, listOf(Triple(t("donations"), d.donationTrend.map { it.second }, Tokens.Emerald400)), Modifier.padding(top = 8.dp), 124.dp, area = true)
                        }
                    }
                    item {
                        ChartCard(t("member-distribution"), Modifier.fillParentMaxWidth(0.88f).height(206.dp)) {
                            DonutChartView(d.memberChart.map { it.first to it.second.toDouble() }, Modifier.padding(top = 8.dp), 112.dp, centerLabel = t("members"))
                        }
                    }
                }
            }
            item {
                var active by remember { mutableStateOf(0) }
                val tab = tabs[active]
                AppCard(Modifier.enterAnim(delayMs = 300)) {
                    Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        tabs.forEachIndexed { i, x ->
                            Box(
                                Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                    .background(if (active == i) Tokens.Primary.copy(alpha = .2f) else Color.Transparent)
                                    .clickable { active = i }.padding(horizontal = 4.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) { Text(x.label, fontSize = 12.sp, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Medium, color = if (active == i) Tokens.Primary else Tokens.MutedForeground) }
                        }
                    }
                    Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp)) {
                        if (tab.rows.isEmpty()) Text(none, fontSize = 14.sp, color = Tokens.MutedForeground, modifier = Modifier.padding(vertical = 8.dp))
                        tab.rows.take(3).forEachIndexed { i, r ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (r.sub.isNotBlank()) Text(r.sub, fontSize = 12.sp, color = Tokens.MutedForeground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(r.right, fontSize = 12.sp, color = r.rightColor, fontWeight = if (r.bold) FontWeight.SemiBold else FontWeight.Normal)
                            }
                            if (i < minOf(tab.rows.size, 3) - 1) Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = .1f)))
                        }
                        Text(t("view-all"), fontSize = 13.sp, color = Tokens.Primary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().clickable { onOpen(tab.route) }.padding(vertical = 10.dp))
                    }
                }
            }
        }
    }
}

private data class Tile4(val label: String, val value: Double, val rupiah: Boolean, val icon: ImageVector, val color: Color, val route: String)
private data class MRow(val title: String, val sub: String, val right: String, val rightColor: Color, val bold: Boolean)
private data class MobileTab(val label: String, val route: String, val rows: List<MRow>)

@Composable
private fun StatTile(t: Tile4, lang: com.growthaxys.churchmanagements.core.Lang, modifier: Modifier, onClick: () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(t.value) { anim.snapTo(0f); anim.animateTo(1f, tween(900, 250, androidx.compose.animation.core.FastOutSlowInEasing)) }
    val v = t.value * anim.value
    val text = if (t.rupiah) Format.rupiahCompact(Math.round(v).toDouble(), lang) else Math.round(v).toString()
    AppCard(modifier.clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().heightIn(min = 92.dp).padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(t.color.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                Icon(t.icon, null, tint = t.color, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(t.label, fontSize = 10.sp, lineHeight = 12.sp, color = Tokens.MutedForeground, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, modifier: Modifier = Modifier, icon: ImageVector? = null, content: @Composable ColumnScope.() -> Unit) = AppCard(modifier) {
    Column(Modifier.fillMaxSize().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, null, tint = Tokens.Emerald400, modifier = Modifier.size(16.dp))
            Text(title, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center) { content() }
    }
}
