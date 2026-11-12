package com.growthaxys.churchmanagements.feature.donations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.Donation
import com.growthaxys.churchmanagements.data.model.Donor
import com.growthaxys.churchmanagements.data.repo.DonationInput
import com.growthaxys.churchmanagements.data.repo.GivingRepository
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private val TYPES = listOf("perpuluhan", "persembahan", "pembangunan", "sosial")

data class DonationsData(val donations: List<Donation>, val donors: List<Donor>)

@HiltViewModel
class DonationsViewModel @Inject constructor(private val repo: GivingRepository) : FeatureViewModel<DonationsData>() {
    override suspend fun fetch() = DonationsData(repo.donations(), repo.donors())
    fun save(i: DonationInput, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveDonation(i) }
    fun delete(id: Long) = mutate { repo.deleteDonation(id) }
}

@Composable
fun DonationsScreen(role: Role, padding: PaddingValues, initialQuery: String, vm: DonationsViewModel = hiltViewModel()) {
    LoadGate(vm) { data ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        val s = LocalStrings.current
        var search by remember { mutableStateOf(initialQuery) }
        var type by remember { mutableStateOf("") }
        var editing by remember { mutableStateOf<DonationInput?>(null) }
        val donorName = { d: Donation -> d.donors?.nama ?: data.donors.firstOrNull { it.id == d.donorId }?.nama ?: "-" }
        val q = search.trim().lowercase()
        val filtered = data.donations.filter { d ->
            (q.isEmpty() || listOf(donorName(d), d.keterangan, d.tipe, d.customType).any { (it ?: "").lowercase().contains(q) }) && (type.isEmpty() || d.tipe == type)
        }
        val total = data.donations.sumOf { it.jumlah }

        ModulePage(padding) {
            item { PageHeader(t("donations"), t("manage-donations")) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(t("total-donations"), Format.rupiahCompact(total, s.lang), Modifier.weight(1f), Tokens.Emerald400)
                    StatCard(t("total-donors"), data.donors.size.toString(), Modifier.weight(1f))
                }
            }
            item { StatCard(t("average-donation"), Format.rupiah(if (data.donations.isEmpty()) 0.0 else total / data.donations.size)) }
            item {
                FilterRow {
                    SearchField(search, { search = it }, t("search-donations"))
                    AppSelect(type, listOf(Option("", t("all-types"))) + TYPES.map { Option(it, t(it)) }, { type = it })
                    ExportButton(t("donations-report"), "donations", {
                        filtered.map { mapOf(s.t("exp-date") to it.tanggal, s.t("donor") to donorName(it), s.t("exp-type") to (it.customType ?: it.tipe), s.t("exp-amount") to it.jumlah, s.t("exp-desc") to it.keterangan) }
                    })
                    if (Rbac.canAdd(role)) AppButton(t("add-donation"), { editing = DonationInput(null, null, "persembahan", "", 0.0, Format.today(), "") }, icon = Icons.Outlined.Add)
                }
            }
            items(filtered, key = { it.id }) { d ->
                DataRowCard(
                    donorName(d),
                    listOf(
                        t("date") to { FieldText(Format.date(d.tanggal, s.lang)) },
                        t("type") to { AppBadge(d.customType ?: t(d.tipe), BadgeVariant.Default) },
                        t("amount") to { FieldText(Format.rupiah(d.jumlah), Tokens.Emerald400) },
                        t("description") to { FieldText(d.keterangan) },
                    ),
                    actions = {
                        RowActions(Rbac.canEdit(role), Rbac.canDelete(role),
                            onEdit = { editing = DonationInput(d.id, d.donorId, d.tipe, d.customType.orEmpty(), d.jumlah, d.tanggal, d.keterangan.orEmpty()) },
                            onDelete = { vm.delete(d.id) })
                    },
                )
            }
            if (filtered.isEmpty()) item { EmptyText() }
        }

        editing?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            var amount by remember(init) { mutableStateOf(if (init.jumlah > 0) init.jumlah.toLong().toString() else "") }
            AppDialog(t("add-donation"), { editing = null }) {
                LabeledField(t("select-donor")) { AppSelect(f.donorId?.toString().orEmpty(), listOf(Option("", t("select"))) + data.donors.map { Option(it.id.toString(), it.nama) }, { f = f.copy(donorId = it.toLongOrNull()) }) }
                LabeledField(t("donation-type")) { AppSelect(f.tipe, TYPES.map { Option(it, t(it)) }, { f = f.copy(tipe = it) }) }
                LabeledField(t("amount")) { AppTextField(amount, { amount = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number) }
                LabeledField(t("date")) { DateField(f.tanggal, { f = f.copy(tanggal = it) }) }
                LabeledField(t("description")) { AppTextField(f.keterangan, { f = f.copy(keterangan = it) }, singleLine = false, minHeight = 80.dp) }
                DialogActions({ editing = null }, {
                    val n = amount.toDoubleOrNull() ?: 0.0
                    if (n > 0 && f.tanggal.isNotBlank()) vm.save(f.copy(jumlah = n)) { editing = null }
                }, saving = busy)
            }
        }
    }
}
