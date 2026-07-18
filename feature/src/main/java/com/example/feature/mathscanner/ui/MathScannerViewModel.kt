package com.example.feature.mathscanner.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.scanner.ImagePreprocessor
import com.example.data.scanner.OCRRepositoryImpl
import com.example.domain.scanner.PreprocessFilterMode
import com.example.domain.scanner.OCRRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MathScannerState(
    val isLoading: Boolean = false,
    val result: String? = null,
    val error: String? = null,
    val capturedImage: Bitmap? = null,
    val processedImage: Bitmap? = null,
    val isCaptured: Boolean = false,
    val filterMode: PreprocessFilterMode = PreprocessFilterMode.ENHANCED_GRAY,
    val useCrop: Boolean = true
)

class MathScannerViewModel(
    private val ocrRepository: OCRRepository = OCRRepositoryImpl(ImagePreprocessor())
) : ViewModel() {
    private val _state = MutableStateFlow(MathScannerState())
    val state: StateFlow<MathScannerState> = _state.asStateFlow()
    private val preprocessor = ImagePreprocessor()

    fun onCaptureImage(bitmap: Bitmap) {
        if (bitmap.isRecycled) {
            _state.update { 
                it.copy(
                    error = "Captured image is invalid or recycled",
                    isLoading = false
                )
            }
            return
        }

        val filterMode = _state.value.filterMode
        val useCrop = _state.value.useCrop

        _state.update { 
            it.copy(
                capturedImage = bitmap, 
                isLoading = true, 
                error = null, 
                result = null,
                isCaptured = true
            ) 
        }
        viewModelScope.launch {
            var croppedBitmap: Bitmap? = null
            try {
                withContext(Dispatchers.Default) {
                    croppedBitmap = if (useCrop) {
                        preprocessor.cropToCenterRegion(bitmap)
                    } else {
                        bitmap
                    }

                    val recognizedText = ocrRepository.recognizeText(croppedBitmap!!, filterMode)
                    val processedPreview = preprocessor.preprocess(croppedBitmap!!, filterMode)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            result = recognizedText,
                            processedImage = processedPreview
                        )
                    }
                }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Processing failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            } finally {
                // Safely recycle croppedBitmap if it was a temporary cropped copy
                val localCropped = croppedBitmap
                if (localCropped != null && localCropped != bitmap && !localCropped.isRecycled) {
                    try {
                        localCropped.recycle()
                    } catch (e: Throwable) {
                        // Ignore recycling failure
                    }
                }
            }
        }
    }

    fun setFilterMode(mode: PreprocessFilterMode) {
        _state.update { it.copy(filterMode = mode) }
        val captured = _state.value.capturedImage
        if (captured != null && !captured.isRecycled) {
            onCaptureImage(captured)
        }
    }

    fun setUseCrop(enabled: Boolean) {
        _state.update { it.copy(useCrop = enabled) }
        val captured = _state.value.capturedImage
        if (captured != null && !captured.isRecycled) {
            onCaptureImage(captured)
        }
    }

    fun clearResult() {
        val currentState = _state.value
        currentState.capturedImage?.let { if (!it.isRecycled) it.recycle() }
        currentState.processedImage?.let { if (!it.isRecycled) it.recycle() }
        
        _state.update { 
            it.copy(
                result = null, 
                capturedImage = null, 
                processedImage = null,
                error = null, 
                isCaptured = false
            ) 
        }
    }

    override fun onCleared() {
        super.onCleared()
        val currentState = _state.value
        currentState.capturedImage?.let { if (!it.isRecycled) it.recycle() }
        currentState.processedImage?.let { if (!it.isRecycled) it.recycle() }
    }
}
