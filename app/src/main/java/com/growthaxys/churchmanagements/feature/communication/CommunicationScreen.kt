package com.growthaxys.churchmanagements.feature.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Add
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Announcement
import com.growthaxys.churchmanagements.data.model.Message
import com.growthaxys.churchmanagements.data.model.Profile
import com.growthaxys.churchmanagements.data.model.ProfileLite
import com.growthaxys.churchmanagements.data.repo.CommunicationRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommData(val contacts: List<ProfileLite>, val messages: List<Message>, val announcements: List<Announcement>)

@HiltViewModel
class CommunicationViewModel @Inject constructor(private val repo: CommunicationRepository) : FeatureViewModel<CommData>() {
    override suspend fun fetch() = CommData(repo.contacts(), repo.messages(), repo.announcements())

    init {
        // Realtime chat (web `chat-realtime-<uid>`): any inserted message refreshes the thread list.
        viewModelScope.launch { runCatching { repo.incomingMessages().collect { refresh() } } }
    }

    fun send(to: String, text: String, onOk: () -> Unit) = mutate("sent", onOk) { repo.sendMessage(to, text) }
    fun markRead(id: Long) { viewModelScope.launch { repo.markMessageRead(id) } }
    fun broadcast(subject: String, message: String, send: Boolean, onOk: () -> Unit) = mutate(if (send) "broadcast-sent" else "draft-saved", onOk) { repo.saveBroadcast(subject, message, send) }
    fun saveAnnouncement(id: Long?, judul: String, konten: String, expiry: String, important: Boolean, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveAnnouncement(id, judul, konten, expiry, important) }
    fun deleteAnnouncement(id: Long) = mutate { repo.deleteAnnouncement(id) }
}

@Composable
fun CommunicationScreen(profile: Profile?, role: Role, padding: PaddingValues, initialQuery: String, vm: CommunicationViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Tabs) { data ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        val s = LocalStrings.current
        val me = profile?.id.orEmpty()
        val tabs = buildList {
            if (Rbac.canAdd(role)) add(TabItem("broadcast", t("email-broadcast")))
            add(TabItem("messages", t("send-message"))); add(TabItem("announcements", t("announcements-tab")))
        }
        var tab by remember { mutableStateOf(tabs.first().id) }
        var contactSearch by remember { mutableStateOf(initialQuery) }
        var active by remember { mutableStateOf<String?>(null) }
        var draft by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var annEdit by remember { mutableStateOf<AnnDraft?>(null) }
        val nameOf = { id: String -> data.contacts.firstOrNull { it.id == id }?.nama ?: "-" }

        // Mark incoming messages of the open conversation read (web behaviour).
        val thread = active?.let { a -> data.messages.filter { (it.senderId == a && it.receiverId == me) || (it.senderId == me && it.receiverId == a) } }.orEmpty()
        LaunchedEffect(active, thread.size) { thread.filter { !it.read && it.receiverId == me }.forEach { vm.markRead(it.id) } }

        ModulePage(padding) {
            item { PageHeader(t("communication"), t("communication-system")) }
            item { AppTabs(tabs, tab, { tab = it }) }

            when (tab) {
                "broadcast" -> item {
                    AppCard {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LabeledField(t("recipients")) { AppSelect("all", listOf(Option("all", t("all-members"))), {}) }
                            LabeledField(t("subject")) { AppTextField(subject, { subject = it }) }
                            LabeledField(t("message")) { AppTextField(body, { body = it }, singleLine = false, minHeight = 140.dp) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppButton(t("save-draft"), { vm.broadcast(subject, body, false) { subject = ""; body = "" } }, variant = BtnVariant.Secondary, enabled = !busy && subject.isNotBlank() && body.isNotBlank())
                                AppButton(t("send-broadcast"), { vm.broadcast(subject, body, true) { subject = ""; body = "" } }, icon = Icons.AutoMirrored.Outlined.Send, enabled = !busy && subject.isNotBlank() && body.isNotBlank())
                            }
                        }
                    }
                }

                "messages" -> if (active == null) {
                    item { SearchField(contactSearch, { contactSearch = it }, t("search-contacts")) }
                    val list = data.contacts.filter { it.id != me && it.nama.contains(contactSearch, true) }
                    items(list, key = { it.id }) { c ->
                        val unread = data.messages.count { it.senderId == c.id && it.receiverId == me && !it.read }
                        AppCard(Modifier.clickable { active = c.id }) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                InitialsAvatar(c.nama, imageUrl = c.avatarUrl)
                                Column(Modifier.weight(1f)) {
                                    Text(c.nama, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(Rbac.roleLabel(Role.from(c.role)), color = Tokens.MutedForeground, fontSize = 12.sp)
                                }
                                if (unread > 0) AppBadge(unread.toString(), BadgeVariant.Destructive)
                            }
                        }
                    }
                    if (list.isEmpty()) item { EmptyText() }
                } else {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppButton("‹", { active = null }, variant = BtnVariant.Ghost, height = 36.dp)
                            Text(nameOf(active!!), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (thread.isEmpty()) item { EmptyText(t("select-contact")) }
                    items(thread, key = { it.id }) { m ->
                        val mine = m.senderId == me
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                            if (!mine) InitialsAvatar(nameOf(m.senderId), size = 30.dp, imageUrl = data.contacts.firstOrNull { it.id == m.senderId }?.avatarUrl)
                            Column(
                                Modifier.widthIn(max = 280.dp).background(if (mine) Tokens.Primary else Tokens.Card, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                val fg = if (mine) Tokens.OnPrimary else Color.White
                                Text(m.content, color = fg, fontSize = 14.sp)
                                Text(Format.timeAgo(m.createdAt, s.lang), color = fg.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppTextField(draft, { draft = it }, Modifier.weight(1f), placeholder = t("type-message"))
                            AppButton(t("act-send"), { vm.send(active!!, draft) { draft = "" } }, icon = Icons.AutoMirrored.Outlined.Send, enabled = !busy && draft.isNotBlank())
                        }
                    }
                }

                else -> {
                    if (Rbac.canAdd(role)) item { AppButton(t("create-announcement"), { annEdit = AnnDraft(null, "", "", "", false) }, icon = Icons.Outlined.Add) }
                    items(data.announcements, key = { it.id }) { a ->
                        AppCard {
                            CardHeaderRow {
                                Column(Modifier.weight(1f)) {
                                    Text(a.judul, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (a.important) AppBadge(t("important"), BadgeVariant.Destructive)
                                        FieldText(Format.date(a.tanggal, s.lang), Tokens.MutedForeground)
                                    }
                                }
                                RowActions(Rbac.canEdit(role), Rbac.canDelete(role),
                                    onEdit = { annEdit = AnnDraft(a.id, a.judul, a.konten, a.expiry.orEmpty().take(10), a.important) },
                                    onDelete = { vm.deleteAnnouncement(a.id) })
                            }
                            CardBody { FieldText(a.konten, Tokens.MutedForeground) }
                        }
                    }
                    if (data.announcements.isEmpty()) item { EmptyText() }
                }
            }
        }

        annEdit?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            AppDialog(t("create-announcement"), { annEdit = null }) {
                LabeledField(t("title")) { AppTextField(f.judul, { f = f.copy(judul = it) }) }
                LabeledField(t("content")) { AppTextField(f.konten, { f = f.copy(konten = it) }, singleLine = false, minHeight = 120.dp) }
                LabeledField(t("expiry-date")) { DateField(f.expiry, { f = f.copy(expiry = it) }) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppSwitch(f.important, { f = f.copy(important = it) })
                    Text(t("mark-important"), color = Color.White, fontSize = 14.sp)
                }
                DialogActions({ annEdit = null }, { if (f.judul.isNotBlank() && f.konten.isNotBlank()) vm.saveAnnouncement(f.id, f.judul.trim(), f.konten.trim(), f.expiry, f.important) { annEdit = null } }, saving = busy)
            }
        }
    }
}

private data class AnnDraft(val id: Long?, val judul: String, val konten: String, val expiry: String, val important: Boolean)
