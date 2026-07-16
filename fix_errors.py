import re

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "r") as f:
    content = f.read()

# Fix padding errors
content = content.replace("padding(horizontal = 16.dp, bottom = 8.dp)", "padding(start = 16.dp, end = 16.dp, bottom = 8.dp)")
content = content.replace("padding(horizontal = 12.dp, bottom = 16.dp)", "padding(start = 12.dp, end = 12.dp, bottom = 16.dp)")

# Remove the trailing garbage starting at line 619
# We can find `    }
# }
#           horizontalArrangement = Arrangement.spacedBy(buttonSpacing)`
garbage_start = content.find("          horizontalArrangement = Arrangement.spacedBy(buttonSpacing)")
if garbage_start != -1:
    content = content[:garbage_start]

with open("feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt", "w") as f:
    f.write(content.strip() + "\n")
