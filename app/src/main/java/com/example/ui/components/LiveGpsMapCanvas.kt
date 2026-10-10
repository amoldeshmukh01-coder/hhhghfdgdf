package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CollectionPoint
import com.example.data.model.PointStatus
import com.example.data.model.Van
import kotlin.math.*

@Composable
fun LiveGpsMapCanvas(
    points: List<CollectionPoint>,
    activeVan: Van?,
    selectedPoint: CollectionPoint?,
    onPointSelected: (CollectionPoint?) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 44f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val textMeasurer = rememberTextMeasurer()

    // Bounding coordinates
    val (minLat, maxLat, minLng, maxLng) = remember(points, activeVan) {
        val allLats = points.map { it.latitude } + listOfNotNull(activeVan?.currentLat)
        val allLngs = points.map { it.longitude } + listOfNotNull(activeVan?.currentLng)
        if (allLats.isEmpty()) {
            listOf(18.51, 18.54, 73.84, 73.88)
        } else {
            val minLt = allLats.minOrNull() ?: 18.51
            val maxLt = allLats.maxOrNull() ?: 18.54
            val minLg = allLngs.minOrNull() ?: 73.84
            val maxLg = allLngs.maxOrNull() ?: 73.88
            val latPadding = (maxLt - minLt).coerceAtLeast(0.004) * 0.25
            val lngPadding = (maxLg - minLg).coerceAtLeast(0.004) * 0.25
            listOf(minLt - latPadding, maxLt + latPadding, minLg - lngPadding, maxLg + lngPadding)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F5E9)) // Light eco village terrain background
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 4.0f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("live_gps_map_canvas")
                .pointerInput(points, minLat, maxLat, minLng, maxLng, zoomScale, panOffsetX, panOffsetY) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height

                        // Find if tap is close to any point
                        val tappedPoint = points.find { pt ->
                            val ptOffset = latLngToCanvasOffset(
                                pt.latitude, pt.longitude, minLat, maxLat, minLng, maxLng,
                                w.toFloat(), h.toFloat(), zoomScale, panOffsetX, panOffsetY
                            )
                            val dist = hypot((tapOffset.x - ptOffset.x).toDouble(), (tapOffset.y - ptOffset.y).toDouble())
                            dist <= 40 * zoomScale
                        }
                        onPointSelected(tappedPoint)
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw Rural Field & Topographic Patterns
            drawRuralTerrainGrid(w, h, zoomScale, panOffsetX, panOffsetY)

            // 2. Draw Collection Route connecting points
            if (points.isNotEmpty()) {
                val routeOffsets = points.sortedBy { it.sequenceOrder }.map { pt ->
                    latLngToCanvasOffset(pt.latitude, pt.longitude, minLat, maxLat, minLng, maxLng, w, h, zoomScale, panOffsetX, panOffsetY)
                }

                // Road underlay
                val roadPath = Path()
                routeOffsets.forEachIndexed { idx, offset ->
                    if (idx == 0) roadPath.moveTo(offset.x, offset.y)
                    else roadPath.lineTo(offset.x, offset.y)
                }

                // Wide road casing
                drawPath(
                    path = roadPath,
                    color = Color(0xFFCFD8DC),
                    style = Stroke(width = 16f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Road core
                drawPath(
                    path = roadPath,
                    color = Color(0xFFECEFF1),
                    style = Stroke(width = 11f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Colored progress line between completed points
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val o1 = routeOffsets[i]
                    val o2 = routeOffsets[i + 1]
                    val isDone = p1.status == PointStatus.COMPLETED && p2.status == PointStatus.COMPLETED
                    val isNext = p1.status == PointStatus.COMPLETED && p2.status == PointStatus.IN_PROGRESS

                    val segmentColor = when {
                        isDone -> Color(0xFF10B981)
                        isNext -> Color(0xFFF59E0B)
                        else -> Color(0xFF94A3B8)
                    }

                    val pathEffect = if (!isDone) PathEffect.dashPathEffect(floatArrayOf(20f * zoomScale, 15f * zoomScale)) else null

                    drawLine(
                        color = segmentColor,
                        start = o1,
                        end = o2,
                        strokeWidth = 6f * zoomScale,
                        cap = StrokeCap.Round,
                        pathEffect = pathEffect
                    )
                }
            }

            // 3. Draw Collection Point Markers
            points.forEach { point ->
                val ptOffset = latLngToCanvasOffset(
                    point.latitude, point.longitude, minLat, maxLat, minLng, maxLng, w, h, zoomScale, panOffsetX, panOffsetY
                )
                drawCollectionPointMarker(point, ptOffset, zoomScale, point.id == selectedPoint?.id, textMeasurer)
            }

            // 4. Draw Active Van Marker
            activeVan?.let { van ->
                val vanOffset = latLngToCanvasOffset(
                    van.currentLat, van.currentLng, minLat, maxLat, minLng, maxLng, w, h, zoomScale, panOffsetX, panOffsetY
                )

                // Pulse Geofence
                drawCircle(
                    color = Color(0xFF047857).copy(alpha = pulseAlpha),
                    radius = pulseRadius * zoomScale,
                    center = vanOffset
                )

                // Accuracy circle
                drawCircle(
                    color = Color(0xFF047857).copy(alpha = 0.25f),
                    radius = 24f * zoomScale,
                    center = vanOffset
                )

                // Van Pin Base
                drawCircle(
                    color = Color.White,
                    radius = 16f * zoomScale,
                    center = vanOffset
                )
                drawCircle(
                    color = if (van.speedKmH > 1f) Color(0xFF047857) else Color(0xFFF59E0B),
                    radius = 13f * zoomScale,
                    center = vanOffset
                )

                // Heading Triangle / Direction Indicator
                rotate(degrees = van.headingDegrees, pivot = vanOffset) {
                    val arrowPath = Path().apply {
                        moveTo(vanOffset.x, vanOffset.y - (20f * zoomScale))
                        lineTo(vanOffset.x - (8f * zoomScale), vanOffset.y - (10f * zoomScale))
                        lineTo(vanOffset.x + (8f * zoomScale), vanOffset.y - (10f * zoomScale))
                        close()
                    }
                    drawPath(arrowPath, color = Color(0xFFD97706))
                }

                // Van Label
                val vanLabel = "${van.name} (${van.speedKmH.toInt()} km/h)"
                val textLayout = textMeasurer.measure(
                    text = vanLabel,
                    style = TextStyle(fontSize = (11f * zoomScale.coerceIn(0.8f, 1.4f)).sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                )
                val textW = textLayout.size.width.toFloat()
                val textH = textLayout.size.height.toFloat()
                val badgePadding = 6f
                val badgeLeft = vanOffset.x - textW / 2f - badgePadding
                val badgeTop = vanOffset.y + 18f * zoomScale
                val badgeW = textW + badgePadding * 2f
                val badgeH = textH + badgePadding

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.92f),
                    topLeft = Offset(badgeLeft, badgeTop),
                    size = androidx.compose.ui.geometry.Size(badgeW, badgeH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = vanLabel,
                    topLeft = Offset(badgeLeft + badgePadding, badgeTop + badgePadding / 2f),
                    style = TextStyle(fontSize = (11f * zoomScale.coerceIn(0.8f, 1.4f)).sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                )
            }
        }

        // Overlay Controls (Zoom In, Zoom Out, Reset Center)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(4.0f) },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("btn_zoom_in"),
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }

            FloatingActionButton(
                onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.6f) },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("btn_zoom_out"),
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FloatingActionButton(
                onClick = {
                    zoomScale = 1.0f
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("btn_center_map"),
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center Map")
            }
        }

        // Legend Badge at bottom left
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapLegendItem(color = Color(0xFF10B981), label = "Cleaned")
                MapLegendItem(color = Color(0xFFF59E0B), label = "Next Target")
                MapLegendItem(color = Color(0xFF3B82F6), label = "Pending")
                MapLegendItem(color = Color(0xFFEF4444), label = "Missed")
            }
        }
    }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
        )
    }
}

private fun DrawScope.drawRuralTerrainGrid(w: Float, h: Float, zoom: Float, panX: Float, panY: Float) {
    // Village contour curves & agricultural plots
    val gridSize = 70f * zoom
    val startX = (panX % gridSize) - gridSize
    val startY = (panY % gridSize) - gridSize

    var x = startX
    while (x < w + gridSize) {
        drawLine(
            color = Color(0xFFC8E6C9).copy(alpha = 0.5f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += gridSize
    }

    var y = startY
    while (y < h + gridSize) {
        drawLine(
            color = Color(0xFFC8E6C9).copy(alpha = 0.5f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += gridSize
    }

    // River / canal curve through rural area
    val canalPath = Path().apply {
        moveTo(0f, h * 0.85f + panY * 0.2f)
        cubicTo(
            w * 0.35f, h * 0.9f + panY * 0.2f,
            w * 0.65f, h * 0.7f + panY * 0.2f,
            w, h * 0.78f + panY * 0.2f
        )
    }
    drawPath(
        path = canalPath,
        color = Color(0xFF81D4FA).copy(alpha = 0.6f),
        style = Stroke(width = 14f * zoom, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawCollectionPointMarker(
    point: CollectionPoint,
    offset: Offset,
    zoom: Float,
    isSelected: Boolean,
    textMeasurer: TextMeasurer
) {
    val markerRadius = if (isSelected) 17f * zoom else 13f * zoom

    val markerColor = when (point.status) {
        PointStatus.COMPLETED -> Color(0xFF10B981)
        PointStatus.IN_PROGRESS -> Color(0xFFF59E0B)
        PointStatus.MISSED -> Color(0xFFEF4444)
        PointStatus.PENDING -> Color(0xFF3B82F6)
    }

    // Halo if selected
    if (isSelected) {
        drawCircle(
            color = markerColor.copy(alpha = 0.35f),
            radius = markerRadius + 10f * zoom,
            center = offset
        )
    }

    // Outer border
    drawCircle(
        color = Color.White,
        radius = markerRadius + 3f * zoom,
        center = offset
    )

    // Main marker circle
    drawCircle(
        color = markerColor,
        radius = markerRadius,
        center = offset
    )

    // Sequence number inside circle
    val seqText = "${point.sequenceOrder}"
    val textLayout = textMeasurer.measure(
        text = seqText,
        style = TextStyle(fontSize = (11f * zoom.coerceIn(0.8f, 1.3f)).sp, fontWeight = FontWeight.Bold, color = Color.White)
    )
    val seqW = textLayout.size.width.toFloat()
    val seqH = textLayout.size.height.toFloat()
    drawText(
        textMeasurer = textMeasurer,
        text = seqText,
        topLeft = Offset(offset.x - seqW / 2f, offset.y - seqH / 2f),
        style = TextStyle(fontSize = (11f * zoom.coerceIn(0.8f, 1.3f)).sp, fontWeight = FontWeight.Bold, color = Color.White)
    )

    // Short label beneath marker
    val shortName = point.code
    val labelLayout = textMeasurer.measure(
        text = shortName,
        style = TextStyle(fontSize = (9f * zoom.coerceIn(0.8f, 1.2f)).sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
    )
    val labelW = labelLayout.size.width.toFloat()
    val labelH = labelLayout.size.height.toFloat()
    val labelTop = offset.y + markerRadius + 3f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(offset.x - labelW / 2f - 3f, labelTop),
        size = androidx.compose.ui.geometry.Size(labelW + 6f, labelH + 2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = shortName,
        topLeft = Offset(offset.x - labelW / 2f, labelTop + 1f),
        style = TextStyle(fontSize = (9f * zoom.coerceIn(0.8f, 1.2f)).sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
    )
}

private fun latLngToCanvasOffset(
    lat: Double,
    lng: Double,
    minLat: Double,
    maxLat: Double,
    minLng: Double,
    maxLng: Double,
    width: Float,
    height: Float,
    zoom: Float,
    panX: Float,
    panY: Float
): Offset {
    val normX = ((lng - minLng) / (maxLng - minLng).coerceAtLeast(0.0001)).toFloat()
    val normY = 1.0f - ((lat - minLat) / (maxLat - minLat).coerceAtLeast(0.0001)).toFloat()

    val centerX = width / 2f
    val centerY = height / 2f

    val rawX = width * 0.1f + normX * (width * 0.8f)
    val rawY = height * 0.1f + normY * (height * 0.8f)

    val scaledX = centerX + (rawX - centerX) * zoom + panX
    val scaledY = centerY + (rawY - centerY) * zoom + panY

    return Offset(scaledX, scaledY)
}
