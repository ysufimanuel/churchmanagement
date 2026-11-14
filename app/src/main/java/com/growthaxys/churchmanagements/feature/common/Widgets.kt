package com.growthaxys.churchmanagements.feature.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.designsystem.AppButton
import com.growthaxys.churchmanagements.designsystem.AppCard
import com.growthaxys.churchmanagements.designsystem.AppDialog
import com.growthaxys.churchmanagements.designsystem.AppIconButton
import com.growthaxys.churchmanagements.designsystem.BtnVariant
import com.growthaxys.churchmanagements.designsystem.ConfirmDialog
import com.growthaxys.churchmanagements.designsystem.Tokens

/** Standard module page: LazyColumn with the shell's content padding and 16dp rhythm (web `space-y-4`). */
@Composable
fun ModulePage(padding: PaddingValues, content: LazyListScope.() -> Unit) =
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = padding, verticalArrangement = Arrangement.spacedBy(8.dp), content = content)

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.White) {
    AppCard(modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(label, fontSize = 11.sp, color = Tokens.MutedForeground, maxLines = 1)
            Text(value, fontSize = 18.sp, color = valueColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 1)
        }
    }
}

/** Edit / Delete (+ optional detail) icon buttons gated by role. */
@Composable
fun RowActions(canEdit: Boolean, canDelete: Boolean, onEdit: () -> Unit, onDelete: () -> Unit, onDetail: (() -> Unit)? = null) {
    var confirm by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        if (onDetail != null) AppIconButton(Icons.Outlined.Visibility, t("act-detail"), onDetail)
        if (canEdit) AppIconButton(Icons.Outlined.Edit, t("act-edit"), onEdit)
        if (canDelete) AppIconButton(Icons.Outlined.Delete, t("act-delete"), { confirm = true }, tint = Tokens.Red400)
    }
    if (confirm) ConfirmDialog({ confirm = false; onDelete() }, { confirm = false })
}

/** Export button -> format chooser (CSV / XLS / TXT / PDF-print). */
@Composable
fun ExportButton(title: String, filename: String, rows: () -> ExportRows, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    AppButton(t("export"), { open = true }, modifier, variant = BtnVariant.Secondary, icon = Icons.Outlined.Download)
    if (open) AppDialog(t("export"), { open = false }) {
        Exporter.Format.entries.forEach { f ->
            AppButton(f.name, { open = false; Exporter.export(ctx, f, rows(), filename, title) }, Modifier.fillMaxWidth(), variant = BtnVariant.Secondary)
        }
    }
}
