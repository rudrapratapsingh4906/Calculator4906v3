import re

with open("app/src/main/java/com/example/service/VoiceBackgroundService.kt", "r") as f:
    content = f.read()
content = re.sub(r'\s*text\.contains\("equation"\) -> \{\n\s*speakAndLaunch\("Opening Equation Solver", "equation_solver"\)\n\s*\}\n', '\n', content)
with open("app/src/main/java/com/example/service/VoiceBackgroundService.kt", "w") as f:
    f.write(content)

with open("feature/src/main/java/com/example/feature/advancedfeatures/ui/AdvancedFeaturesScreen.kt", "r") as f:
    content = f.read()
content = re.sub(r'\s*ConverterItem\("Equation Solver", Icons\.Default\.Calculate, true, false, "equation_solver"\),\n', '\n', content)
with open("feature/src/main/java/com/example/feature/advancedfeatures/ui/AdvancedFeaturesScreen.kt", "w") as f:
    f.write(content)

with open("feature/src/main/java/com/example/feature/calculator/ui/VoiceAITutorDialog.kt", "r") as f:
    content = f.read()
content = re.sub(r'\s*text\.contains\("equation"\) -> \{\n\s*speakAloud\("Opening Equation Solver"\)\n\s*onDismissRequest\(\)\n\s*onNavigateTo\("equation_solver"\)\n\s*\}\n', '\n', content)
with open("feature/src/main/java/com/example/feature/calculator/ui/VoiceAITutorDialog.kt", "w") as f:
    f.write(content)
