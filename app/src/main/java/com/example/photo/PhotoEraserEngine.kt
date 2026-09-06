package com.example.photo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object PhotoEraserEngine {

    /**
     * Erases masked region from sourceBitmap by intelligently inpainting from surrounding pixels.
     */
    suspend fun inpaintErase(
        sourceBitmap: Bitmap,
        maskPaths: List<Pair<Path, Float>>,
        rectSelections: List<RectF>
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        // 1. Create a binary mask bitmap
        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val maskCanvas = Canvas(maskBitmap)
        maskCanvas.drawColor(Color.TRANSPARENT)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.WHITE
        }

        for ((path, strokeWidth) in maskPaths) {
            strokePaint.strokeWidth = strokeWidth
            maskCanvas.drawPath(path, strokePaint)
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        for (rect in rectSelections) {
            maskCanvas.drawRect(rect, fillPaint)
        }

        // 2. Read pixels
        val pixels = IntArray(width * height)
        sourceBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val maskPixels = ByteArray(width * height)
        val maskBuffer = IntArray(width * height)
        maskBitmap.getPixels(maskBuffer, 0, width, 0, 0, width, height)
        for (i in maskBuffer.indices) {
            // If alpha > 30, it's masked
            maskPixels[i] = if ((maskBuffer[i] ushr 24) > 30) 1 else 0
        }

        val outputPixels = pixels.clone()
        val isMasked = BooleanArray(width * height) { maskPixels[it].toInt() == 1 }

        // Find bounding box of masked pixels to limit computation
        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0
        var hasMasked = false

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (isMasked[y * width + x]) {
                    hasMasked = true
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        if (!hasMasked) {
            return@withContext sourceBitmap.copy(sourceBitmap.config ?: Bitmap.Config.ARGB_8888, true)
        }

        val searchRadius = 18
        val radiusSq = searchRadius * searchRadius

        // Multi-pass Telea-inspired fast inpainting
        val passes = 2
        for (pass in 0 until passes) {
            for (y in max(0, minY - 1)..min(height - 1, maxY + 1)) {
                for (x in max(0, minX - 1)..min(width - 1, maxX + 1)) {
                    val idx = y * width + x
                    if (!isMasked[idx]) continue

                    var weightSum = 0f
                    var rSum = 0f
                    var gSum = 0f
                    var bSum = 0f
                    var validNeighbors = 0

                    val dyStart = max(-searchRadius, -y)
                    val dyEnd = min(searchRadius, height - 1 - y)
                    val dxStart = max(-searchRadius, -x)
                    val dxEnd = min(searchRadius, width - 1 - x)

                    var dy = dyStart
                    while (dy <= dyEnd) {
                        var dx = dxStart
                        while (dx <= dxEnd) {
                            val distSq = dx * dx + dy * dy
                            if (distSq in 1..radiusSq) {
                                val ny = y + dy
                                val nx = x + dx
                                val nIdx = ny * width + nx

                                if (!isMasked[nIdx] || pass > 0) {
                                    val dist = sqrt(distSq.toFloat())
                                    val weight = 1.0f / (dist * dist + 0.1f)

                                    val color = outputPixels[nIdx]
                                    val r = (color shr 16) and 0xFF
                                    val g = (color shr 8) and 0xFF
                                    val b = color and 0xFF

                                    rSum += r * weight
                                    gSum += g * weight
                                    bSum += b * weight
                                    weightSum += weight
                                    validNeighbors++
                                }
                            }
                            dx += 2 // step by 2 for high performance
                        }
                        dy += 2
                    }

                    if (validNeighbors > 0 && weightSum > 0f) {
                        val finalR = (rSum / weightSum).toInt().coerceIn(0, 255)
                        val finalG = (gSum / weightSum).toInt().coerceIn(0, 255)
                        val finalB = (bSum / weightSum).toInt().coerceIn(0, 255)
                        outputPixels[idx] = (0xFF shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
                    }
                }
            }
        }

        // Apply 3x3 gaussian smoothing over infilled region for realistic edge blending
        val finalOutput = outputPixels.clone()
        for (y in max(1, minY)..min(height - 2, maxY)) {
            for (x in max(1, minX)..min(width - 2, maxX)) {
                val idx = y * width + x
                if (isMasked[idx]) {
                    var r = 0
                    var g = 0
                    var b = 0
                    for (ky in -1..1) {
                        for (kx in -1..1) {
                            val c = outputPixels[(y + ky) * width + (x + kx)]
                            r += (c shr 16) and 0xFF
                            g += (c shr 8) and 0xFF
                            b += c and 0xFF
                        }
                    }
                    finalOutput[idx] = (0xFF shl 24) or ((r / 9) shl 16) or ((g / 9) shl 8) or (b / 9)
                }
            }
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(finalOutput, 0, width, 0, 0, width, height)
        resultBitmap
    }
}
