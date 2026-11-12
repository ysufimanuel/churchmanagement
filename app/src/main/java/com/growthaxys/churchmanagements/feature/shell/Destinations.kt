package com.growthaxys.churchmanagements.feature.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role

/** Mirrors web `nav-items.ts`; labels use the same i18n keys. */
data class Dest(val route: String, val key: String, val icon: ImageVector, val visible: (Role) -> Boolean = { true })

object Destinations {
    val all = listOf(
        Dest("dashboard", "dashboard", Icons.Outlined.Dashboard),
        Dest("members", "members", Icons.Outlined.People),
        Dest("families", "families", Icons.Outlined.Home),
        Dest("groups", "groups", Icons.Outlined.Groups),
        Dest("events", "events", Icons.Outlined.CalendarMonth),
        Dest("attendance", "attendance", Icons.Outlined.Checklist),
        Dest("donations", "donations", Icons.Outlined.VolunteerActivism),
        Dest("finance", "finance", Icons.Outlined.AccountBalanceWallet),
        Dest("volunteers", "volunteers", Icons.Outlined.Handshake),
        Dest("communication", "communication", Icons.Outlined.Forum),
        Dest("reports", "reports", Icons.Outlined.BarChart),
        Dest("users", "users", Icons.Outlined.ManageAccounts) { Rbac.canManageUsers(it) },
    )
    fun forRole(role: Role) = all.filter { it.visible(role) }
    fun byRoute(r: String) = all.firstOrNull { it.route == r }
}
