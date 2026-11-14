package com.growthaxys.churchmanagements.feature.webmanager

import com.growthaxys.churchmanagements.data.repo.SectionDraft
import com.growthaxys.churchmanagements.data.repo.ThemeValues
import com.growthaxys.churchmanagements.data.repo.WebManagerRepository
import com.growthaxys.churchmanagements.data.repo.WmData
import com.growthaxys.churchmanagements.data.repo.WmSection
import com.growthaxys.churchmanagements.feature.common.FeatureViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

@HiltViewModel
class WebManagerViewModel @Inject constructor(private val repo: WebManagerRepository) : FeatureViewModel<WmData>() {
    override suspend fun fetch() = repo.load()

    fun saveNav(id: Long?, label: String, href: String, visible: Boolean, order: Int, onOk: () -> Unit) = mutate("saved-ok", onOk) { repo.saveNavItem(id, label, href, visible, order) }
    fun toggleNav(row: JsonObject, v: Boolean) = mutate("saved-ok") { repo.toggleNavVisibility(row, v) }
    fun deleteNav(row: JsonObject) = mutate("deleted-ok") { repo.deleteNavItem(row) }
    fun publishNav() = mutate("publish") { repo.publishNav() }

    fun saveTheme(t: ThemeValues) = mutate("wm-draft") { repo.saveThemeDraft(t) }
    fun publishTheme() = mutate("publish") { repo.publishTheme() }
    fun restoreDefaultTheme(onOk: () -> Unit) = mutate("wm-draft", onOk) { repo.restoreDefaultThemeDraft() }
    fun setDefaultThemePref(on: Boolean) = mutate("saved-ok") { repo.saveWebsiteThemePreference(on) }

    fun saveSection(d: SectionDraft, onOk: (Long?) -> Unit) = mutateWithId("wm-draft", onOk) { repo.saveSectionDraft(d) }
    fun saveContent(s: WmSection, content: JsonObject, onOk: () -> Unit) = mutate("wm-draft", onOk) { repo.saveSectionContent(s, content) }
    fun reorder(ids: List<Long>) = mutate("saved-ok") { repo.reorderSections(ids) }
    fun setEnabled(id: Long, on: Boolean) = mutate("saved-ok") { repo.setSectionEnabled(id, on) }
    fun deleteSection(s: WmSection) = mutate("deleted-ok") { repo.deleteSection(s) }
    fun publishPage(page: String) = mutate("publish") { repo.publishSections(page) }
    fun publishAll() = mutate("publish") { repo.publishWebManager() }
}
