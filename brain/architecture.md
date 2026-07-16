# Project Architecture

## Overview
The project follows a **Multi-Module Clean Architecture** pattern, emphasizing separation of concerns and scalability.

## Module Structure

### 1. `:app`
- **Purpose**: The entry point and integration layer.
- **Key Components**:
    - `MainActivity.kt`: Orchestrates navigation and global UI state.
    - `Navigation`: Type-safe navigation using Kotlin Serialization.
    - Theme configuration (M3).

### 2. `:feature`
- **Purpose**: UI implementation for specific functional modules.
- **Key Features**:
    - `calculator`: Standard and Scientific interfaces.
    - `mathscanner`: OCR and Gemini-powered math solving.
    - `unitconverter`, `agecalculator`, `emicalculator`, `healthcalculator`, `percentagecgpa`.
    - `datetimecalculator`, `currencyconverter`.

### 3. `:domain`
- **Purpose**: Pure Kotlin business logic and abstractions.
- **Key Components**:
    - `CalculatorEngine.kt`: Core math evaluation logic.
    - `UseCases`: Orchestrate repository calls and business rules.
    - `Repository Interfaces`: Abstractions for data access.

### 4. `:data`
- **Purpose**: Implementation of data access and external integrations.
- **Key Components**:
    - `Room Database`: Local persistence for history and settings.
    - `OCRRepositoryImpl`: ML Kit integration for text recognition.
    - `GeminiApiService`: Interface for AI-powered solving.

### 5. `:core`
- **Purpose**: Shared utilities and common types.
- **Key Components**:
    - `Result.kt` / `AppError.kt`: Standardized error handling.
    - `DispatcherProvider`: Coroutine threading management.

## State Management
- **Pattern**: MVVM (Model-View-ViewModel).
- **Technology**: Jetpack Compose with `StateFlow` and `collectAsStateWithLifecycle`.
