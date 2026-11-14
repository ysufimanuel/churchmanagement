@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.growthaxys.churchmanagements.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.growthaxys.churchmanagements.core.t
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/* ---------- Background: web body::before mesh gradient ---------- */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(Tokens.Background)
            .drawBehind {
                val w = size.width
                val h = size.height
                fun glow(c: Color, cx: Float, cy: Float, r: Float) = drawRect(
                    Brush.radialGradient(listOf(c, Color.Transparent), center = Offset(w * cx, h * cy), radius = r)
                )
                glow(Color(0x3800E5FF), 0.12f, 0.08f, w * 0.9f)   // rgb(0 229 255 / .22)
                glow(Color(0x337C5CFF), 0.90f, 0.15f, w * 0.8f)   // rgb(124 92 255 / .20)
                glow(Color(0x2410B981), 0.80f, 0.95f, w * 0.85f)  // rgb(16 185 129 / .14)
                glow(Color(0x1AFF7850), 0.05f, 0.90f, w * 0.7f)   // rgb(255 120 80 / .10)
            },
        content = content,
    )
}

/* ---------- Glass surfaces (.glass / .glass-strong / .glass-subtle) ---------- */
@Composable
fun Glass(
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = Tokens.CardShape,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val brush = Brush.linearGradient(
        listOf(
            if (strong) Tokens.GlassStrongTop else Tokens.GlassTop,
            if (strong) Tokens.GlassStrongBottom else Tokens.GlassBottom,
        )
    )
    val base = modifier
        .clip(shape)
        .background(Tokens.GlassFallback.copy(alpha = 0.55f))
        .background(brush)
        .border(1.dp, Tokens.Border, shape)
    val src = remember { MutableInteractionSource() }
    Column(
        if (onClick != null) base.pressScale(src, 0.985f).clickable(interactionSource = src, indication = null, onClick = onClick) else base,
        content = content,
    )
}

@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    Glass(modifier.fillMaxWidth(), content = content)

@Composable
fun CardHeaderRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) =
    Row(modifier.fillMaxWidth().padding(12.dp, 12.dp, 12.dp, 4.dp), verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween, content = content)

@Composable
fun CardBody(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    Column(modifier.fillMaxWidth().padding(12.dp, 0.dp, 12.dp, 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = content)

/* ---------- Buttons (web Button: h-10 rounded-lg text-sm font-medium) ---------- */
enum class BtnVariant { Default, Secondary, Destructive, Outline, Ghost }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BtnVariant = BtnVariant.Default,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 40.dp,
) {
    val (bg, fg, border) = when (variant) {
        BtnVariant.Default -> Triple(Tokens.Primary, Tokens.OnPrimary, null)
        BtnVariant.Secondary -> Triple(Color(0x1AFFFFFF), Tokens.OnSecondary, Color(0x1AFFFFFF))
        BtnVariant.Destructive -> Triple(Tokens.Destructive, Color.White, null)
        BtnVariant.Outline -> Triple(Color(0x0DFFFFFF), Color.White, Color(0x33FFFFFF))
        BtnVariant.Ghost -> Triple(Color.Transparent, Color.White, null)
    }
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    // `.btn-fx-default::after`: light sweep across the primary button (on touch: while pressed).
    val shine by animateFloatAsState(if (pressed) 1f else 0f, tween(if (pressed) 700 else 0, easing = EaseOutStrong), label = "shine")
    Row(
        modifier
            .heightIn(min = height)
            .alpha(if (enabled) 1f else 0.5f)
            .pressScale(src)
            .clip(Tokens.ControlShape)
            .background(bg)
            .let { if (border != null) it.border(1.dp, border, Tokens.ControlShape) else it }
            .let {
                if (variant != BtnVariant.Default) it else it.drawWithContent {
                    drawContent()
                    val w = size.width
                    val x = -1.1f * w + shine * 2.2f * w
                    drawRect(Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent), start = Offset(x, 0f), end = Offset(x + w * 0.5f, size.height)))
                }
            }
            .clickable(interactionSource = src, indication = null, enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(16.dp), color = fg, strokeWidth = 2.dp)
        else if (icon != null) Icon(icon, null, Modifier.size(16.dp).iconFx(icon, pressed), tint = fg)
        Text(text, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 40.dp,
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    Box(
        modifier.size(size).pressScale(src).clip(Tokens.ControlShape).background(if (pressed) Color(0x1AFFFFFF) else Color.Transparent)
            .clickable(interactionSource = src, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription, Modifier.size(16.dp).iconFx(icon, pressed), tint = tint) }
}

/* ---------- Badges ---------- */
enum class BadgeVariant { Default, Secondary, Destructive, Outline, Success, Warning }

@Composable
fun AppBadge(text: String, variant: BadgeVariant = BadgeVariant.Default, modifier: Modifier = Modifier) {
    val (bg, fg, bd) = when (variant) {
        BadgeVariant.Default -> Triple(Tokens.Primary, Tokens.OnPrimary, null)
        BadgeVariant.Secondary -> Triple(Tokens.Secondary, Tokens.OnSecondary, null)
        BadgeVariant.Destructive -> Triple(Tokens.Destructive, Color.White, null)
        BadgeVariant.Outline -> Triple(Color.Transparent, Color.White, Tokens.Border)
        BadgeVariant.Success -> Triple(Color(0x3310B981), Tokens.Emerald400, Color(0x4D10B981))
        BadgeVariant.Warning -> Triple(Color(0x33F59E0B), Tokens.Amber400, Color(0x4DF59E0B))
    }
    Text(
        text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1,
        modifier = modifier
            .clip(CircleShape).background(bg)
            .let { if (bd != null) it.border(1.dp, bd, CircleShape) else it }
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}

/* ---------- Text fields (h-10 glass-input rounded-lg) ---------- */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minHeight: Dp = 40.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
    password: Boolean = false,
    enabled: Boolean = true,
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
        cursorBrush = SolidColor(Tokens.Primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.fillMaxWidth().onFocus { focused = it },
        decorationBox = { inner ->
            Row(
                Modifier
                    .heightIn(min = minHeight)
                    .clip(Tokens.ControlShape)
                    .background(if (focused) Color(0x1AFFFFFF) else Tokens.InputBg)
                    .border(1.dp, if (focused) Tokens.Primary.copy(alpha = 0.7f) else Tokens.InputBorder, Tokens.ControlShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .alpha(if (enabled) 1f else 0.5f),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (leading != null) Icon(leading, null, Modifier.size(16.dp), tint = Tokens.MutedForeground)
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty())
                        Text(placeholder, color = Tokens.MutedForeground, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    inner()
                }
                trailing?.invoke()
            }
        },
    )
}

private fun Modifier.onFocus(cb: (Boolean) -> Unit): Modifier =
    this.onFocusChanged { cb(it.isFocused) }

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) =
    AppTextField(value, onValueChange, modifier, placeholder, leading = Icons.Outlined.Search)

@Composable
fun LabeledField(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) { FieldLabel(label); content() }
}

/* ---------- Select (web <select>) ---------- */
data class Option(val value: String, val label: String)

@Composable
fun AppSelect(
    value: String,
    options: List<Option>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
) {
    var open by remember { mutableStateOf(false) }
    val shown = options.firstOrNull { it.value == value }?.label ?: placeholder
    Box(modifier) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 40.dp)
                .clip(Tokens.ControlShape).background(Tokens.InputBg)
                .border(1.dp, Tokens.InputBorder, Tokens.ControlShape)
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(enabled = enabled) { open = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(shown, color = if (value.isEmpty()) Tokens.MutedForeground else Color.White, fontSize = 14.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.KeyboardArrowDown, null, Modifier.size(16.dp), tint = Tokens.MutedForeground)
        }
        DropdownMenu(open, { open = false }, Modifier.heightIn(max = 320.dp).background(Color(0xFF1A1D24))) {
            options.forEach { o ->
                DropdownMenuItem(
                    text = { Text(o.label, fontSize = 14.sp, color = Color.White) },
                    onClick = { onSelect(o.value); open = false },
                )
            }
        }
    }
}

/* ---------- Date / datetime inputs (web type=date / datetime-local) ---------- */
@Composable
fun DateField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "") {
    var open by remember { mutableStateOf(false) }
    PickerBox(value.ifEmpty { placeholder }, value.isEmpty(), modifier) { open = true }
    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = runCatching { LocalDate.parse(value.take(10)).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() }.getOrNull()
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton({
                    state.selectedDateMillis?.let { onChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()) }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton({ open = false }) { Text(t("cancel")) } },
        ) { DatePicker(state) }
    }
}

/** value format: yyyy-MM-ddTHH:mm (same as <input type="datetime-local">). */
@Composable
fun DateTimeField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var dateOpen by remember { mutableStateOf(false) }
    var timeOpen by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf("") }
    PickerBox(value.replace('T', ' '), value.isEmpty(), modifier) { dateOpen = true }
    if (dateOpen) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = runCatching { LocalDate.parse(value.take(10)).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() }.getOrNull()
        )
        DatePickerDialog(
            onDismissRequest = { dateOpen = false },
            confirmButton = {
                TextButton({
                    state.selectedDateMillis?.let {
                        pendingDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        dateOpen = false; timeOpen = true
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton({ dateOpen = false }) { Text(t("cancel")) } },
        ) { DatePicker(state) }
    }
    if (timeOpen) {
        val h = value.drop(11).take(2).toIntOrNull() ?: 9
        val m = value.drop(14).take(2).toIntOrNull() ?: 0
        val ts = rememberTimePickerState(h, m, true)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { timeOpen = false },
            confirmButton = {
                TextButton({
                    onChange("%sT%02d:%02d".format(pendingDate, ts.hour, ts.minute)); timeOpen = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton({ timeOpen = false }) { Text(t("cancel")) } },
            text = { TimePicker(ts) },
        )
    }
}

@Composable
fun TimeField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    PickerBox(value.take(5), value.isEmpty(), modifier) { open = true }
    if (open) {
        val ts = rememberTimePickerState(value.take(2).toIntOrNull() ?: 9, value.drop(3).take(2).toIntOrNull() ?: 0, true)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = { TextButton({ onChange("%02d:%02d".format(ts.hour, ts.minute)); open = false }) { Text("OK") } },
            dismissButton = { TextButton({ open = false }) { Text(t("cancel")) } },
            text = { TimePicker(ts) },
        )
    }
}

@Composable
private fun PickerBox(text: String, isPlaceholder: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.fillMaxWidth().heightIn(min = 40.dp)
            .clip(Tokens.ControlShape).background(Tokens.InputBg)
            .border(1.dp, Tokens.InputBorder, Tokens.ControlShape)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { Text(text.ifEmpty { "—" }, color = if (isPlaceholder) Tokens.MutedForeground else Color.White, fontSize = 14.sp) }
}

/* ---------- Switch ---------- */
@Composable
fun AppSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(
        Modifier.size(44.dp, 24.dp).clip(CircleShape)
            .background(if (checked) Tokens.Primary else Tokens.Secondary)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier.padding(start = if (checked) 24.dp else 4.dp, top = 4.dp).size(16.dp).clip(CircleShape).background(Color.White)
        )
    }
}

/* ---------- Tabs (glass-subtle container, active = solid primary pill) ---------- */
data class TabItem(val id: String, val label: String)

@Composable
fun AppTabs(tabs: List<TabItem>, active: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val spots = remember { androidx.compose.runtime.mutableStateMapOf<String, FloatArray>() } // x, width, height (px)
    var viewport by remember { mutableStateOf(0) }
    val target = spots[active]
    val pillX = remember { androidx.compose.animation.core.Animatable(0f) }
    val pillW = remember { androidx.compose.animation.core.Animatable(0f) }
    var placed by remember { mutableStateOf(false) }
    // Same motion language as the bottom-nav lens: spring slide, stretches while travelling.
    androidx.compose.runtime.LaunchedEffect(target?.get(0), target?.get(1)) {
        val t = target ?: return@LaunchedEffect
        if (!placed) { pillX.snapTo(t[0]); pillW.snapTo(t[1]); placed = true }
        else kotlinx.coroutines.coroutineScope {
            launch { pillX.animateTo(t[0], androidx.compose.animation.core.spring(0.74f, 320f)) }
            launch { pillW.animateTo(t[1], androidx.compose.animation.core.spring(0.82f, 340f)) }
        }
    }
    androidx.compose.runtime.LaunchedEffect(target?.get(0), viewport) {
        val t = target ?: return@LaunchedEffect
        if (viewport > 0) scroll.animateScrollTo((t[0] + t[1] / 2 - viewport / 2f).toInt().coerceAtLeast(0))
    }
    Box(
        modifier.fillMaxWidth().clip(Tokens.ControlShape).background(Color(0x0AFFFFFF))
            .border(1.dp, Tokens.BorderSubtle, Tokens.ControlShape)
            .onSizeChanged { viewport = it.width },
    ) {
        Box(Modifier.horizontalScroll(scroll).padding(3.dp)) {
            if (placed && target != null) {
                val travel = (kotlin.math.abs(pillX.value - target[0]) / target[1].coerceAtLeast(1f)).coerceIn(0f, 1f)
                Box(
                    Modifier
                        .offset { androidx.compose.ui.unit.IntOffset(pillX.value.toInt(), 0) }
                        .size(with(density) { pillW.value.toDp() }, with(density) { target[2].toDp() })
                        .graphicsLayer { scaleX = 1f + 0.14f * travel; scaleY = 1f - 0.06f * travel }
                        .clip(Tokens.ChipShape).background(Tokens.Primary),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                tabs.forEach { tab ->
                    val on = tab.id == active
                    val tint by androidx.compose.animation.animateColorAsState(
                        if (on) Tokens.OnPrimary else Tokens.MutedForeground, androidx.compose.animation.core.tween(200), label = "tab-tint",
                    )
                    Text(
                        tab.label, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, color = tint,
                        modifier = Modifier
                            .onGloballyPositioned { spots[tab.id] = floatArrayOf(it.positionInParent().x, it.size.width.toFloat(), it.size.height.toFloat()) }
                            .clip(Tokens.ChipShape)
                            .clickable { onChange(tab.id) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

/* ---------- Dialog (web Modal: glass-strong, max-w-md / max-w-2xl) ---------- */
@Composable
fun AppDialog(title: String, onDismiss: () -> Unit, wide: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Glass(
                Modifier.widthIn(max = if (wide) 672.dp else 448.dp).fillMaxWidth().heightIn(max = 640.dp)
                    .enterAnim(dy = 16.dp, durationMs = 300, fromScale = 0.97f),
                strong = true,
            ) {
                Column(Modifier.background(Color(0xE60F1117)).padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.weight(1f))
                        AppIconButton(Icons.Outlined.Close, t("close"), onDismiss)
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AppDialog(t("confirmation"), onDismiss) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            AppButton(t("cancel"), onDismiss, variant = BtnVariant.Secondary)
            AppButton(t("act-delete"), onConfirm, variant = BtnVariant.Destructive)
        }
    }
}

@Composable
fun DialogActions(onCancel: () -> Unit, onSave: () -> Unit, saving: Boolean = false, saveLabel: String = t("save")) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
        AppButton(t("cancel"), onCancel, variant = BtnVariant.Secondary)
        AppButton(saveLabel, onSave, loading = saving)
    }
}

/* ---------- Page header / misc ---------- */
@Composable
fun PageHeader(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Tokens.MutedForeground, maxLines = 1)
    }
}

@Composable
fun EmptyText(text: String = t("none"), modifier: Modifier = Modifier) =
    Text(text, modifier.fillMaxWidth().padding(8.dp), color = Tokens.MutedForeground, fontSize = 14.sp, textAlign = TextAlign.Center)

@Composable
fun LoadingBox(modifier: Modifier = Modifier) =
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Tokens.Primary, strokeWidth = 2.dp)
    }

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit) {
    AppCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(t("page-load-failed"), fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(message, color = Tokens.MutedForeground, fontSize = 12.sp, textAlign = TextAlign.Center)
            AppButton(t("try-again"), onRetry, variant = BtnVariant.Secondary)
        }
    }
}

@Composable
fun InitialsAvatar(name: String?, modifier: Modifier = Modifier, size: Dp = 36.dp, imageUrl: String? = null, fontSize: Int = 12) {
    val initials = (name ?: "").trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.codePointAt(0).let { cp -> String(Character.toChars(cp)) } }.uppercase().ifEmpty { "?" }
    Box(modifier.size(size).clip(CircleShape).background(Tokens.Primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
        if (!imageUrl.isNullOrBlank()) AsyncImage(imageUrl, name, Modifier.fillMaxSize())
        else Text(initials, color = Tokens.Primary, fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Mobile "table → compact card": title + inline label/value pairs that wrap (several per line).
 * Only the first [COLLAPSED_FIELDS] fields show until the card is expanded, so long lists stay short.
 */
private const val COLLAPSED_FIELDS = 3

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataRowCard(
    title: String,
    fields: List<Pair<String, @Composable () -> Unit>>,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val collapsible = fields.size > COLLAPSED_FIELDS + 1
    val shown = if (!collapsible || expanded) fields else fields.take(COLLAPSED_FIELDS)
    Glass(modifier.fillMaxWidth().animateContentSize(androidx.compose.animation.core.spring(0.85f, 500f)), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                leading?.invoke()
                Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (actions != null) Row(horizontalArrangement = Arrangement.spacedBy(0.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
                if (collapsible) {
                    val rot by androidx.compose.animation.core.animateFloatAsState(if (expanded) 180f else 0f, androidx.compose.animation.core.spring(0.8f, 500f), label = "row-chevron")
                    Box(Modifier.size(32.dp).clip(CircleShape).clickable { expanded = !expanded }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.ExpandMore, null, Modifier.size(20.dp).graphicsLayer { rotationZ = rot }, tint = Tokens.MutedForeground)
                    }
                }
            }
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                shown.forEach { (label, value) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(label.uppercase(), fontSize = 9.sp, lineHeight = 11.sp, color = Tokens.MutedForeground, letterSpacing = 0.4.sp)
                        value()
                    }
                }
            }
        }
    }
}

@Composable
fun FieldText(text: String?, color: Color = Color.White) =
    Text(text?.takeIf { it.isNotBlank() } ?: "-", color = color, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    FlowRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
