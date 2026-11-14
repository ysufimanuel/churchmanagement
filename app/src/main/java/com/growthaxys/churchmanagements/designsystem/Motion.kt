package com.growthaxys.churchmanagements.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Motion tokens mirrored from the web (`tailwind.config.ts` + `globals.css`):
 * ease-out-strong cubic-bezier(.23,1,.32,1); press scale .96 / 160ms; skeleton shimmer 1.5s.
 */
val EaseOutStrong = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
val EaseBackOut = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/** `.btn-fx:active { scale: .96 }` */
@Composable
fun Modifier.pressScale(source: InteractionSource, pressedScale: Float = 0.96f): Modifier {
    val pressed by source.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pressed) pressedScale else 1f, tween(160, easing = EaseOutStrong), label = "press")
    return this.graphicsLayer { scaleX = sc; scaleY = sc }
}

/** `.skeleton::after` — light band sweeping left→right every 1.5s (draw-phase only). */
@Composable
fun Modifier.shimmer(): Modifier {
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Restart), label = "x")
    return this.drawWithContent {
        drawContent()
        val w = size.width
        val start = -w + x * 2f * w
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.09f), Color.Transparent), startX = start, endX = start + w))
    }
}

/** GSAP-like entrance: fade + translateY (+ optional scale/rotate), started after [delayMs]. */
@Composable
fun Modifier.enterAnim(
    delayMs: Int = 0,
    dy: Dp = 14.dp,
    dx: Dp = 0.dp,
    durationMs: Int = 400,
    fromScale: Float = 1f,
    fromRotate: Float = 0f,
    easing: androidx.compose.animation.core.Easing = EaseOutStrong,
): Modifier {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(durationMs, delayMs, easing)) }
    val dyPx = with(LocalDensity.current) { dy.toPx() }
    val dxPx = with(LocalDensity.current) { dx.toPx() }
    return this.graphicsLayer {
        val v = a.value
        alpha = (v * 1.6f).coerceIn(0f, 1f) // reaches full opacity before the move settles (autoAlpha feel)
        translationX = (1f - v) * dxPx
        translationY = (1f - v) * dyPx
        val s = fromScale + (1f - fromScale) * v
        scaleX = s; scaleY = s
        rotationZ = fromRotate * (1f - v)
    }
}

/**
 * Per-icon motion of `.btn-fx svg[class*="lucide-*"]`, driven by press (touch has no hover):
 * default grow 10%; plus ↻180°, x ↻90°, pencil tilt, search tilt+grow, send fly, refresh spin,
 * check pop, arrows nudge; bell rings, trash wiggles, download drops, eye blinks.
 */
@Composable
fun Modifier.iconFx(icon: ImageVector?, pressed: Boolean): Modifier {
    val n = icon?.name.orEmpty()
    val fx by animateFloatAsState(if (pressed) 1f else 0f, tween(260, easing = EaseOutStrong), label = "iconfx")
    val kf = remember { Animatable(0f) }
    val keyframed = n.endsWith("Notifications") || n.endsWith("Delete") || n.endsWith("Download") || n.endsWith("Visibility")
    LaunchedEffect(pressed) {
        if (pressed && keyframed) { kf.snapTo(0f); kf.animateTo(1f, tween(500)) }
    }
    val dp3 = with(LocalDensity.current) { 3.dp.toPx() }
    return this.graphicsLayer {
        var scale = 1f + fx * 0.1f
        var rot = 0f
        var tx = 0f
        var ty = 0f
        val k = kf.value
        val decay = (1f - k)
        when {
            n.endsWith("Add") -> rot = fx * 180f
            n.endsWith("Close") -> { rot = fx * 90f; scale = 1f + fx * 0.15f }
            n.endsWith("Edit") -> { rot = fx * -18f; tx = fx * 1f; ty = -fx * 1f }
            n.endsWith("Search") -> { rot = fx * -12f; scale = 1f + fx * 0.18f }
            n.endsWith("Send") -> { tx = fx * dp3; ty = -fx * dp3 }
            n.endsWith("Refresh") -> { rot = fx * 180f; scale = 1f }
            n.endsWith("Check") -> scale = 1f + fx * 0.25f
            n.endsWith("ArrowBack") -> tx = -fx * dp3
            n.endsWith("Print") -> ty = -fx * dp3 * 0.66f
            n.endsWith("Notifications") -> { rot = (sin(k * PI.toFloat() * 5f) * 16f * decay); transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f) }
            n.endsWith("Delete") -> rot = (sin(k * PI.toFloat() * 4f) * 12f * decay)
            n.endsWith("Download") -> ty = sin(k * PI.toFloat()) * dp3
            n.endsWith("Visibility") -> { scaleY = 1f - 0.85f * sin(k * PI.toFloat()); scaleX = scale; return@graphicsLayer }
        }
        scaleX = scale; scaleY = scale
        rotationZ = rot
        translationX = tx; translationY = ty
    }
}
