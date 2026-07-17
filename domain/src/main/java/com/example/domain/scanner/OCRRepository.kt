package com.example.domain.scanner

import android.graphics.Bitmap

interface OCRRepository {
    suspend fun recognizeText(
        bitmap: Bitmap,
        mode: PreprocessFilterMode = PreprocessFilterMode.ENHANCED_GRAY
    ): String
}