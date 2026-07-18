import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Remove imports
content = re.sub(r'import com\.example\.feature\.advancedfeatures\.ui\.EquationSolverScreen\n', '', content)
content = re.sub(r'import com\.example\.feature\.advancedfeatures\.ui\.EquationSolverViewModel\n', '', content)
content = re.sub(r'import com\.example\.domain\.usecase\.SolveEquationUseCase\n', '', content)

# Remove equationViewModel instantiation
content = re.sub(r'\s*private val equationViewModel by lazy \{ EquationSolverViewModel\(SolveEquationUseCase\(\)\) \}\n', '\n', content)

# Remove from VoiceAITutorDialog parameters
content = re.sub(r'\s*equationViewModel = equationViewModel,\n', '\n', content)

# Remove from onNavigateTo callbacks in CalculatorScreen
content = re.sub(r'\s*onNavigateToEquationSolver = \{ navigateTo\("equation_solver"\) \},\n', '\n', content)

# Remove from AdvancedFeaturesScreen
content = re.sub(r'\s*\} else if \(currentScreen == "equation_solver"\) \{\n\s*EquationSolverScreen\(\n\s*viewModel = equationViewModel,\n\s*onBack = \{ screenStack = screenStack\.dropLast\(1\) \}\n\s*\)\n', '\n', content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
