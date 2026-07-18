import re

with open("feature/src/main/java/com/example/feature/calculator/ui/VoiceAITutorDialog.kt", "r") as f:
    content = f.read()

content = re.sub(r'\s*equationViewModel:\s*com\.example\.feature\.advancedfeatures\.ui\.EquationSolverViewModel\?\s*=\s*null,', '', content)

with open("feature/src/main/java/com/example/feature/calculator/ui/VoiceAITutorDialog.kt", "w") as f:
    f.write(content)
