sed -i 's/Modifier.fillMaxWidth().then(if(isLandscape) Modifier.weight(1f) else Modifier)/Modifier.fillMaxWidth().weight(1f)/g' feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt
sed -i 's/val baseButtonModifier = if (isLandscape) {/val baseButtonModifier = Modifier.fillMaxHeight()/g' feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt
sed -i '/Modifier.fillMaxHeight()/!b;n;s/} else {//g;n;s/Modifier.aspectRatio(1f)//g;n;s/}//g' feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt
sed -i '/Modifier.fillMaxHeight()/!b;n;s/} else {//g;n;s/Modifier.aspectRatio(1.2f)//g;n;s/}//g' feature/src/main/java/com/example/feature/calculator/ui/CalculatorScreen.kt
