package com.example.feature.advancedfeatures.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import kotlin.math.*

data class Point3D(val x: Double, val y: Double, val z: Double)

fun DrawScope.draw3DGraph(state: GraphState, viewModel: GraphPlotterViewModel, W: Float, H: Float) {
    val rotX = state.cameraRotationX
    val rotZ = state.cameraRotationZ
    val zoom = state.cameraZoom
    
    // 1. Draw Axes
    val axisLen = 5.0
    val origin = project3D(0.0, 0.0, 0.0, rotX, rotZ, zoom, W, H)
    val xAxis = project3D(axisLen, 0.0, 0.0, rotX, rotZ, zoom, W, H)
    val yAxis = project3D(0.0, axisLen, 0.0, rotX, rotZ, zoom, W, H)
    val zAxis = project3D(0.0, 0.0, axisLen, rotX, rotZ, zoom, W, H)
    
    drawLine(Color.Red.copy(alpha = 0.5f), origin, xAxis, 2f)
    drawLine(Color.Green.copy(alpha = 0.5f), origin, yAxis, 2f)
    drawLine(Color.Blue.copy(alpha = 0.5f), origin, zAxis, 2f)
    
    // 2. Mesh generation
    val res = 25
    val minX = -5.0
    val maxX = 5.0
    val minY = -5.0
    val maxY = 5.0
    val stepX = (maxX - minX) / res
    val stepY = (maxY - minY) / res
    
    val mesh = Array(res + 1) { i ->
        Array(res + 1) { j ->
            val x = minX + i * stepX
            val y = minY + j * stepY
            val z = viewModel.evaluateExpression(state.zExpr, x, y) ?: 0.0
            Point3D(x, y, z)
        }
    }
    
    val primaryColor = Color(0xFF6200EE)
    val gridColor = Color(0x33FFFFFF)
    
    // 3. Draw Mesh
    for (i in 0 until res) {
        for (j in 0 until res) {
            val p1 = mesh[i][j]
            val p2 = mesh[i+1][j]
            val p3 = mesh[i+1][j+1]
            val p4 = mesh[i][j+1]
            
            val proj1 = project3D(p1.x, p1.y, p1.z, rotX, rotZ, zoom, W, H)
            val proj2 = project3D(p2.x, p2.y, p2.z, rotX, rotZ, zoom, W, H)
            val proj3 = project3D(p3.x, p3.y, p3.z, rotX, rotZ, zoom, W, H)
            val proj4 = project3D(p4.x, p4.y, p4.z, rotX, rotZ, zoom, W, H)
            
            if (state.renderMode == RenderMode.Solid || state.renderMode == RenderMode.Mesh) {
                val path = Path().apply {
                    moveTo(proj1.x, proj1.y)
                    lineTo(proj2.x, proj2.y)
                    lineTo(proj3.x, proj3.y)
                    lineTo(proj4.x, proj4.y)
                    close()
                }
                
                val avgZ = (p1.z + p2.z + p3.z + p4.z) / 4.0
                val color = if (state.surfaceColorGradient) {
                    val t = ((avgZ + 2.0) / 4.0).coerceIn(0.0, 1.0).toFloat()
                    Color(
                        red = (0.2f + 0.6f * t).coerceIn(0f, 1f),
                        green = 0.4f,
                        blue = (0.8f - 0.6f * t).coerceIn(0f, 1f),
                        alpha = 0.7f
                    )
                } else {
                    primaryColor.copy(alpha = 0.7f)
                }
                
                drawPath(path, color)
            }

            if (state.renderMode == RenderMode.Wireframe || state.renderMode == RenderMode.Mesh) {
                drawLine(gridColor, proj1, proj2, 1f)
                drawLine(gridColor, proj1, proj4, 1f)
            }
        }
    }
}

private fun project3D(x: Double, y: Double, z: Double, rotX: Float, rotZ: Float, zoom: Float, W: Float, H: Float): Offset {
    val radX = Math.toRadians(rotX.toDouble())
    val radZ = Math.toRadians(rotZ.toDouble())
    
    // Rotate around Z (Yaw)
    val x1 = x * cos(radZ) - y * sin(radZ)
    val y1 = x * sin(radZ) + y * cos(radZ)
    
    // Rotate around X (Pitch)
    val y2 = y1 * cos(radX) - z * sin(radX)
    val z2 = y1 * sin(radX) + z * cos(radX)
    
    val scale = (min(W, H) / 12f) * zoom
    return Offset(
        W / 2f + (x1 * scale).toFloat(),
        H / 2f - (y2 * scale).toFloat()
    )
}
