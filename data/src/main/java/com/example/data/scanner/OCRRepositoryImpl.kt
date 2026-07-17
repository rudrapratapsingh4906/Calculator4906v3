package com.example.data.scanner

import android.graphics.Bitmap
import com.example.domain.scanner.OCRRepository
import com.example.domain.scanner.PreprocessFilterMode
import com.example.domain.scanner.MathOcrCleanup
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OCRRepositoryImpl @Inject constructor(
    private val preprocessor: ImagePreprocessor
) : OCRRepository {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override suspend fun recognizeText(
        bitmap: Bitmap,
        mode: PreprocessFilterMode
    ): String = withContext(Dispatchers.Default) {
        if (bitmap.isRecycled) {
            return@withContext "Error: Image source is recycled"
        }
        val processedBitmap = try {
            preprocessor.preprocess(bitmap, mode)
        } catch (e: Throwable) {
            return@withContext "Error: Preprocessing failed (${e.localizedMessage})"
        }
        
        val image = try {
            InputImage.fromBitmap(processedBitmap, 0)
        } catch (e: Throwable) {
            if (processedBitmap != bitmap) {
                processedBitmap.recycle()
            }
            return@withContext "Error: Invalid image format"
        }

        try {
            val result = recognizer.process(image).await()
            if (result.text.isNullOrBlank()) {
                "Error: No text recognized. Try adjust angle or contrast filter."
            } else {
                MathOcrCleanup.cleanup(result.text)
            }
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "OCR recognition failed"}"
        } finally {
            if (processedBitmap != bitmap) {
                processedBitmap.recycle()
            }
        }
    }
}
