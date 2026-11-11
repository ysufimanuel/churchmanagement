package com.growthaxys.churchmanagements.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.growthaxys.churchmanagements.core.designsystem.*
import com.growthaxys.churchmanagements.domain.model.UserProfile

private data class ModuleItem(val title: String, val description: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private val modules = listOf(
    ModuleItem("Dashboard", "Ringkasan gereja", Icons.Outlined.Dashboard),
    ModuleItem("Jemaat", "Data anggota dan keluarga", Icons.Outlined.Groups),
    ModuleItem("Grup", "Kelompok dan komunitas", Icons.Outlined.PeopleAlt),
    ModuleItem("Jadwal & Event", "Ibadah dan kegiatan", Icons.Outlined.Event),
    ModuleItem("Kehadiran", "Check-in dan laporan", Icons.Outlined.HowToReg),
    ModuleItem("Donasi", "Donatur dan donasi", Icons.Outlined.VolunteerActivism),
    ModuleItem("Keuangan", "Transaksi dan persetujuan", Icons.Outlined.AccountBalanceWallet),
    ModuleItem("Relawan", "Pelayanan dan penugasan", Icons.Outlined.Handshake),
    ModuleItem("Komunikasi", "Pesan, pengumuman, broadcast", Icons.Outlined.Campaign),
    ModuleItem("Laporan", "Ringkasan dan ekspor", Icons.Outlined.Assessment),
    ModuleItem("Manajemen Pengguna", "Akun dan role", Icons.Outlined.ManageAccounts),
    ModuleItem("Web Manager", "Konten dan tampilan situs", Icons.Outlined.Web)
)

@Composable
fun DashboardScreen(profile: UserProfile, onSignOut: () -> Unit) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Church Managements", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Gereja Digital", color = Muted, fontSize = 11.sp)
                    }
                },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Icon(Icons.Outlined.Logout, null, tint = Cyan)
                        Spacer(Modifier.width(4.dp))
                        Text("Keluar", color = Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth().background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF17333C), Color(0xFF211C3B))
                        ), shape = RoundedCornerShape(22.dp)
                    ).border(1.dp, Border, RoundedCornerShape(22.dp)).padding(20.dp)
                ) {
                    Text("Selamat datang,", color = Muted, fontSize = 12.sp)
                    Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 23.sp)
                    Spacer(Modifier.height(10.dp))
                    Surface(color = Cyan.copy(alpha = .12f), shape = RoundedCornerShape(50)) {
                        Text(profile.role.name, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Cyan, fontSize = 10.sp)
                    }
                }
            }
            item { Text("Menu Utama", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
            items(modules) { module ->
                Row(
                    Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(16.dp))
                        .border(1.dp, Border, RoundedCornerShape(16.dp)).padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(module.icon, null, tint = Cyan, modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f)) {
                        Text(module.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(module.description, color = Muted, fontSize = 11.sp)
                    }
                    Text("›", color = Muted, fontSize = 22.sp)
                }
            }
            item {
                Text(
                    "Tahap awal: autentikasi dan app shell. Modul akan diaktifkan setelah audit alur, role, dan query web per fitur.",
                    color = Muted, fontSize = 11.sp
                )
            }
        }
    }
}
