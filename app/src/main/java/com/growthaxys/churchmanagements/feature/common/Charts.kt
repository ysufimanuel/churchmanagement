package com.growthaxys.churchmanagements.feature.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthaxys.churchmanagements.core.t
import com.growthaxys.churchmanagements.designsystem.Tokens
import kotlin.math.max

/** Lightweight Canvas charts replacing web Recharts (same palette: #00E5FF #3B82F6 #10B981 #F59E0B #8B5CF6 #EF4444). */

@Composable
fun BarChartView(data: List<Pair<String, Int>>, modifier: Modifier = Modifier, height: Dp = 180.dp) {
    if (data.isEmpty() || data.all { it.second == 0 }) { EmptyChart(height); return }
    val maxV = max(1, data.maxOf { it.second })
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val barW = size.width / data.size
            data.forEachIndexed { i, (_, v) ->
                val h = size.height * v / maxV
                drawRoundRect(
                    Brush.verticalGradient(listOf(Tokens.Primary, Tokens.Primary.copy(alpha = 0.35f))),
                    topLeft = Offset(i * barW + barW * 0.2f, size.height - h),
                    size = Size(barW * 0.6f, h), cornerRadius = CornerRadius(6.dp.toPx()),
                )
            }
            drawLine(Color(0x1AFFFFFF), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
        }
        Row(Modifier.fillMaxWidth()) {
            data.forEach { Text(it.first, Modifier.weight(1f), fontSize = 10.sp, color = Tokens.ChartAxis, maxLines = 1) }
        }
    }
}

/** Multi-series line chart. series = name -> (values aligned to labels) with color. */
@Composable
fun LineChartView(labels: List<String>, series: List<Triple<String, List<Double>, Color>>, modifier: Modifier = Modifier, height: Dp = 180.dp, area: Boolean = false) {
    if (labels.isEmpty() || series.all { s -> s.second.all { it == 0.0 } }) { EmptyChart(height); return }
    val maxV = series.maxOf { s -> s.second.maxOrNull() ?: 0.0 }.coerceAtLeast(1.0)
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val stepX = if (labels.size > 1) size.width / (labels.size - 1) else size.width
            series.forEach { (_, values, color) ->
                val path = Path()
                values.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - (size.height * (v / maxV)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                if (area) {
                    val fill = Path().apply { addPath(path); lineTo((values.size - 1) * stepX, size.height); lineTo(0f, size.height); close() }
                    drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent)))
                }
                drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
            }
            drawLine(Color(0x1AFFFFFF), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
        }
        Row(Modifier.fillMaxWidth()) { labels.forEach { Text(it, Modifier.weight(1f), fontSize = 10.sp, color = Tokens.ChartAxis, maxLines = 1) } }
        if (series.size > 1) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            series.forEach { (n, _, c) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(8.dp)) { drawCircle(c) }
                    Text(" $n", fontSize = 11.sp, color = Tokens.ChartAxis)
                }
            }
        }
    }
}

/** Donut (innerRadius ~62%) or full pie when donut=false. */
@Composable
fun DonutChartView(data: List<Pair<String, Double>>, modifier: Modifier = Modifier, height: Dp = 180.dp, centerLabel: String? = null, donut: Boolean = true) {
    val total = data.sumOf { it.second }
    if (data.isEmpty() || total <= 0.0) { EmptyChart(height); return }
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(height)) {
                var start = -90f
                val stroke = if (donut) size.minDimension * 0.19f else size.minDimension / 2
                data.forEachIndexed { i, (_, v) ->
                    val sweep = (v / total * 360).toFloat()
                    drawArc(
                        Tokens.ChartColors[i % Tokens.ChartColors.size], start, sweep, useCenter = !donut,
                        topLeft = if (donut) Offset(stroke / 2, stroke / 2) else Offset.Zero,
                        size = if (donut) Size(size.width - stroke, size.height - stroke) else size,
                        style = if (donut) Stroke(stroke) else androidx.compose.ui.graphics.drawscope.Fill,
                    )
                    start += sweep
                }
            }
            if (donut && centerLabel != null) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(total.toInt().toString(), fontSize = 22.sp, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(centerLabel, fontSize = 11.sp, color = Tokens.ChartAxis)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
            data.forEachIndexed { i, (n, _) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(8.dp)) { drawCircle(Tokens.ChartColors[i % Tokens.ChartColors.size]) }
                    Text(" $n", fontSize = 11.sp, color = Tokens.ChartAxis)
                }
            }
        }
    }
}

@Composable
private fun EmptyChart(height: Dp) = Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
    Text(t("none"), color = Tokens.MutedForeground, fontSize = 14.sp)
}
