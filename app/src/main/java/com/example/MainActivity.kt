package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
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
import com.example.domain.usecase.GetScientificConstantsUseCase
import com.example.feature.calculator.CalculatorViewModel
import com.example.feature.calculator.ui.CalculatorScreen
import com.example.feature.agecalculator.AgeCalculatorViewModel
import com.example.feature.agecalculator.ui.AgeCalculatorScreen
import com.example.feature.unitconverter.UnitConverterViewModel
import com.example.feature.unitconverter.ui.UnitConverterScreen
import com.example.feature.scientificconstants.ScientificConstantsViewModel
import com.example.feature.scientificconstants.ui.ScientificConstantsScreen
import com.example.feature.percentagecgpa.PercentageCgpaViewModel
import com.example.feature.percentagecgpa.ui.PercentageCgpaScreen
import com.example.feature.emicalculator.EmiCalculatorViewModel
import com.example.feature.emicalculator.ui.EmiCalculatorScreen
import com.example.feature.healthcalculator.HealthCalculatorViewModel
import com.example.feature.healthcalculator.ui.HealthCalculatorScreen
import com.example.feature.currencyconverter.CurrencyConverterViewModel
import com.example.feature.currencyconverter.ui.CurrencyConverterScreen
import com.example.feature.datetimecalculator.DateTimeCalculatorViewModel
import com.example.feature.datetimecalculator.ui.DateTimeCalculatorScreen
import com.example.feature.mathscanner.ui.MathScannerScreen
import com.example.feature.mathscanner.ui.MathScannerViewModel
import com.example.feature.advancedfeatures.ui.GraphPlotterScreen
import com.example.feature.advancedfeatures.ui.GraphPlotterViewModel
import com.example.feature.advancedfeatures.ui.MatrixCalculatorScreen
import com.example.feature.advancedfeatures.ui.MatrixCalculatorViewModel
import com.example.feature.advancedfeatures.ui.EquationSolverScreen
import com.example.feature.advancedfeatures.ui.EquationSolverViewModel
import com.example.feature.advancedfeatures.ui.CalculusScreen
import com.example.feature.advancedfeatures.ui.CalculusViewModel
import com.example.feature.advancedfeatures.ui.ComplexCalculatorScreen
import com.example.feature.advancedfeatures.ui.ComplexViewModel
import com.example.feature.advancedfeatures.ui.StatisticsScreen
import com.example.feature.advancedfeatures.ui.StatisticsViewModel
import com.example.domain.usecase.PlotGraphUseCase
import com.example.domain.usecase.MatrixOperationsUseCase
import com.example.domain.usecase.SolveEquationUseCase
import com.example.domain.usecase.CalculusUseCase
import com.example.domain.usecase.ComplexUseCase
import com.example.domain.usecase.StatisticsUseCase
import com.example.data.repository.GraphRepositoryImpl
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    private val db by lazy { AppDatabase.getInstance(applicationContext) }
    private val settingsRepository by lazy { SettingsRepositoryImpl(applicationContext) }
    private val dispatcherProvider by lazy {
        object : DispatcherProvider {
            override val main: CoroutineDispatcher = Dispatchers.Main
            override val io: CoroutineDispatcher = Dispatchers.IO
            override val default: CoroutineDispatcher = Dispatchers.Default
            override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        }
    }
    private val repository by lazy { CalculationRepositoryImpl(db.calculationDao, dispatcherProvider) }
    private val calculateUseCase by lazy { CalculateExpressionUseCaseImpl(com.example.domain.math.CalculatorEngine(), repository) }
    private val getHistoryUseCase by lazy { GetHistoryUseCase(repository) }
    private val clearHistoryUseCase by lazy { ClearHistoryUseCase(repository) }
    private val calculateAgeUseCase by lazy { CalculateAgeUseCase() }
    private val convertUnitUseCase by lazy { ConvertUnitUseCase() }
    private val getConversionUnitsUseCase by lazy { GetConversionUnitsUseCase() }
    private val getScientificConstantsUseCase by lazy { GetScientificConstantsUseCase() }

    private val viewModel by lazy {
        CalculatorViewModel(
            calculateUseCase, 
            getHistoryUseCase, 
            clearHistoryUseCase, 
            settingsRepository
        )
    }

    private val ageViewModel by lazy { AgeCalculatorViewModel(calculateAgeUseCase) }
    private val unitViewModel by lazy { UnitConverterViewModel(convertUnitUseCase, getConversionUnitsUseCase, applicationContext) }
    private val constantsViewModel by lazy { ScientificConstantsViewModel(getScientificConstantsUseCase, applicationContext) }
    private val percentageViewModel by lazy { PercentageCgpaViewModel(repository, applicationContext) }
    private val emiViewModel by lazy { EmiCalculatorViewModel(repository, applicationContext) }
    private val healthViewModel by lazy { HealthCalculatorViewModel(repository, applicationContext) }
    private val currencyViewModel by lazy { CurrencyConverterViewModel(repository, applicationContext) }
    private val dateTimeViewModel by lazy { DateTimeCalculatorViewModel(repository, applicationContext) }
    private val mathScannerViewModel by lazy { MathScannerViewModel() }
    private val graphViewModel by lazy { GraphPlotterViewModel(PlotGraphUseCase(GraphRepositoryImpl(applicationContext)), com.example.domain.math.CalculatorEngine()) }
    private val matrixViewModel by lazy { MatrixCalculatorViewModel(MatrixOperationsUseCase()) }
    private val equationViewModel by lazy { EquationSolverViewModel(SolveEquationUseCase()) }
    private val calculusViewModel by lazy { CalculusViewModel(CalculusUseCase(com.example.domain.math.CalculatorEngine())) }
    private val complexViewModel by lazy { ComplexViewModel(ComplexUseCase()) }
    private val statisticsViewModel by lazy { StatisticsViewModel(StatisticsUseCase()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by settingsRepository.themeFlow.collectAsState(initial = "Default")
            val orientationLock by settingsRepository.orientationLockFlow.collectAsState(initial = false)

            var currentScreen by remember { mutableStateOf("calculator") }

            LaunchedEffect(orientationLock) {
                if (orientationLock) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            MyApplicationTheme(themeName = theme) {
                if (currentScreen == "calculator") {
                    CalculatorScreen(
                        viewModel = viewModel,
                        onNavigateToAgeCalculator = { currentScreen = "age_calculator" },
                        onNavigateToUnitConverter = { currentScreen = "unit_converter" },
                        onNavigateToConstants = { currentScreen = "scientific_constants" },
                        onNavigateToPercentageCgpa = { currentScreen = "percentage_cgpa" },
                        onNavigateToEmiCalculator = { currentScreen = "emi_calculator" },
                        onNavigateToHealthCalculator = { currentScreen = "health_calculator" },
                        onNavigateToCurrencyConverter = { currentScreen = "currency_calculator" },
                        onNavigateToDateTimeCalculator = { currentScreen = "datetime_calculator" },
                        onNavigateToMathScanner = { currentScreen = "math_scanner" },
                        onNavigateToGraphPlotter = { currentScreen = "graph_plotter" },
                        onNavigateToMatrixCalculator = { currentScreen = "matrix_calculator" },
                        onNavigateToEquationSolver = { currentScreen = "equation_solver" },
                        onNavigateToCalculus = { currentScreen = "calculus" },
                        onNavigateToComplexCalculator = { currentScreen = "complex_calculator" },
                        onNavigateToStatistics = { currentScreen = "statistics_calculator" }
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
                } else if (currentScreen == "scientific_constants") {
                    ScientificConstantsScreen(
                        viewModel = constantsViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "percentage_cgpa") {
                    PercentageCgpaScreen(
                        viewModel = percentageViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "emi_calculator") {
                    EmiCalculatorScreen(
                        viewModel = emiViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "health_calculator") {
                    HealthCalculatorScreen(
                        viewModel = healthViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "currency_calculator") {
                    CurrencyConverterScreen(
                        viewModel = currencyViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "datetime_calculator") {
                    DateTimeCalculatorScreen(
                        viewModel = dateTimeViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "math_scanner") {
                    MathScannerScreen(
                        viewModel = mathScannerViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "graph_plotter") {
                    GraphPlotterScreen(
                        viewModel = graphViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "matrix_calculator") {
                    MatrixCalculatorScreen(
                        viewModel = matrixViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "equation_solver") {
                    EquationSolverScreen(
                        viewModel = equationViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "calculus") {
                    CalculusScreen(
                        viewModel = calculusViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "complex_calculator") {
                    ComplexCalculatorScreen(
                        viewModel = complexViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                } else if (currentScreen == "statistics_calculator") {
                    StatisticsScreen(
                        viewModel = statisticsViewModel,
                        onBack = { currentScreen = "calculator" }
                    )
                }
            }
        }
    }
}
