package com.example.data.scanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Color
import javax.inject.Inject
import com.example.domain.scanner.PreprocessFilterMode

class ImagePreprocessor @Inject constructor() {

    fun preprocess(bitmap: Bitmap, mode: PreprocessFilterMode = PreprocessFilterMode.ENHANCED_GRAY): Bitmap {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return bitmap
        
        return when (mode) {
            PreprocessFilterMode.ORIGINAL -> bitmap
            PreprocessFilterMode.ENHANCED_GRAY -> {
                val gray = toGrayscale(bitmap)
                if (gray == bitmap) {
                    bitmap
                } else {
                    val enhanced = applyContrast(gray, 1.3f)
                    if (enhanced != gray) {
                        gray.recycle()
                    }
                    enhanced
                }
            }
            PreprocessFilterMode.HIGH_CONTRAST_BW -> {
                val gray = toGrayscale(bitmap)
                if (gray == bitmap) {
                    bitmap
                } else {
                    val binarized = toBinarized(gray)
                    if (binarized != gray) {
                        gray.recycle()
                    }
                    binarized
                }
            }
        }
    }

    fun cropToCenterRegion(bitmap: Bitmap, widthPercentage: Float = 0.85f, heightPercentage: Float = 0.25f): Bitmap {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return bitmap
        
        val width = bitmap.width
        val height = bitmap.height
        
        val minWidth = 100.coerceAtMost(width)
        val minHeight = 50.coerceAtMost(height)
        
        val cropWidth = (width * widthPercentage).toInt().coerceIn(minWidth, width)
        val cropHeight = (height * heightPercentage).toInt().coerceIn(minHeight, height)
        
        val startX = ((width - cropWidth) / 2).coerceIn(0, (width - cropWidth).coerceAtLeast(0))
        val startY = ((height - cropHeight) / 2).coerceIn(0, (height - cropHeight).coerceAtLeast(0))
        
        return try {
            Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight)
        } catch (e: Throwable) {
            bitmap
        }
    }

    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return bitmap
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = try {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } catch (e: Throwable) {
            return bitmap
        }
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        try {
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        } catch (e: Throwable) {
            return bitmap
        }
        return grayscaleBitmap
    }

    private fun applyContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return bitmap
        val width = bitmap.width
        val height = bitmap.height
        val contrastBitmap = try {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } catch (e: Throwable) {
            return bitmap
        }
        val canvas = Canvas(contrastBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        
        // Adjust contrast
        val scale = contrast
        val translate = (-.5f * scale + .5f) * 255f
        colorMatrix.set(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        try {
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        } catch (e: Throwable) {
            return bitmap
        }
        return contrastBitmap
    }

    private fun toBinarized(bitmap: Bitmap): Bitmap {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return bitmap
        val width = bitmap.width
        val height = bitmap.height
        val binarized = try {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } catch (e: Throwable) {
            return bitmap
        }
        
        val pixels = try {
            IntArray(width * height)
        } catch (e: Throwable) {
            return binarized
        }
        
        try {
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        } catch (e: Throwable) {
            return binarized
        }
        
        if (pixels.isEmpty()) return binarized
        
        // Calculate average intensity for adaptive thresholding
        var sum = 0L
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val gray = (r * 299 + g * 587 + b * 114) / 1000
            sum += gray
        }
        val threshold = (sum / pixels.size).toInt()
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val gray = (r * 299 + g * 587 + b * 114) / 1000
            
            val newColor = if (gray > threshold) Color.WHITE else Color.BLACK
            pixels[i] = newColor
        }
        
        try {
            binarized.setPixels(pixels, 0, width, 0, 0, width, height)
        } catch (e: Throwable) {
            // return partial binarized
        }
        return binarized
    }
}
