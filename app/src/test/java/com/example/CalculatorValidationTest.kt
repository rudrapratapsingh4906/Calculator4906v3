package com.example

import com.example.feature.calculator.CalculatorEvent
import com.example.feature.calculator.CalculatorViewModel
import com.example.domain.usecase.CalculateExpressionUseCase
import com.example.domain.usecase.GetHistoryUseCase
import com.example.domain.usecase.ClearHistoryUseCase
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.CalculationRepository
import com.example.domain.model.Calculation
import com.example.core.util.AppError
import com.example.core.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeCalculateExpressionUseCase : CalculateExpressionUseCase {
    var evaluateOnlyResult: Result<String, AppError.Calculation> = Result.Success("0")
    
    override suspend fun invoke(
        expression: String,
        isDegreeMode: Boolean
    ): Result<Calculation, AppError.Calculation> {
        return Result.Success(Calculation("id", expression, "0", System.currentTimeMillis()))
    }
    
    override suspend fun evaluateOnly(
        expression: String,
        isDegreeMode: Boolean
    ): Result<String, AppError.Calculation> {
        return evaluateOnlyResult
    }
}

class FakeSettingsRepository : SettingsRepository {
    override val themeFlow = MutableStateFlow("Default")
    override val themeModeFlow = MutableStateFlow("System")
    override val vibrationEnabledFlow = MutableStateFlow(false)
    override val soundEnabledFlow = MutableStateFlow(false)
    override val orientationLockFlow = MutableStateFlow(false)
    override val backgroundImageUriFlow = MutableStateFlow<String?>(null)
    override val backgroundOpacityFlow = MutableStateFlow(1.0f)
    override val advancedVoiceModeEnabledFlow = MutableStateFlow(false)
    override val voiceAutoStartEnabledFlow = MutableStateFlow(false)
    override val continuousListeningEnabledFlow = MutableStateFlow(false)

    override fun setTheme(theme: String) { themeFlow.value = theme }
    override fun setThemeMode(themeMode: String) { themeModeFlow.value = themeMode }
    override fun setVibrationEnabled(enabled: Boolean) { vibrationEnabledFlow.value = enabled }
    override fun setSoundEnabled(enabled: Boolean) { soundEnabledFlow.value = enabled }
    override fun setOrientationLock(locked: Boolean) { orientationLockFlow.value = locked }
    override fun setBackgroundImageUri(uri: String?) { backgroundImageUriFlow.value = uri }
    override fun setBackgroundOpacity(opacity: Float) { backgroundOpacityFlow.value = opacity }
    override fun setAdvancedVoiceModeEnabled(enabled: Boolean) { advancedVoiceModeEnabledFlow.value = enabled }
    override fun setVoiceAutoStartEnabled(enabled: Boolean) { voiceAutoStartEnabledFlow.value = enabled }
    override fun setContinuousListeningEnabled(enabled: Boolean) { continuousListeningEnabledFlow.value = enabled }
}

class FakeCalculationRepository : CalculationRepository {
    override fun getCalculationHistory(): Flow<List<Calculation>> = flowOf(emptyList())
    override suspend fun saveCalculation(calculation: Calculation): Result<Unit, AppError.Storage> = Result.Success(Unit)
    override suspend fun clearHistory(): Result<Unit, AppError.Storage> = Result.Success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class CalculatorValidationTest {

    private lateinit var viewModel: CalculatorViewModel
    private val fakeCalculateExpressionUseCase = FakeCalculateExpressionUseCase()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val calculationRepository = FakeCalculationRepository()
        viewModel = CalculatorViewModel(
            calculateExpressionUseCase = fakeCalculateExpressionUseCase,
            getHistoryUseCase = GetHistoryUseCase(calculationRepository),
            clearHistoryUseCase = ClearHistoryUseCase(calculationRepository),
            settingsRepository = FakeSettingsRepository()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testOperatorReplacement() = runTest {
        // 23+ then press × -> 23×
        viewModel.onEvent(CalculatorEvent.InputChar('2'))
        viewModel.onEvent(CalculatorEvent.InputChar('3'))
        viewModel.onEvent(CalculatorEvent.InputChar('+'))
        assertEquals("23+", viewModel.state.value.currentExpression)

        viewModel.onEvent(CalculatorEvent.InputChar('×'))
        assertEquals("23×", viewModel.state.value.currentExpression)

        // 23× then press ÷ -> 23÷
        viewModel.onEvent(CalculatorEvent.InputChar('÷'))
        assertEquals("23÷", viewModel.state.value.currentExpression)
    }

    @Test
    fun testDuplicateOperators() = runTest {
        // 5++6 -> 5+6
        viewModel.onEvent(CalculatorEvent.InputChar('5'))
        viewModel.onEvent(CalculatorEvent.InputChar('+'))
        viewModel.onEvent(CalculatorEvent.InputChar('+'))
        viewModel.onEvent(CalculatorEvent.InputChar('6'))
        assertEquals("5+6", viewModel.state.value.currentExpression)
    }

    @Test
    fun testDecimalValidation() = runTest {
        // 12..5 -> 12.5
        viewModel.onEvent(CalculatorEvent.InputChar('1'))
        viewModel.onEvent(CalculatorEvent.InputChar('2'))
        viewModel.onEvent(CalculatorEvent.InputChar('.'))
        viewModel.onEvent(CalculatorEvent.InputChar('.'))
        viewModel.onEvent(CalculatorEvent.InputChar('5'))
        assertEquals("12.5", viewModel.state.value.currentExpression)
    }

    @Test
    fun testStartRules() = runTest {
        // Do NOT allow expressions to start with: ×, ÷, %, ^
        viewModel.onEvent(CalculatorEvent.InputChar('×'))
        assertEquals("", viewModel.state.value.currentExpression)

        viewModel.onEvent(CalculatorEvent.InputChar('÷'))
        assertEquals("", viewModel.state.value.currentExpression)

        viewModel.onEvent(CalculatorEvent.InputChar('%'))
        assertEquals("", viewModel.state.value.currentExpression)

        viewModel.onEvent(CalculatorEvent.InputChar('^'))
        assertEquals("", viewModel.state.value.currentExpression)

        // Allow number, -, +, (
        viewModel.onEvent(CalculatorEvent.InputChar('5'))
        assertEquals("5", viewModel.state.value.currentExpression)
    }

    @Test
    fun testCursorEditingReplacement() = runTest {
        // 12+45 -> 12|+45 -> press × -> 12×45
        viewModel.onEvent(CalculatorEvent.InputChar('1'))
        viewModel.onEvent(CalculatorEvent.InputChar('2'))
        viewModel.onEvent(CalculatorEvent.InputChar('+'))
        viewModel.onEvent(CalculatorEvent.InputChar('4'))
        viewModel.onEvent(CalculatorEvent.InputChar('5'))
        
        // Move cursor to before '+' (index 2)
        viewModel.onEvent(CalculatorEvent.UpdateExpression(TextFieldValue("12+45", selection = androidx.compose.ui.text.TextRange(2))))
        
        viewModel.onEvent(CalculatorEvent.InputChar('×'))
        assertEquals("12×45", viewModel.state.value.currentExpression)
    }

    @Test
    fun testModReplacement() = runTest {
        // 12mod then press + -> 12+
        viewModel.onEvent(CalculatorEvent.InputChar('1'))
        viewModel.onEvent(CalculatorEvent.InputChar('2'))
        viewModel.onEvent(CalculatorEvent.InputString("mod"))
        assertEquals("12mod", viewModel.state.value.currentExpression)

        viewModel.onEvent(CalculatorEvent.InputChar('+'))
        assertEquals("12+", viewModel.state.value.currentExpression)

        // 12+ then press mod -> 12mod
        viewModel.onEvent(CalculatorEvent.InputString("mod"))
        assertEquals("12mod", viewModel.state.value.currentExpression)
    }

    @Test
    fun testEqualButtonNoEvaluationOnOperatorEnd() = runTest {
        // 12+ = -> No evaluation
        fakeCalculateExpressionUseCase.evaluateOnlyResult = Result.Success("12")
        viewModel.onEvent(CalculatorEvent.InputChar('1'))
        viewModel.onEvent(CalculatorEvent.InputChar('2'))
        viewModel.onEvent(CalculatorEvent.InputChar('+'))
        
        // Initially result should be empty
        viewModel.onEvent(CalculatorEvent.Calculate)
        assertEquals("", viewModel.state.value.result)
    }
}
