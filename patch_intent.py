import re

with open("app/src/main/java/com/example/service/VoiceBackgroundService.kt", "r") as f:
    content = f.read()

content = re.sub(r'\s*"open_equation" -> \{\n\s*speakAndLaunch\("Opening Equation Solver", "equation_solver"\)\n\s*saveHistory\(input, "Opening Equation Solver"\)\n\s*\}\n', '\n', content)

with open("app/src/main/java/com/example/service/VoiceBackgroundService.kt", "w") as f:
    f.write(content)

with open("feature/src/main/java/com/example/feature/calculator/ui/VoiceAITutorDialog.kt", "r") as f:
    content = f.read()

content = re.sub(r'\s*"open_equation" -> \{\n\s*val reply = "Opening Equation Solver\."\n\s*chatMessages\.add\(TutorChatMessage\(sender = MessageSender\.TUTOR, text = reply\)\)\n\s*speakAloud\(reply\)\n\s*coroutineScope\.launch \{\n\s*delay\(1200\)\n\s*onNavigateTo\("equation_solver"\)\n\s*onDismissRequest\(\)\n\s*\}\n\s*return\n\s*\}', '\n', content)

with open("feature/src/main/java/com/example/feature/calculator/ui/VoiceAITutorDialog.kt", "w") as f:
    f.write(content)
