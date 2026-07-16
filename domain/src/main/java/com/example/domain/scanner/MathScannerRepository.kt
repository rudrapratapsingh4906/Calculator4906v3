package com.example.domain.scanner

import android.graphics.Bitmap

interface MathScannerRepository {
    suspend fun saveImage(bitmap: Bitmap): String
}
