package com.growthaxys.churchmanagements.feature.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.repo.SearchHit
import com.growthaxys.churchmanagements.data.repo.SearchOutcome
import com.growthaxys.churchmanagements.data.repo.SearchRepository
import com.growthaxys.churchmanagements.designsystem.AppDialog
import com.growthaxys.churchmanagements.designsystem.SearchField
import com.growthaxys.churchmanagements.designsystem.Tokens
import com.growthaxys.churchmanagements.designsystem.enterAnim
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUi(val loading: Boolean = false, val outcome: SearchOutcome? = null)

@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: SearchRepository) : ViewModel() {
    private val _ui = MutableStateFlow(SearchUi())
    val ui: StateFlow<SearchUi> = _ui.asStateFlow()
    private var job: Job? = null

    fun query(q: String, role: Role) {
        job?.cancel()
        if (q.trim().length < 2) { _ui.value = SearchUi(); return }
        job = viewModelScope.launch {
            delay(250) // debounce
            _ui.value = SearchUi(loading = true)
            _ui.value = SearchUi(outcome = repo.search(q, role))
        }
    }
}

@Composable
fun GlobalSearchDialog(role: Role, onClose: () -> Unit, onOpen: (route: String, query: String?) -> Unit, vm: SearchViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var q by remember { mutableStateOf("") }
    LaunchedEffect(q) { vm.query(q, role) }
    val strings = com.growthaxys.churchmanagements.core.LocalStrings.current
    val featureHits = if (q.trim().length >= 2) Destinations.forRole(role).filter { strings.t(it.key).contains(q.trim(), ignoreCase = true) } else emptyList()

    AppDialog(t("search"), onClose, wide = true) {
        SearchField(q, { q = it }, t("search"))
        val outcome = ui.outcome
        when {
            q.trim().length < 2 -> Text(t("search-hint"), color = Tokens.MutedForeground, fontSize = 13.sp)
            ui.loading -> Text(t("searching"), color = Tokens.MutedForeground, fontSize = 13.sp)
            outcome != null && outcome.hits.isEmpty() && featureHits.isEmpty() ->
                Text(if (outcome.failed) t("search-failed") else t("no-results"), color = Tokens.MutedForeground, fontSize = 13.sp)
        }
        if (featureHits.isNotEmpty()) Group(t("all-features"), 0) {
            featureHits.forEachIndexed { index, d -> HitRow(t(d.key), null, index) { onOpen(d.route, null) } }
        }
        outcome?.hits?.groupBy { it.group }?.entries?.forEachIndexed { groupIndex, (g, hits) ->
            Group(groupLabel(g), 80 + groupIndex * 70) {
                hits.forEachIndexed { index, h -> HitRow(h.title, h.subtitle, index) { onOpen(h.route, h.query) } }
            }
        }
    }
}

@Composable
private fun groupLabel(g: String): String = when (g) {
    "members" -> t("members"); "families" -> t("families"); "groups" -> t("groups"); "events" -> t("events")
    "donations" -> t("donations"); "finance" -> t("finance"); "volunteers" -> t("volunteers")
    "communication" -> t("communication"); "user-management" -> t("user-management"); "contacts" -> t("search-contacts"); else -> g
}

@Composable
private fun Group(title: String, delayMs: Int = 0, content: @Composable () -> Unit) {
    Column(Modifier.enterAnim(delayMs = delayMs, dx = (-12).dp, dy = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0x80FFFFFF), letterSpacing = 1.sp)
        content()
    }
}

@Composable
private fun HitRow(title: String, subtitle: String?, index: Int = 0, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().enterAnim(delayMs = 30 + index * 28, dx = (-8).dp, dy = 5.dp).clickable(onClick = onClick).padding(vertical = 8.dp)) {
        Text(title, fontSize = 14.sp, color = Color.White, maxLines = 1)
        if (!subtitle.isNullOrBlank()) Text(subtitle, fontSize = 12.sp, color = Tokens.MutedForeground, maxLines = 1)
    }
}
