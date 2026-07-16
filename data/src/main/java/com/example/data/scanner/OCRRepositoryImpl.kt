package com.example.data.scanner

import android.graphics.Bitmap
import com.example.domain.scanner.OCRRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class OCRRepositoryImpl : OCRRepository {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override suspend fun recognizeText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
