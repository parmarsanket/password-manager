# AGENTS.md

## Project Overview
- **Type**: Compose Multiplatform project (Android, Desktop, Web, iOS, Server)
- **Tech Stack**: Kotlin 2.3, Compose Multiplatform 1.10, Gradle 9.1, Ktor 3.4
- **Rename Script**: Use `rename_project.bat` (Windows) or `rename_project.sh` (macOS/Linux)

## Critical Commands
```bash
# Always use ./gradlew (never system gradle)
./gradlew --stop                    # Stop all Gradle daemons
./gradlew clean                     # Clean build outputs
./gradlew allTests                  # Run all tests

# Platform-specific builds
./gradlew :androidApp:assembleDebug # Android debug APK
./gradlew :composeApp:run           # Desktop app
./gradlew :server:run               # Start Ktor server
./gradlew :composeApp:jsBrowserDevelopmentRun  # Web JS dev server
```

## Module Structure
1. `shared` - Core business logic (Room DB, Koin DI, DataStore)
2. `composeApp` - Shared UI components (Compose Multiplatform)
3. `androidApp` - Android entry point
4. `iosApp` - iOS Xcode project (disabled in current config)
5. `server` - Ktor backend server

## Key Dependencies
- **DI**: Koin 4.2.1
- **DB**: Room 2.8.4 with SQLite bundled
- **Serialization**: kotlinx-serialization 1.11.0
- **Navigation**: androidx-navigation-compose 2.9.2
- **UI**: Material3 with MaterialKolor

## Development Notes
- Uses Gradle Wrapper + Foojay Toolchain (no JDK install needed)
- First build will auto-download Gradle 9.1 and JDK 17
- Android minSdk = 24, compileSdk = 36
- iOS targets currently commented out in build configs

## Testing
- Tests located in `src/*Test*/` directories
- Run with `./gradlew allTests`
- Uses kotlin.test framework

## Troubleshooting
- Use `./gradlew --stop` if getting JDK/class version errors
- iOS framework issues: `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`
- Always use ./gradlew, never system gradle