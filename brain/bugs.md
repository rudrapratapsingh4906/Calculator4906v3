# Known Issues & Bug Tracking

## Category: Workspace Environment Issues (Temporary)
*These issues are specific to the AI Studio build environment and do not exist in the source repository.*

### 1. Workspace `gradle.properties` Missing
- **Symptoms**: `checkDebugAarMetadata` fails because AndroidX is not enabled by default in the workspace.
- **Status**: Mitigated via temporary `/gradle.properties`.

### 2. JVM Heap OutOfMemory (D8)
- **Symptoms**: Dexing fails with default heap settings.
- **Status**: Mitigated via `org.gradle.jvmargs` in workspace properties.

## Category: Project Source Issues
*These issues exist within the project codebase and may require source changes or user input.*

### 1. Missing `google-services.json`
- **Symptoms**: Google Services Plugin warnings during build.
- **Impact**: Potential runtime failure for Firebase/Google API features.
- **Required Action**: User to provide JSON file for permanent fix.

### 2. Hardcoded Signing Properties Requirement
- **Symptoms**: `app/build.gradle.kts` used `.get()` on missing signing properties, blocking non-release builds.
- **Status**: Adjusted to `.orNull` to ensure environment compatibility while preserving release support.
