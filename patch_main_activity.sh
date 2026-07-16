cat << 'INNER_EOF' > app/src/main/java/com/example/MainActivity.kt
package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.core.util.DispatcherProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsRepositoryImpl
import com.example.data.repository.CalculationRepositoryImpl
import com.example.domain.usecase.CalculateExpressionUseCaseImpl
import com.example.domain.usecase.CalculateAgeUseCase
import com.example.domain.usecase.ClearHistoryUseCase
import com.example.domain.usecase.GetHistoryUseCase
import com.example.domain.usecase.ConvertUnitUseCase
import com.example.domain.usecase.GetConversionUnitsUseCase
import com.example.feature.calculator.CalculatorViewModel
import com.example.feature.calculator.ui.CalculatorScreen
import com.example.feature.agecalculator.AgeCalculatorViewModel
import com.example.feature.agecalculator.ui.AgeCalculatorScreen
import com.example.feature.unitconverter.UnitConverterViewModel
import com.example.feature.unitconverter.ui.UnitConverterScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val settingsRepository = SettingsRepositoryImpl(applicationContext)
        val dispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = Dispatchers.Main
            override val io: CoroutineDispatcher = Dispatchers.IO
            override val default: CoroutineDispatcher = Dispatchers.Default
            override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        }
        val repository = CalculationRepositoryImpl(db.calculationDao, dispatcherProvider)
        val calculateUseCase = CalculateExpressionUseCaseImpl(com.example.domain.math.CalculatorEngine(), repository)
        val getHistoryUseCase = GetHistoryUseCase(repository)
        val clearHistoryUseCase = ClearHistoryUseCase(repository)
        val calculateAgeUseCase = CalculateAgeUseCase()
        val convertUnitUseCase = ConvertUnitUseCase()
        val getConversionUnitsUseCase = GetConversionUnitsUseCase()

        val viewModel = CalculatorViewModel(
            calculateUseCase, 
            getHistoryUseCase, 
            clearHistoryUseCase, 
            settingsRepository
        )

        val ageViewModel = AgeCalculatorViewModel(calculateAgeUseCase)
        val unitViewModel = UnitConverterViewModel(convertUnitUseCase, getConversionUnitsUseCase, applicationContext)

        setContent {
            val theme by settingsRepository.themeFlow.collectAsState(initial = "Default")
            val orientationLock by settingsRepository.orientationLockFlow.collectAsState(initial = false)

            var currentScreen by remember { mutableStateOf("calculator") }

            if (orientationLock) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }

            MyApplicationTheme(themeName = theme) {
                if (currentScreen == "calculator") {
                    CalculatorScreen(
                        viewModel = viewModel,
                        onNavigateToAgeCalculator = { currentScreen = "age_calculator" },
                        onNavigateToUnitConverter = { currentScreen = "unit_converter" }
                    )
                } else if (currentScreen == "age_calculator") {
                    AgeCalculatorScreen(
                        viewModel = ageViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "unit_converter") {
                    UnitConverterScreen(
                        viewModel = unitViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                }
            }
        }
    }
}
INNER_EOF
