import re

with open("feature/src/main/java/com/example/feature/advancedfeatures/ui/AdvancedFeaturesScreen.kt", "r") as f:
    content = f.read()

# Remove callback
content = re.sub(r'\s*onNavigateToEquationSolver: \(\) -> Unit,\n', '\n', content)

# Remove list item
content = re.sub(r'\s*AdvancedFeatureItem\("Equation Solver", Icons\.Default\.Calculate, "Solve polynomial and linear equations", "equation_solver"\),\n', '\n', content)

# Remove switch branch
content = re.sub(r'\s*"equation_solver" -> onNavigateToEquationSolver\(\)\n', '\n', content)

with open("feature/src/main/java/com/example/feature/advancedfeatures/ui/AdvancedFeaturesScreen.kt", "w") as f:
    f.write(content)
