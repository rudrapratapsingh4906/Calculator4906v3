./gradlew assembleDebug || echo "assembleDebug failed"
./gradlew test || echo "test failed"
ls -la app/build/outputs/apk/debug/app-debug.apk || echo "Debug APK missing"
