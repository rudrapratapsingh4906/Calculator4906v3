package com.example.feature.mathscanner.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.math.CalculatorEngine
import com.example.data.scanner.ExpressionParserImpl
import com.example.data.scanner.ExpressionValidatorImpl
import com.example.data.scanner.MathSolverRepositoryImpl
import com.example.data.scanner.OCRRepositoryImpl
import com.example.domain.scanner.OCRRepository
import com.example.domain.scanner.SolveUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MathScannerState(
    val isLoading: Boolean = false,
    val result: String? = null,
    val error: String? = null,
    val capturedImage: Bitmap? = null,
    val isCaptured: Boolean = false,
    val isSolved: Boolean = false
)

class MathScannerViewModel(
    private val ocrRepository: OCRRepository = OCRRepositoryImpl(),
    private val solveUseCase: SolveUseCase = SolveUseCase(
        MathSolverRepositoryImpl(
            ExpressionParserImpl(CalculatorEngine()), 
            ExpressionValidatorImpl()
        )
    )
) : ViewModel() {
    private val _state = MutableStateFlow(MathScannerState())
    val state: StateFlow<MathScannerState> = _state.asStateFlow()

    fun onCaptureImage(bitmap: Bitmap) {
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
            val recognizedText = ocrRepository.recognizeText(bitmap)
            _state.update {
                it.copy(
                    isLoading = false,
                    result = recognizedText
                )
            }
        }
    }

    fun solveMath(expression: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = solveUseCase.execute(expression)
            _state.update { it.copy(isLoading = false, result = result, isSolved = true) }
        }
    }

    fun clearResult() {
        _state.update { it.copy(result = null, capturedImage = null, error = null, isCaptured = false, isSolved = false) }
    }
}
