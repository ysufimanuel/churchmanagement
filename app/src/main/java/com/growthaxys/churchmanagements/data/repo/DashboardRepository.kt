package com.growthaxys.churchmanagements.data.repo

import com.growthaxys.churchmanagements.data.model.*
import io.github.jan.supabase.SupabaseClient
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

data class DashboardData(
    val totalMembers: Int, val attendanceWeek: Int, val donationsMonth: Double, val activeEvents: Int,
    val totalGroups: Int, val activeVolunteers: Int,
    val activities: List<Activity>, val upcomingEvents: List<Event>, val recentDonations: List<Donation>,
    val attendanceChart: List<Pair<String, Int>>,       // (MM-dd, total) last 7 calendar days
    val memberChart: List<Pair<String, Int>>,           // gender distribution
    val donationTrend: List<Pair<String, Double>>,      // (yyyy-MM, total) last 6 months
)

/** Same 10 queries as web `src/app/dashboard/page.tsx`. */
@Singleton
class DashboardRepository @Inject constructor(client: SupabaseClient, auth: AuthRepository) : BaseRepo(client, auth) {
    suspend fun load(): DashboardData {
        val today = LocalDate.now()
        val chartStart = today.minusDays(6)
        val utcNow = java.time.OffsetDateTime.now(ZoneOffset.UTC)
        val monthStart = utcNow.toLocalDate().withDayOfMonth(1).toString()
        val trendStart = YearMonth.from(utcNow).minusMonths(5).atDay(1).toString()

        val members = table("members").select(raw("id, jk, status")).decodeList<MemberGender>()
        val att = table("attendance").select(raw("id, tanggal")) { filter { gte("tanggal", chartStart.toString()); lte("tanggal", today.toString()) } }.decodeList<AttendanceDay>()
        val donMonth = table("donations").select(raw("jumlah")) { filter { gte("tanggal", monthStart) } }.decodeList<DonationAmount>()
        val events = table("events").select(raw("id, nama, status")).decodeList<EventLite>()
        val groups = table("groups").select(raw("id")).decodeList<IdOnly>()
        val vols = table("volunteers").select(raw("id")) { filter { eq("status", "aktif") } }.decodeList<IdOnly>()
        val activities = table("activities").select { order("created_at", DESC); limit(8) }.decodeList<Activity>()
        val upcoming = table("events").select { filter { isIn("status", listOf("upcoming", "ongoing")) }; order("start_at", ASC); limit(5) }.decodeList<Event>()
        val recent = table("donations").select(raw("*, donors(nama)")) { order("tanggal", DESC); limit(5) }.decodeList<Donation>()
        val trend = table("donations").select(raw("jumlah, tanggal")) { filter { gte("tanggal", trendStart) } }.decodeList<DonationAmount>()

        val byDay = att.groupingBy { it.tanggal }.eachCount()
        val attendanceChart = (0..6).map { i ->
            val key = chartStart.plusDays(i.toLong()).toString()
            key.substring(5) to (byDay[key] ?: 0)
        }
        val gender = members.groupingBy { it.jk ?: "Lainnya" }.eachCount().map { it.key to it.value }
        val byMonth = trend.groupBy { it.tanggal.take(7) }.mapValues { (_, v) -> v.sumOf { it.jumlah } }
        val donationTrend = (0..5).map { i ->
            val ym = YearMonth.from(utcNow).minusMonths((5 - i).toLong()).toString()
            ym to (byMonth[ym] ?: 0.0)
        }
        return DashboardData(
            totalMembers = members.size, attendanceWeek = att.size, donationsMonth = donMonth.sumOf { it.jumlah },
            activeEvents = events.count { it.status != "completed" }, totalGroups = groups.size, activeVolunteers = vols.size,
            activities = activities, upcomingEvents = upcoming, recentDonations = recent,
            attendanceChart = attendanceChart, memberChart = gender, donationTrend = donationTrend,
        )
    }
}
