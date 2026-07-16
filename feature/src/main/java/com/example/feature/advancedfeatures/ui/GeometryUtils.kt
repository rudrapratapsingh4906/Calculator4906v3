package com.example.feature.advancedfeatures.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Magenta,
        Color.Cyan,
        Color.Yellow
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(120.dp)
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .background(color, CircleShape)
                            .clickable {
                                onColorSelected(color)
                                onDismiss()
                            }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun drawGeometryObjects(canvas: DrawScope, state: GraphState, toPxX: (Double) -> Float, toPxY: (Double) -> Float) {
    val pointsMap = state.geometryObjects.filterIsInstance<GeometryObject.Point>().associateBy { it.id }
    state.geometryObjects.forEach { obj ->
        if (obj.style.isHidden) return@forEach
        val color = if (state.selectedObjectId == obj.id) Color.Yellow else obj.style.color
        val stroke = obj.style.strokeWidth
        with(canvas) {
            when (obj) {
                is GeometryObject.Point -> drawCircle(color, 6f, androidx.compose.ui.geometry.Offset(toPxX(obj.x), toPxY(obj.y)))
                is GeometryObject.Line -> {
                    val p1 = pointsMap[obj.p1Id] ?: return@forEach
                    val p2 = pointsMap[obj.p2Id] ?: return@forEach
                    val x1 = toPxX(p1.x); val y1 = toPxY(p1.y)
                    val x2 = toPxX(p2.x); val y2 = toPxY(p2.y)
                    if (x1 == x2 && y1 == y2) return@forEach
                    val dx = x2 - x1; val dy = y2 - y1
                    drawLine(color, androidx.compose.ui.geometry.Offset(x1 - 2000f * dx, y1 - 2000f * dy), androidx.compose.ui.geometry.Offset(x1 + 2000f * dx, y1 + 2000f * dy), stroke)
                }
                is GeometryObject.Segment -> {
                    val p1 = pointsMap[obj.p1Id] ?: return@forEach
                    val p2 = pointsMap[obj.p2Id] ?: return@forEach
                    drawLine(color, androidx.compose.ui.geometry.Offset(toPxX(p1.x), toPxY(p1.y)), androidx.compose.ui.geometry.Offset(toPxX(p2.x), toPxY(p2.y)), stroke)
                }
                is GeometryObject.Ray -> {
                    val p1 = pointsMap[obj.p1Id] ?: return@forEach
                    val p2 = pointsMap[obj.p2Id] ?: return@forEach
                    val x1 = toPxX(p1.x); val y1 = toPxY(p1.y)
                    val x2 = toPxX(p2.x); val y2 = toPxY(p2.y)
                    val dx = x2 - x1; val dy = y2 - y1
                    drawLine(color, androidx.compose.ui.geometry.Offset(x1, y1), androidx.compose.ui.geometry.Offset(x1 + 2000f * dx, y1 + 2000f * dy), stroke)
                }
                is GeometryObject.Vector -> {
                    val p1 = pointsMap[obj.p1Id] ?: return@forEach
                    val p2 = pointsMap[obj.p2Id] ?: return@forEach
                    val start = androidx.compose.ui.geometry.Offset(toPxX(p1.x), toPxY(p1.y))
                    val end = androidx.compose.ui.geometry.Offset(toPxX(p2.x), toPxY(p2.y))
                    drawLine(color, start, end, stroke)
                    val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
                    val arrowLen = 15f; val arrowAngle = PI / 6
                    drawLine(color, end, androidx.compose.ui.geometry.Offset((end.x - arrowLen * cos(angle - arrowAngle)).toFloat(), (end.y - arrowLen * sin(angle - arrowAngle)).toFloat()), stroke)
                    drawLine(color, end, androidx.compose.ui.geometry.Offset((end.x - arrowLen * cos(angle + arrowAngle)).toFloat(), (end.y - arrowLen * sin(angle + arrowAngle)).toFloat()), stroke)
                }
                is GeometryObject.Circle -> {
                    val center = pointsMap[obj.centerId] ?: return@forEach
                    val radiusPx = if (obj.pointId != null) {
                        val p = pointsMap[obj.pointId] ?: return@forEach
                        sqrt((toPxX(p.x) - toPxX(center.x)).toDouble().pow(2.0) + (toPxY(p.y) - toPxY(center.y)).toDouble().pow(2.0)).toFloat()
                    } else {
                        ((obj.radius ?: 1.0) * (size.width / (state.viewport.maxX - state.viewport.minX))).toFloat()
                    }
                    drawCircle(color, radiusPx, androidx.compose.ui.geometry.Offset(toPxX(center.x), toPxY(center.y)), style = Stroke(stroke))
                }
                is GeometryObject.Polygon -> {
                    if (obj.pointIds.size < 2) return@forEach
                    val path = androidx.compose.ui.graphics.Path()
                    obj.pointIds.forEachIndexed { index, pId ->
                        val p = pointsMap[pId] ?: return@forEach
                        val px = toPxX(p.x)
                        val py = toPxY(p.y)
                        if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    path.close()
                    drawPath(path, color.copy(alpha = 0.2f), style = androidx.compose.ui.graphics.drawscope.Fill)
                    drawPath(path, color, style = Stroke(stroke))
                }
                else -> {}
            }
        }
    }
}
