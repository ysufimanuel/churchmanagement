package com.growthaxys.churchmanagements.feature.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.growthaxys.churchmanagements.core.Format
import com.growthaxys.churchmanagements.core.LocalStrings
import com.growthaxys.churchmanagements.core.Rbac
import com.growthaxys.churchmanagements.core.Role
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.data.model.FinanceCategory
import com.growthaxys.churchmanagements.data.model.FinanceTransaction
import com.growthaxys.churchmanagements.data.repo.FinanceData
import com.growthaxys.churchmanagements.data.repo.GivingRepository
import com.growthaxys.churchmanagements.data.repo.TrxInput
import com.growthaxys.churchmanagements.designsystem.*
import com.growthaxys.churchmanagements.feature.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(private val repo: GivingRepository) : FeatureViewModel<FinanceData>() {
    override suspend fun fetch() = repo.finance()
    fun saveTrx(i: TrxInput, autoApprove: Boolean, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveTransaction(i, autoApprove) }
    fun deleteTrx(id: Long) = mutate { repo.deleteTransaction(id) }
    fun process(id: Long, approve: Boolean) = mutate { repo.process(id, approve) }
    fun saveCategory(id: Long?, nama: String, tipe: String, desc: String, onOk: () -> Unit) = mutate(onOk = onOk) { repo.saveCategory(id, nama, tipe, desc) }
    fun deleteCategory(id: Long) = mutate { repo.deleteCategory(id) }
    fun saldo(v: Double, onOk: () -> Unit) = mutate(onOk = onOk) { repo.updateSaldoAwal(v) }
}

private fun statusVariant(s: String) = when (s) { "approved" -> BadgeVariant.Success; "rejected" -> BadgeVariant.Destructive; else -> BadgeVariant.Warning }
private fun statusKey(s: String) = when (s) { "approved" -> "approved"; "rejected" -> "rejected"; else -> "pending" }

@Composable
fun FinanceScreen(role: Role, padding: PaddingValues, initialQuery: String, vm: FinanceViewModel = hiltViewModel()) {
    LoadGate(vm, padding, com.growthaxys.churchmanagements.designsystem.SkeletonKind.Tabs) { data ->
        val busy by vm.busy.collectAsStateWithLifecycle()
        val s = LocalStrings.current
        var tab by remember { mutableStateOf("dashboard") }
        var search by remember { mutableStateOf(initialQuery) }
        var catFilter by remember { mutableStateOf("") }
        var trx by remember { mutableStateOf<TrxInput?>(null) }
        var cat by remember { mutableStateOf<FinanceCategory?>(null) }
        var catNew by remember { mutableStateOf(false) }
        var saldoDialog by remember { mutableStateOf(false) }
        val canApprove = Rbac.canApprove(role)
        val catName = { id: Long? -> data.categories.firstOrNull { it.id == id }?.nama ?: "-" }
        val approved = data.transactions.filter { it.status == "approved" }
        val income = approved.filter { it.tipe == "pemasukan" }.sumOf { it.jumlah }
        val expense = approved.filter { it.tipe == "pengeluaran" }.sumOf { it.jumlah }
        val balance = data.saldoAwal + income - expense
        val pending = data.transactions.filter { it.status == "pending" }
        val q = search.trim().lowercase()
        fun listFor(tipe: String) = data.transactions.filter { x ->
            x.tipe == tipe && (catFilter.isEmpty() || x.kategoriId?.toString() == catFilter) &&
                (q.isEmpty() || (x.keterangan ?: "").lowercase().contains(q) || catName(x.kategoriId).lowercase().contains(q))
        }
        val tabs = buildList {
            add(TabItem("dashboard", t("finance-dashboard"))); add(TabItem("pemasukan", t("income"))); add(TabItem("pengeluaran", t("expense")))
            add(TabItem("kategori", t("categories"))); add(TabItem("laporan", t("finance-report")))
            if (Rbac.canAdd(role)) add(TabItem("approval", t("approval") + if (pending.isNotEmpty()) " (${pending.size})" else ""))
        }
        fun emptyTrx(tipe: String) = TrxInput(null, tipe, null, 0.0, Format.today(), "", null)

        ModulePage(padding) {
            item { PageHeader(t("finance"), t("manage-finance")) }
            item { AppTabs(tabs, tab, { tab = it }) }

            when (tab) {
                "dashboard" -> {
                    item {
                        AppCard {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(t("current-balance"), fontSize = 13.sp, color = Tokens.MutedForeground)
                                Text(Format.rupiah(balance), fontSize = 24.sp, color = if (balance >= 0) Tokens.Emerald400 else Tokens.Red400, fontWeight = FontWeight.Bold)
                                if (canApprove) AppButton("${t("saldo-awal")}: ${Format.rupiah(data.saldoAwal)}", { saldoDialog = true }, variant = BtnVariant.Ghost, height = 36.dp)
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard(t("total-income"), Format.rupiahCompact(income, s.lang), Modifier.weight(1f), Tokens.Emerald400)
                            StatCard(t("total-expense"), Format.rupiahCompact(expense, s.lang), Modifier.weight(1f), Tokens.Red400)
                            StatCard(t("total-donors"), data.donors.size.toString(), Modifier.weight(1f))
                        }
                    }
                    item {
                        val months = approved.map { it.tanggal.take(7) }.distinct().sorted().takeLast(6)
                        AppCard {
                            Column(Modifier.padding(16.dp)) {
                                Text(t("income-expense-chart"), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                LineChartView(
                                    months.map { Format.monthShort(it, s.lang) },
                                    listOf(
                                        Triple(t("income"), months.map { m -> approved.filter { it.tipe == "pemasukan" && it.tanggal.startsWith(m) }.sumOf { it.jumlah } }, Tokens.Emerald400),
                                        Triple(t("expense"), months.map { m -> approved.filter { it.tipe == "pengeluaran" && it.tanggal.startsWith(m) }.sumOf { it.jumlah } }, Tokens.Red400),
                                    ),
                                    Modifier.padding(top = 12.dp), 200.dp,
                                )
                            }
                        }
                    }
                    for (tipe in listOf("pemasukan", "pengeluaran")) item(key = "pie-$tipe") {
                        val grouped = approved.filter { it.tipe == tipe }.groupBy { catName(it.kategoriId) }.map { it.key to it.value.sumOf { x -> x.jumlah } }
                        AppCard {
                            Column(Modifier.padding(16.dp)) {
                                Text(t(if (tipe == "pemasukan") "income-by-category" else "expense-by-category"), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                DonutChartView(grouped, Modifier.padding(top = 12.dp), 200.dp)
                            }
                        }
                    }
                }

                "pemasukan", "pengeluaran" -> {
                    val tipe = tab
                    val rows = listFor(tipe)
                    item {
                        FilterRow {
                            SearchField(search, { search = it }, t("search"))
                            AppSelect(catFilter, listOf(Option("", t("all-categories"))) + data.categories.filter { it.tipe == tipe }.map { Option(it.id.toString(), it.nama) }, { catFilter = it })
                            if (Rbac.canAdd(role)) AppButton(t(if (tipe == "pemasukan") "add-income" else "add-expense"), { trx = emptyTrx(tipe) }, icon = Icons.Outlined.Add)
                        }
                    }
                    items(rows, key = { it.id }) { x ->
                        DataRowCard(
                            catName(x.kategoriId),
                            listOf(
                                t("date") to { FieldText(Format.date(x.tanggal, s.lang)) },
                                t("amount") to { FieldText(Format.rupiah(x.jumlah), if (tipe == "pemasukan") Tokens.Emerald400 else Tokens.Red400) },
                                t("status") to { AppBadge(t(statusKey(x.status)), statusVariant(x.status)) },
                                t("description") to { FieldText(x.keterangan) },
                            ),
                            actions = {
                                // Approved rows are immutable for non-superadmin (RLS enforces too).
                                val editable = x.status != "approved" || canApprove
                                RowActions(Rbac.canEdit(role) && editable, Rbac.canDelete(role),
                                    onEdit = { trx = TrxInput(x.id, x.tipe, x.kategoriId, x.jumlah, x.tanggal, x.keterangan.orEmpty(), x.donaturId) },
                                    onDelete = { vm.deleteTrx(x.id) })
                            },
                        )
                    }
                    if (rows.isEmpty()) item { EmptyText() }
                }

                "kategori" -> {
                    item { if (Rbac.canAdd(role)) AppButton(t("add-category"), { catNew = true; cat = null }, icon = Icons.Outlined.Add) }
                    items(data.categories, key = { it.id }) { c ->
                        DataRowCard(
                            c.nama,
                            listOf(
                                t("category-type") to { AppBadge(t(if (c.tipe == "pemasukan") "income" else "expense"), if (c.tipe == "pemasukan") BadgeVariant.Success else BadgeVariant.Destructive) },
                                t("description") to { FieldText(c.deskripsi) },
                            ),
                            actions = { RowActions(Rbac.canEdit(role), Rbac.canDelete(role), onEdit = { cat = c; catNew = true }, onDelete = { vm.deleteCategory(c.id) }) },
                        )
                    }
                    if (data.categories.isEmpty()) item { EmptyText() }
                }

                "laporan" -> {
                    item {
                        ExportButton(t("finance-report"), "finance-report", {
                            approved.map { mapOf(s.t("date") to it.tanggal, s.t("type") to s.t(if (it.tipe == "pemasukan") "income" else "expense"), s.t("category") to catName(it.kategoriId), s.t("amount") to it.jumlah, s.t("description") to it.keterangan) }
                        })
                    }
                    item { StatCard(t("current-balance"), Format.rupiah(balance)) }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard(t("total-income"), Format.rupiahCompact(income, s.lang), Modifier.weight(1f), Tokens.Emerald400)
                            StatCard(t("total-expense"), Format.rupiahCompact(expense, s.lang), Modifier.weight(1f), Tokens.Red400)
                        }
                    }
                    items(approved, key = { it.id }) { x ->
                        DataRowCard(catName(x.kategoriId), listOf(
                            t("date") to { FieldText(Format.date(x.tanggal, s.lang)) },
                            t("amount") to { FieldText(Format.rupiah(x.jumlah), if (x.tipe == "pemasukan") Tokens.Emerald400 else Tokens.Red400) },
                        ))
                    }
                }

                else -> { // approval
                    item { Text(t("pending-approval"), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold) }
                    items(pending, key = { it.id }) { x ->
                        DataRowCard(
                            catName(x.kategoriId),
                            listOf(
                                t("date") to { FieldText(Format.date(x.tanggal, s.lang)) },
                                t("type") to { FieldText(t(if (x.tipe == "pemasukan") "income" else "expense")) },
                                t("amount") to { FieldText(Format.rupiah(x.jumlah), if (x.tipe == "pemasukan") Tokens.Emerald400 else Tokens.Red400) },
                                t("description") to { FieldText(x.keterangan) },
                            ),
                            actions = {
                                if (canApprove) {
                                    AppButton(t("approve"), { vm.process(x.id, true) }, icon = Icons.Outlined.Check, height = 36.dp, enabled = !busy)
                                    AppButton(t("reject"), { vm.process(x.id, false) }, variant = BtnVariant.Destructive, icon = Icons.Outlined.Close, height = 36.dp, enabled = !busy)
                                }
                            },
                        )
                    }
                    if (pending.isEmpty()) item { EmptyText() }
                    item { Text(t("approval-history"), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold) }
                    items(data.history, key = { it.id }) { h ->
                        val tx = data.transactions.firstOrNull { it.id == h.transactionId }
                        DataRowCard(tx?.let { catName(it.kategoriId) } ?: "#${h.transactionId}", listOf(
                            t("status") to { AppBadge(t(statusKey(h.action)), statusVariant(h.action)) },
                            t("date") to { FieldText(Format.dateTime(h.createdAt, s.lang)) },
                        ))
                    }
                    if (data.history.isEmpty()) item { EmptyText() }
                }
            }
        }

        trx?.let { init ->
            var f by remember(init) { mutableStateOf(init) }
            var amount by remember(init) { mutableStateOf(if (init.jumlah > 0) init.jumlah.toLong().toString() else "") }
            AppDialog(t(if (init.tipe == "pemasukan") "add-income" else "add-expense"), { trx = null }) {
                LabeledField(t("category")) { AppSelect(f.kategoriId?.toString().orEmpty(), data.categories.filter { it.tipe == f.tipe }.map { Option(it.id.toString(), it.nama) }, { f = f.copy(kategoriId = it.toLongOrNull()) }, placeholder = t("select-category")) }
                LabeledField(t("amount")) { AppTextField(amount, { amount = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number) }
                LabeledField(t("date")) { DateField(f.tanggal, { f = f.copy(tanggal = it) }) }
                if (f.tipe == "pemasukan") LabeledField(t("donor")) { AppSelect(f.donaturId?.toString().orEmpty(), listOf(Option("", t("none"))) + data.donors.map { Option(it.id.toString(), it.nama) }, { f = f.copy(donaturId = it.toLongOrNull()) }) }
                LabeledField(t("description")) { AppTextField(f.keterangan, { f = f.copy(keterangan = it) }, singleLine = false, minHeight = 80.dp) }
                DialogActions({ trx = null }, {
                    val n = amount.toDoubleOrNull() ?: 0.0
                    if (n > 0 && f.kategoriId != null && f.tanggal.isNotBlank()) vm.saveTrx(f.copy(jumlah = n), canApprove) { trx = null }
                }, saving = busy)
            }
        }

        if (catNew) {
            val init = cat
            var nama by remember(init) { mutableStateOf(init?.nama.orEmpty()) }
            var tipe by remember(init) { mutableStateOf(init?.tipe ?: "pemasukan") }
            var desc by remember(init) { mutableStateOf(init?.deskripsi.orEmpty()) }
            AppDialog(t("add-category"), { catNew = false }) {
                LabeledField(t("category-name")) { AppTextField(nama, { nama = it }) }
                LabeledField(t("category-type")) { AppSelect(tipe, listOf(Option("pemasukan", t("income")), Option("pengeluaran", t("expense"))), { tipe = it }) }
                LabeledField(t("description")) { AppTextField(desc, { desc = it }, singleLine = false, minHeight = 80.dp) }
                DialogActions({ catNew = false }, { if (nama.isNotBlank()) vm.saveCategory(init?.id, nama.trim(), tipe, desc) { catNew = false } }, saving = busy)
            }
        }

        if (saldoDialog) {
            var v by remember { mutableStateOf(data.saldoAwal.toLong().toString()) }
            AppDialog(t("saldo-awal"), { saldoDialog = false }) {
                LabeledField(t("amount")) { AppTextField(v, { v = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Number) }
                DialogActions({ saldoDialog = false }, { vm.saldo(v.toDoubleOrNull() ?: 0.0) { saldoDialog = false } }, saving = busy)
            }
        }
    }
}
