import re

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "r") as f:
    content = f.read()

# Fix padding errors
content = content.replace("padding(horizontal = 32.dp, bottom = 8.dp)", "padding(start = 32.dp, end = 32.dp, bottom = 8.dp)")
content = content.replace("padding(horizontal = 32.dp, bottom = 16.dp)", "padding(start = 32.dp, end = 32.dp, bottom = 16.dp)")

# Add aspectRatio import
if "import androidx.compose.foundation.layout.aspectRatio" not in content:
    content = content.replace("import androidx.compose.foundation.layout.Arrangement", "import androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.aspectRatio")

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "w") as f:
    f.write(content)
