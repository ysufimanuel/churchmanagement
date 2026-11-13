package com.growthaxys.churchmanagements.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Lang
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

/** Mobile dashboard mirrors the web's 3-column stats, swipeable chart rail and tabbed activity card. */
@Composable
fun DashboardScreen(role: Role, padding: PaddingValues, onOpen: (String) -> Unit, vm: DashboardViewModel = hiltViewModel()) {
    LoadGate(vm, padding, SkeletonKind.Dashboard) { d ->
        val strings = LocalStrings.current
        ModulePage(padding) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(t("dashboard"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(t("welcome-back"), fontSize = 13.sp, color = Tokens.MutedForeground)
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DashboardStatCard(t("total-members"), d.totalMembers, Icons.Outlined.People, Color(0xFFFB923C), Modifier.weight(1f)) { onOpen("members") }
                    DashboardStatCard(t("attendance-this-week"), d.attendanceWeek, Icons.Outlined.Checklist, Color(0xFF60A5FA), Modifier.weight(1f)) { onOpen("attendance") }
                    DashboardStatCard(t("donations-this-month"), d.donationsMonth, Icons.Outlined.VolunteerActivism, Tokens.Emerald400, Modifier.weight(1f), money = true) { onOpen("donations") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DashboardStatCard(t("active-events"), d.activeEvents, Icons.Outlined.CalendarMonth, Color(0xFFC084FC), Modifier.weight(1f)) { onOpen("events") }
                    DashboardStatCard(t("total-groups"), d.totalGroups, Icons.Outlined.Groups, Color(0xFFF87171), Modifier.weight(1f)) { onOpen("groups") }
                    DashboardStatCard(t("active-volunteers"), d.activeVolunteers, Icons.Outlined.Event, Color(0xFFFBBF24), Modifier.weight(1f)) { onOpen("volunteers") }
                }
            }

            // Web mobile uses an 88%-width snap rail instead of stacking every chart vertically.
            item {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val cardWidth = maxWidth * 0.88f
                    val chartState = rememberLazyListState()
                    LazyRow(
                        state = chartState,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = chartState),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 12.dp),
                    ) {
                        item {
                            ChartCard(t("attendance-stats"), Modifier.width(cardWidth).enterAnim(delayMs = 120)) {
                                BarChartView(d.attendanceChart, Modifier.padding(top = 12.dp), 150.dp)
                            }
                        }
                        item {
                            ChartCard(t("donation-trend"), Modifier.width(cardWidth).enterAnim(delayMs = 170)) {
                                LineChartView(
                                    d.donationTrend.map { Format.monthShort(it.first, strings.lang) },
                                    listOf(Triple(t("donations"), d.donationTrend.map { it.second }, Tokens.Primary)),
                                    Modifier.padding(top = 12.dp), 150.dp, area = true,
                                )
                            }
                        }
                        item {
                            ChartCard(t("member-distribution"), Modifier.width(cardWidth).enterAnim(delayMs = 220)) {
                                DonutChartView(d.memberChart.map { it.first to it.second.toDouble() }, Modifier.padding(top = 12.dp), 180.dp)
                            }
                        }
                    }
                }
            }

            item {
                DashboardActivityCard(d, strings.lang)
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    label: String,
    value: Number,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    money: Boolean = false,
    onClick: () -> Unit,
) {
    AppCard(modifier.clickable(onClick = onClick)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(16.dp), tint = tint)
            }
            if (money) {
                Text(Format.rupiahCompact(value.toDouble(), LocalStrings.current.lang), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tint, maxLines = 1)
            } else {
                CountUpValue(value.toLong())
            }
            Text(label, fontSize = 9.sp, lineHeight = 12.sp, color = Tokens.MutedForeground, maxLines = 2)
        }
    }
}

@Composable
private fun CountUpValue(value: Long) {
    val counter = remember(value) { Animatable(0f) }
    LaunchedEffect(value) {
        counter.snapTo(0f)
        counter.animateTo(value.toFloat(), tween(900, easing = EaseOutStrong))
    }
    Text(counter.value.toLong().toString(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
}

@Composable
private fun ChartCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = AppCard(modifier) {
    Column(Modifier.padding(16.dp)) {
        Text(title, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun DashboardActivityCard(data: DashboardData, lang: Lang) {
    val tabs = listOf(t("recent-donations"), t("upcoming-events"), t("recent-activity"))
    var selected by remember { mutableIntStateOf(0) }
    AppCard(Modifier.enterAnim(delayMs = 260)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEachIndexed { index, label ->
                val active = selected == index
                Text(
                    label,
                    Modifier.clip(RoundedCornerShape(9.dp))
                        .background(if (active) Tokens.Primary.copy(alpha = 0.16f) else Color.Transparent)
                        .clickable { selected = index }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    color = if (active) Tokens.Primary else Tokens.MutedForeground,
                    fontSize = 11.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
            when (selected) {
                0 -> {
                    if (data.recentDonations.isEmpty()) EmptyText()
                    data.recentDonations.take(4).forEach { donation ->
                        ActivityRow(
                            donation.donors?.nama ?: "-",
                            listOfNotNull(donation.tipe, Format.date(donation.tanggal, lang)).joinToString(" · "),
                            Format.rupiahCompact(donation.jumlah.toDouble(), lang),
                            Tokens.Emerald400,
                        )
                    }
                }
                1 -> {
                    if (data.upcomingEvents.isEmpty()) EmptyText()
                    data.upcomingEvents.take(4).forEach { event ->
                        ActivityRow(event.nama, event.lokasi.orEmpty(), Format.dateTime(event.startAt, lang), Tokens.Primary)
                    }
                }
                else -> {
                    if (data.activities.isEmpty()) EmptyText()
                    data.activities.take(4).forEach { activity ->
                        ActivityRow(
                            activity.action,
                            activity.detail.orEmpty(),
                            Format.timeAgo(activity.createdAt, lang),
                            Tokens.MutedForeground,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(title: String, subtitle: String, trailing: String, trailingColor: Color) {
    Column(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(trailing, color = trailingColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        if (subtitle.isNotBlank()) Text(subtitle, color = Tokens.MutedForeground, fontSize = 10.sp, maxLines = 1)
    }
}
