cat << 'INNER' > patch_service.py
import re

with open("app/src/main/java/com/example/service/VoiceBackgroundService.kt", "r") as f:
    content = f.read()

# Fix error handling in VoiceBackgroundService
error_handler = """                    Log.e(TAG, "Speech recognizer error: $message ($error)")
                    isListening = false
                    
                    // Do not restart on critical errors
                    if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS || error == SpeechRecognizer.ERROR_CLIENT) {
                        Log.e(TAG, "Critical error. Stopping continuous listening.")
                        stopContinuousListening()
                        return
                    }

                    // Restart if continuous listening is enabled
                    serviceScope.launch {
                        delay(1000)
                        checkAndRestartListeningIfNeeded()
                    }"""

content = re.sub(
    r'                    Log.e\(TAG, "Speech recognizer error: \$message \(\$error\)"\)\n                    isListening = false\n                    \n                    // Restart if continuous listening is enabled\n                    serviceScope.launch \{\n                        delay\(1000\)\n                        checkAndRestartListeningIfNeeded\(\)\n                    \}',
    error_handler,
    content
)

with open("app/src/main/java/com/example/service/VoiceBackgroundService.kt", "w") as f:
    f.write(content)
INNER
python3 patch_service.py
