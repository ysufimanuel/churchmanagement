package com.growthaxys.churchmanagements.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthaxys.churchmanagements.core.designsystem.*
import com.growthaxys.churchmanagements.domain.model.UserProfile

private data class DashboardMetric(
    val label: String,
    val icon: ImageVector,
    val tint: Color
)

private val dashboardMetrics = listOf(
    DashboardMetric("Total Jemaat", Icons.Outlined.Groups, Color(0xFFFB923C)),
    DashboardMetric("Kehadiran Minggu Ini", Icons.Outlined.FactCheck, Color(0xFF60A5FA)),
    DashboardMetric("Donasi Bulan Ini", Icons.Outlined.VolunteerActivism, Color(0xFF34D399)),
    DashboardMetric("Event Aktif", Icons.Outlined.CalendarMonth, Color(0xFFA78BFA)),
    DashboardMetric("Total Grup", Icons.Outlined.Hub, Color(0xFFF87171)),
    DashboardMetric("Relawan Aktif", Icons.Outlined.Handshake, Color(0xFFFBBF24))
)

private data class DashboardTab(val label: String, val icon: ImageVector)

private val dashboardTabs = listOf(
    DashboardTab("Home", Icons.Outlined.Dashboard),
    DashboardTab("Keuangan", Icons.Outlined.AccountBalanceWallet),
    DashboardTab("Cari", Icons.Outlined.Search),
    DashboardTab("Web", Icons.Outlined.Language),
    DashboardTab("Menu", Icons.Outlined.Menu)
)

private val moduleItems = listOf(
    "Jemaat" to Icons.Outlined.Groups,
    "Keluarga" to Icons.Outlined.FamilyRestroom,
    "Grup" to Icons.Outlined.Hub,
    "Jadwal & Event" to Icons.Outlined.Event,
    "Kehadiran" to Icons.Outlined.FactCheck,
    "Donasi" to Icons.Outlined.VolunteerActivism,
    "Keuangan" to Icons.Outlined.AccountBalanceWallet,
    "Relawan" to Icons.Outlined.Handshake,
    "Komunikasi" to Icons.Outlined.Campaign,
    "Laporan" to Icons.Outlined.Assessment,
    "Manajemen Pengguna" to Icons.Outlined.ManageAccounts,
    "Web Manager" to Icons.Outlined.Web
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(profile: UserProfile, onSignOut: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var selectedModule by remember { mutableStateOf<String?>(null) }
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("GetChurchly", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Church Management System", color = Muted, fontSize = 10.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = true }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Cari", tint = Color.White)
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Keluar", tint = Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xF21A1D24),
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                dashboardTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (index) {
                                2 -> showSearch = true
                                4 -> showMenu = true
                                1 -> selectedModule = "Keuangan"
                                3 -> selectedModule = "Web Manager"
                                else -> selectedModule = null
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cyan,
                            selectedTextColor = Cyan,
                            indicatorColor = Cyan.copy(alpha = .12f),
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { padding ->
        if (selectedModule != null) {
            ModuleComingSoon(
                title = selectedModule!!,
                onBack = { selectedModule = null; selectedTab = 0 }
            )
        } else {
            Column(
                Modifier.fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF101820), Background, Background)
                        )
                    )
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AnimatedVisibility(
                            visible = entered,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 5 })
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Selamat datang kembali, ${profile.name}", fontSize = 12.sp, color = Muted)
                            }
                        }
                    }
                    item {
                        AnimatedVisibility(
                            visible = entered,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })
                        ) {
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF17333C), Color(0xFF211C3B))),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .border(1.dp, Border, RoundedCornerShape(18.dp))
                                    .padding(15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Cyan.copy(alpha = .14f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            profile.name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                                            color = Cyan, fontWeight = FontWeight.Bold, fontSize = 15.sp
                                        )
                                    }
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(profile.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text(profile.role.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp, color = Muted)
                                }
                                Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = Cyan, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Ringkasan Gereja", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Text("Data live", fontSize = 10.sp, color = Muted)
                            }
                            dashboardMetrics.chunked(3).forEach { rowMetrics ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowMetrics.forEach { metric ->
                                        MetricCard(metric, Modifier.weight(1f))
                                    }
                                }
                            }
                            Text(
                                "Angka akan ditampilkan setelah query statistik dashboard terhubung.",
                                color = Muted, fontSize = 10.sp
                            )
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("Analitik", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnalyticsCard("Kehadiran", "Statistik kehadiran", Icons.Outlined.ShowChart)
                                AnalyticsCard("Tren Donasi", "Donasi per bulan", Icons.Outlined.TrendingUp)
                                AnalyticsCard("Distribusi Jemaat", "Komposisi anggota", Icons.Outlined.DonutSmall)
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("Aktivitas Terbaru", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Surface(
                                color = Surface,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Outlined.History, contentDescription = null, tint = Muted)
                                    Text("Aktivitas akan muncul setelah feed aktivitas web terhubung.", fontSize = 11.sp, color = Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMenu = false },
            containerColor = Surface,
            contentColor = Color.White
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Semua Menu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Pilih modul Church Managements", color = Muted, fontSize = 12.sp)
                moduleItems.forEach { (label, icon) ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            showMenu = false
                            selectedModule = label
                        }.padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(icon, contentDescription = null, tint = Cyan, modifier = Modifier.size(21.dp))
                        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Muted)
                    }
                    HorizontalDivider(color = Border)
                }
                TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Keluar")
                }
            }
        }
    }

    if (showSearch) {
        var query by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSearch = false },
            title = { Text("Pencarian Global") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Cari jemaat, keluarga, event...") }
                    )
                    Text("Pencarian lintas modul akan diaktifkan setelah endpoint pencarian web dipetakan.", fontSize = 11.sp, color = Muted)
                }
            },
            confirmButton = { TextButton(onClick = { showSearch = false }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun MetricCard(metric: DashboardMetric, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 94.dp),
        color = Surface,
        shape = RoundedCornerShape(13.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                Modifier.size(27.dp).background(metric.tint.copy(alpha = .13f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(metric.icon, contentDescription = null, tint = metric.tint, modifier = Modifier.size(15.dp))
            }
            Text("—", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(metric.label, fontSize = 9.sp, lineHeight = 11.sp, color = Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AnalyticsCard(title: String, subtitle: String, icon: ImageVector) {
    Surface(
        modifier = Modifier.width(260.dp).height(154.dp),
        color = Surface,
        shape = RoundedCornerShape(15.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(icon, contentDescription = null, tint = Cyan, modifier = Modifier.size(17.dp))
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(subtitle, fontSize = 10.sp, color = Muted)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Menunggu data analitik", fontSize = 10.sp, color = Muted)
            }
        }
    }
}

@Composable
private fun ModuleComingSoon(title: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Background).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Construction, contentDescription = null, tint = Cyan, modifier = Modifier.size(38.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Modul ini sedang dipetakan ke alur web agar fungsi, data, dan izin aksesnya sama persis.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Kembali ke Dashboard") }
    }
}
