package com.growthaxys.churchmanagements.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.growthaxys.churchmanagements.core.t

/** Which `loading.tsx` of the web a page mirrors. */
enum class SkeletonKind { Dashboard, Table, Cards, Stats, Tabs }

/** `Skeleton`: rounded-md, bg white/7, shimmer. Decorative. */
@Composable
fun Skeleton(modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(6.dp)) {
    Box(modifier.clip(shape).background(Color(0x12FFFFFF)).shimmer())
}

@Composable private fun SkCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) = Glass(modifier.fillMaxWidth()) { content() }

@Composable private fun SkHeading() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Skeleton(Modifier.height(24.dp).width(176.dp))
        Skeleton(Modifier.height(16.dp).width(224.dp))
    }
}

@Composable private fun SkToolbar(vararg buttons: Dp) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Skeleton(Modifier.weight(1f).height(40.dp), Tokens.ControlShape)
        buttons.forEach { Skeleton(Modifier.width(it).height(40.dp), Tokens.ControlShape) }
    }
}

@Composable private fun SkTabs(count: Int) {
    Glass(Modifier.fillMaxWidth(), shape = Tokens.ControlShape) {
        Row(Modifier.padding(4.dp)) { repeat(count) { Box(Modifier.height(44.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) { Skeleton(Modifier.height(20.dp).width(72.dp)) } } }
    }
}

@Composable private fun SkTableCard() {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x0DFFFFFF)).padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Skeleton(Modifier.height(16.dp).fillMaxWidth(0.5f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) { Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Skeleton(Modifier.height(8.dp).width(40.dp)); Skeleton(Modifier.height(16.dp).fillMaxWidth(0.8f)) } }
        }
        Skeleton(Modifier.height(8.dp).width(40.dp)); Skeleton(Modifier.height(16.dp).fillMaxWidth(0.6f))
    }
}

@Composable private fun SkEntityCard() = SkCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Skeleton(Modifier.height(16.dp).width(144.dp)); Skeleton(Modifier.height(22.dp).width(64.dp), CircleShape) }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(2) { Skeleton(Modifier.size(40.dp), RoundedCornerShape(6.dp)) } }
        }
        listOf(0.83f, 0.66f, 0.83f, 0.66f).forEach { Skeleton(Modifier.height(14.dp).fillMaxWidth(it)) }
    }
}

@Composable private fun SkStat(modifier: Modifier = Modifier) = SkCard(modifier) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Skeleton(Modifier.height(12.dp).width(96.dp)); Skeleton(Modifier.height(22.dp).width(112.dp)) }
}

@Composable private fun SkPanel(body: Dp) = SkCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Skeleton(Modifier.height(16.dp).width(160.dp)); Skeleton(Modifier.fillMaxWidth().height(body), RoundedCornerShape(8.dp)) }
}

/** Page skeletons mirroring the web `loading.tsx` files (mobile layout). */
@Composable
fun PageSkeleton(kind: SkeletonKind, padding: PaddingValues) {
    val label = t("loading")
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState(), enabled = false).padding(padding).semantics { contentDescription = label },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkHeading()
        when (kind) {
            SkeletonKind.Dashboard -> {
                repeat(2) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) {
                            SkCard(Modifier.weight(1f)) {
                                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Skeleton(Modifier.size(28.dp), RoundedCornerShape(8.dp)); Skeleton(Modifier.height(16.dp).width(48.dp)); Skeleton(Modifier.height(10.dp).width(56.dp))
                                }
                            }
                        }
                    }
                }
                SkPanel(150.dp); SkPanel(150.dp); SkPanel(200.dp)
            }
            SkeletonKind.Table -> { SkToolbar(88.dp, 112.dp); repeat(6) { SkTableCard() } }
            SkeletonKind.Cards -> { SkToolbar(112.dp); repeat(4) { SkEntityCard() } }
            SkeletonKind.Stats -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SkStat(Modifier.weight(1f)); SkStat(Modifier.weight(1f)) }
                SkToolbar(88.dp, 112.dp); repeat(4) { SkTableCard() }
            }
            SkeletonKind.Tabs -> { SkTabs(3); SkToolbar(112.dp); SkPanel(220.dp); repeat(2) { SkTableCard() } }
        }
    }
}
