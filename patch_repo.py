import re

with open("data/src/main/java/com/example/data/repository/VoiceCommandRepositoryImpl.kt", "r") as f:
    content = f.read()

content = re.sub(r'\s*VoiceCommand\(\n\s*"open_equation", "Open Equation Solver",\n\s*listOf\("open equation solver", "equation solver", "solve equation", "equation kholo", "equation solver kholo"\),\n\s*listOf\("open equation solver", "equation solver", "solve equation", "equation kholo", "equation solver kholo"\)\n\s*\),\n', '\n', content)

with open("data/src/main/java/com/example/data/repository/VoiceCommandRepositoryImpl.kt", "w") as f:
    f.write(content)
