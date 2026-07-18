import re

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "r") as f:
    content = f.read()

# Remove callback
content = re.sub(r'\s*onNavigateToEquationSolver: \(\) -> Unit = \{\},\n', '\n', content)

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "w") as f:
    f.write(content)

with open("feature/src/main/java/com/example/feature/calculator/ui/VoiceAITutorDialog.kt", "r") as f:
    content = f.read()

# Remove parameter
content = re.sub(r'\s*equationViewModel: com\.example\.feature\.advancedfeatures\.ui\.EquationSolverViewModel\? = null,\n', '\n', content)

# Check for any usages of equationViewModel in VoiceAITutorDialog and remove them.
# I will do a quick grep first.
