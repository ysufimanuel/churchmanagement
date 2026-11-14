@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.growthaxys.churchmanagements.feature.shell

import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.PaddingValues
import com.growthaxys.churchmanagements.core.Lang
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Profile
import com.growthaxys.churchmanagements.designsystem.AppButton
import com.growthaxys.churchmanagements.designsystem.BtnVariant
import com.growthaxys.churchmanagements.designsystem.Glass
import com.growthaxys.churchmanagements.designsystem.InitialsAvatar
import com.growthaxys.churchmanagements.designsystem.Tokens
import com.growthaxys.churchmanagements.feature.attendance.AttendanceScreen
import com.growthaxys.churchmanagements.feature.communication.CommunicationScreen
import com.growthaxys.churchmanagements.feature.dashboard.DashboardScreen
import com.growthaxys.churchmanagements.feature.donations.DonationsScreen
import com.growthaxys.churchmanagements.feature.events.EventsScreen
import com.growthaxys.churchmanagements.feature.families.FamiliesScreen
import com.growthaxys.churchmanagements.feature.finance.FinanceScreen
import com.growthaxys.churchmanagements.feature.groups.GroupsScreen
import com.growthaxys.churchmanagements.feature.members.MembersScreen
import com.growthaxys.churchmanagements.feature.reports.ReportsScreen
import com.growthaxys.churchmanagements.feature.users.UsersScreen
import com.growthaxys.churchmanagements.feature.volunteers.VolunteersScreen
import android.net.Uri

private const val SEARCH_ROUTE = "search"

@Composable
fun MainShell(profile: Profile, lang: Lang, onLang: (Lang) -> Unit, onLogout: () -> Unit) {
    val role = Role.from(profile.role)
    val nav = rememberNavController()
    val backEntry by nav.currentBackStackEntryAsState()
    val current = backEntry?.destination?.route?.substringBefore("?") ?: "dashboard"
    var menuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var profileOpen by remember { mutableStateOf(false) }

    fun go(route: String, q: String? = null) {
        val target = if (q != null) "$route?q=${Uri.encode(q)}" else route
        nav.navigate(target) { launchSingleTop = true; popUpTo("dashboard") { saveState = false } }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(profile, onProfile = { profileOpen = true })
            Box(Modifier.weight(1f)) {
                NavHost(
                    nav, startDestination = "dashboard",
                    enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220, easing = com.growthaxys.churchmanagements.designsystem.EaseOutStrong)) + androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(220, easing = com.growthaxys.churchmanagements.designsystem.EaseOutStrong)) { it / 40 } },
                    exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120)) },
                    popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220, easing = com.growthaxys.churchmanagements.designsystem.EaseOutStrong)) },
                    popExitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(120)) },
                ) {
                    val pad = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 112.dp)
                    composable("dashboard") { DashboardScreen(role, profile.nama, pad, onOpen = { go(it) }) }
                    for (r in listOf("members", "families", "groups", "events", "attendance", "donations", "finance", "volunteers", "communication", "reports", "users", "web-manager")) {
                        composable("$r?q={q}", arguments = listOf(navArgument("q") { type = NavType.StringType; nullable = true; defaultValue = null })) { e ->
                            val q = e.arguments?.getString("q").orEmpty()
                            when (r) {
                                "members" -> MembersScreen(role, pad, q)
                                "families" -> FamiliesScreen(role, pad, q)
                                "groups" -> GroupsScreen(role, pad, q)
                                "events" -> EventsScreen(role, pad, q)
                                "attendance" -> AttendanceScreen(role, pad)
                                "donations" -> DonationsScreen(role, pad, q)
                                "finance" -> FinanceScreen(role, pad, q)
                                "volunteers" -> VolunteersScreen(role, pad, q)
                                "communication" -> CommunicationScreen(profile, role, pad, q)
                                "reports" -> ReportsScreen(pad)
                                "users" -> if (Rbac.canManageUsers(role)) UsersScreen(pad)
                                "web-manager" -> if (Rbac.canUseWebManager(role)) com.growthaxys.churchmanagements.feature.webmanager.WebManagerScreen(role, pad)
                            }
                        }
                    }
                }
            }
        }

        BottomNav(current, role, onGo = { go(it) }, onSearch = { searchOpen = true }, onMenu = { menuOpen = true },
            modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (menuOpen) MenuSheet(profile, role, lang, onLang, onClose = { menuOpen = false },
        onGo = { menuOpen = false; go(it) }, onProfile = { menuOpen = false; profileOpen = true }, onLogout = { menuOpen = false; onLogout() })
    if (searchOpen) GlobalSearchDialog(role, onClose = { searchOpen = false }, onOpen = { r, q -> searchOpen = false; go(r, q) })
    if (profileOpen) ProfileDialog(profile, role, onClose = { profileOpen = false })
}

@Composable
private fun TopBar(profile: Profile, onProfile: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Color(0x990B0D12)).statusBarsPadding().heightIn(min = 56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(36.dp).background(Tokens.Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Church, null, Modifier.size(20.dp), tint = Tokens.Primary)
        }
        Text(t("cms-title"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f), maxLines = 1)
        NotificationBell()
        Box(Modifier.clickable(onClick = onProfile)) { InitialsAvatar(profile.nama, size = 36.dp, imageUrl = profile.avatarUrl) }
    }
}

@Composable
private fun BottomNav(current: String, role: Role, onGo: (String) -> Unit, onSearch: () -> Unit, onMenu: () -> Unit, modifier: Modifier = Modifier) {
    Glass(modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(), strong = true, shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.SpaceAround) {
            NavItem(Destinations.byRoute("dashboard")!!.icon, t("dashboard"), current == "dashboard", Modifier.weight(1f)) { onGo("dashboard") }
            NavItem(Destinations.byRoute("finance")!!.icon, t("finance"), current == "finance", Modifier.weight(1f)) { onGo("finance") }
            NavItem(Icons.Outlined.Search, t("nav-search"), false, Modifier.weight(1f), onSearch)
            if (Rbac.canUseWebManager(role)) NavItem(Icons.Outlined.Language, t("web-manager"), current == "web-manager", Modifier.weight(1f)) { onGo("web-manager") }
            val inMenu = current != "dashboard" && current != "finance" && current != "web-manager"
            NavItem(Icons.Outlined.Menu, t("nav-menu"), inMenu, Modifier.weight(1f), onMenu)
        }
    }
}

@Composable
private fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val containerColor by animateColorAsState(
        targetValue = if (active) Tokens.Primary.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "nav-container",
    )
    val iconScale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.88f
            active -> 1.10f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.48f, stiffness = 520f),
        label = "nav-icon-scale",
    )
    val indicatorWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (active) 16.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
        label = "nav-indicator",
    )

    Column(
        modifier
            .heightIn(min = 58.dp)
            .background(containerColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp).graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
            },
            tint = if (active) Tokens.Primary else Tokens.MutedForeground,
        )
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = if (active) Tokens.Primary else Tokens.MutedForeground,
            maxLines = 1,
        )
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .size(width = indicatorWidth, height = 3.dp)
                .background(if (active) Tokens.Primary else Color.Transparent, CircleShape),
        )
    }
}

@Composable
private fun MenuSheet(
    profile: Profile, role: Role, lang: Lang, onLang: (Lang) -> Unit,
    onClose: () -> Unit, onGo: (String) -> Unit, onProfile: () -> Unit, onLogout: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xF20F1117), contentColor = Color.White) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onProfile), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InitialsAvatar(profile.nama, size = 44.dp, imageUrl = profile.avatarUrl, fontSize = 14)
                Column(Modifier.weight(1f)) {
                    Text(profile.nama, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
                    Text(Rbac.roleLabel(role), fontSize = 12.sp, color = Tokens.MutedForeground)
                }
                LangSwitcher(lang, onLang)
            }
            Text(t("all-features").uppercase(), fontSize = 11.sp, color = Tokens.MutedForeground, letterSpacing = 1.sp)
            LazyVerticalGrid(GridCells.Fixed(3), Modifier.heightIn(max = 420.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Destinations.forRole(role)) { d ->
                    Column(
                        Modifier.heightIn(min = 68.dp).background(Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
                            .border(1.dp, Tokens.BorderSubtle, RoundedCornerShape(16.dp)).clickable { onGo(d.route) }.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(d.icon, null, Modifier.size(22.dp), tint = Tokens.Primary)
                        Spacer(Modifier.height(4.dp))
                        Text(t(d.key), fontSize = 11.sp, color = Color.White, maxLines = 1)
                    }
                }
            }
            AppButton(t("logout"), onLogout, Modifier.fillMaxWidth(), variant = BtnVariant.Outline, icon = Icons.Outlined.Logout)
        }
    }
}

/** Web LangSwitcher: two pills "ID" / "EN". */
@Composable
fun LangSwitcher(lang: Lang, onLang: (Lang) -> Unit) {
    Row(Modifier.background(Color(0x0DFFFFFF), CircleShape).border(1.dp, Tokens.BorderSubtle, CircleShape).padding(2.dp)) {
        Lang.entries.forEach { l ->
            val on = l == lang
            Text(l.code.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = if (on) Tokens.OnPrimary else Tokens.MutedForeground,
                modifier = Modifier.background(if (on) Tokens.Primary else Color.Transparent, CircleShape)
                    .clickable { onLang(l) }.padding(horizontal = 10.dp, vertical = 4.dp))
        }
    }
}
