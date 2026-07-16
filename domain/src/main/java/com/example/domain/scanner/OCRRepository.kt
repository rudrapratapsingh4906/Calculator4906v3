package com.example.domain.scanner

import android.graphics.Bitmap

interface OCRRepository {
    suspend fun recognizeText(bitmap: Bitmap): String
}