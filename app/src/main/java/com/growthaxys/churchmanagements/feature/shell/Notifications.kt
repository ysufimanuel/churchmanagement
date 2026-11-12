package com.growthaxys.churchmanagements.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Notification
import com.growthaxys.churchmanagements.data.repo.CommunicationRepository
import com.growthaxys.churchmanagements.designsystem.AppButton
import com.growthaxys.churchmanagements.designsystem.AppDialog
import com.growthaxys.churchmanagements.designsystem.AppIconButton
import com.growthaxys.churchmanagements.designsystem.BtnVariant
import com.growthaxys.churchmanagements.designsystem.EmptyText
import com.growthaxys.churchmanagements.designsystem.Tokens
import com.growthaxys.churchmanagements.feature.common.LocalToaster
import com.growthaxys.churchmanagements.feature.common.ToastMsg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(private val repo: CommunicationRepository) : ViewModel() {
    private val _items = MutableStateFlow<List<Notification>>(emptyList())
    val items: StateFlow<List<Notification>> = _items.asStateFlow()
    private val _incoming = MutableStateFlow<Notification?>(null)
    val incoming: StateFlow<Notification?> = _incoming.asStateFlow()

    init {
        reload()
        // Realtime parity with web notification-bell: new rows are prepended and shown as a toast.
        viewModelScope.launch { runCatching { repo.incomingNotifications().collect { n -> _items.value = listOf(n) + _items.value; _incoming.value = n } } }
    }
    fun reload() { viewModelScope.launch { runCatching { repo.notifications() }.onSuccess { _items.value = it } } }
    fun consumeIncoming() { _incoming.value = null }
    fun markRead(n: Notification) {
        if (n.read) return
        viewModelScope.launch { repo.markNotificationRead(n.id); _items.value = _items.value.map { if (it.id == n.id) it.copy(read = true) else it } }
    }
    fun markAll() { viewModelScope.launch { repo.markAllNotificationsRead(); _items.value = _items.value.map { it.copy(read = true) } } }
}

@Composable
fun NotificationBell(vm: NotificationsViewModel = hiltViewModel()) {
    val items by vm.items.collectAsStateWithLifecycle()
    val incoming by vm.incoming.collectAsStateWithLifecycle()
    val lang = LocalStrings.current.lang
    val toaster = LocalToaster.current
    var open by remember { mutableStateOf(false) }
    val unread = items.count { !it.read }

    LaunchedEffect(incoming) { incoming?.let { toaster(ToastMsg(raw = it.title)); vm.consumeIncoming() } }

    Box {
        AppIconButton(Icons.Outlined.Notifications, t("notifications"), { open = true }, size = 40.dp)
        if (unread > 0) Box(Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp).background(Tokens.Destructive, CircleShape), contentAlignment = Alignment.Center) {
            Text(if (unread > 9) "9+" else unread.toString(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    if (open) AppDialog(t("notifications"), { open = false }) {
        if (unread > 0) AppButton(t("mark-all-read"), vm::markAll, Modifier.fillMaxWidth(), variant = BtnVariant.Secondary)
        if (items.isEmpty()) EmptyText(t("no-notifications"))
        else LazyColumn(Modifier.fillMaxWidth().padding(0.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { n ->
                Column(
                    Modifier.fillMaxWidth().background(if (n.read) Color(0x08FFFFFF) else Tokens.Primary.copy(alpha = 0.08f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .clickable { vm.markRead(n) }.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(n.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(1f))
                        Text(Format.timeAgo(n.createdAt, lang), fontSize = 11.sp, color = Tokens.MutedForeground)
                    }
                    Text(n.message, fontSize = 12.sp, color = Tokens.MutedForeground)
                }
            }
        }
    }
}
