package com.example.domain.scanner

import android.graphics.Bitmap

interface ImageProcessor {
    fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap
}
