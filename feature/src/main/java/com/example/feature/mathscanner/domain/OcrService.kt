package com.example.feature.mathscanner.domain

import android.graphics.Bitmap

interface OcrService {
    suspend fun recognizeText(bitmap: Bitmap): String
}

interface MathRecognitionService {
    suspend fun recognizeMathExpression(text: String): String
}

interface StepByStepSolverService {
    suspend fun solveStepByStep(expression: String): List<String>
}
