# 🔐 Password Manager – Project Documentation

---

## 1. 📌 Overview

The Password Manager is a secure, cross-platform password management application built with Kotlin Multiplatform Mobile (KMM). It provides a unified codebase for storing, managing, and securing sensitive credentials across Android, iOS, Desktop, and Web platforms. 

The application addresses the critical need for secure password storage with military-grade encryption while offering an intuitive user interface for seamless credential management.

### Problem Solved
- Password reuse and weak passwords compromise account security
- Managing multiple complex passwords is challenging
- Centralized secure storage for all sensitive credentials

### Target Users
- Privacy-conscious individuals who need secure credential storage
- Users who want a unified password solution across multiple devices
- Organizations seeking secure credential management for teams

---

## 2. ✨ Features

### Core Security Features
* AES-256-GCM encryption for all stored fields
* PBKDF2-HMAC-SHA256 key derivation with 200,000 iterations
* Secure biometric authentication support
* Master password hashing for local verification

### Password Management
* Secure storage of usernames, passwords, and custom fields
* Emoji-based categorization for easy visual identification
* Searchable vault with real-time filtering
* Auto-clear clipboard for copied sensitive data
* Password generator (implied by architecture)

### Platform Support
* Native Android application with biometric integration
* Desktop applications for Windows, macOS, and Linux
* Web interface via JavaScript/WASM
* iOS support (disabled in current configuration)

### Backup & Export
* Encrypted export/import functionality
* Secure JSON backup packages
* Vault wiping and reset capabilities

---

## 3. 🏗 Architecture

### Architecture Pattern
The project follows a clean MVVM (Model-View-ViewModel) architecture pattern:
* **View Layer**: Compose Multiplatform UI components
* **ViewModel Layer**: State management and business logic coordination
* **Repository Layer**: Single source of truth for data operations
* **Data Source Layer**: Room database with encrypted persistence

### Module Structure
```
├── shared/              # Core business logic and cross-platform code
│   ├── data/            # Data layer including repositories, DAOs, and crypto
│   ├── domain/          # Domain models and use cases
│   ├── ui/              # ViewModels and shared UI utilities
│   └── di/              # Dependency injection setup
├── composeApp/         # Shared Compose Multiplatform UI components
├── androidApp/         # Android platform-specific entry point
├── iosApp/             # iOS platform-specific entry point (Xcode project)
└── server/             # Ktor backend server (optional component)
```

### Data Flow
1. **UI** triggers actions through **ViewModel**
2. **ViewModel** coordinates with **Repository** for data operations
3. **Repository** interacts with **DAO** for database operations
4. **CryptoEngine** handles encryption/decryption of sensitive fields
5. **DataStore** stores non-sensitive preferences and cryptographic salts

---

## 4. 📦 Tech Stack

### Languages
* Kotlin Multiplatform (primary language)
* Swift (iOS platform code)

### Frameworks & Tools
* Compose Multiplatform for cross-platform UI
* Kotlin Coroutines & Flow for asynchronous operations
* kotlinx.serialization for JSON handling
* Ktor for networking (server-side)

### Database
* Room Persistence Library with bundled SQLite
* Foreign Key relationships for data integrity

---

## 5. 📚 Libraries Used

### Core Libraries
* **Koin 4.2.1** - Lightweight dependency injection framework for managing app dependencies across platforms
* **Room 2.8.4** - SQLite database abstraction with compile-time query validation
* **kotlinx.coroutines** - Asynchronous programming and reactive streams
* **kotlinx.serialization** - Multiplatform serialization for data exchange

### UI & UX
* **Compose Multiplatform 1.10** - Declarative UI toolkit for all platforms
* **Material 3** - Modern design system for consistent UI
* **MaterialKolor** - Dynamic color scheme generation

### Networking & Utilities
* **Ktor 3.4** - Networking library for server communication
* **AndroidX Libraries** - Lifecycle, ViewModel, Navigation components

### Security-Critical Libraries
* **javax.crypto** (Android) - Standard Java cryptography APIs
* **Security Framework** (iOS) - Apple's native security APIs

Each library serves a specific purpose in ensuring security, performance, and cross-platform compatibility.

---

## 6. 🔐 Security Implementation

### Encryption Algorithms
* **AES-256-GCM**: For encrypting individual credential fields with authenticated encryption
* **PBKDF2-HMAC-SHA256**: Key derivation with 200,000 iterations for brute-force resistance
* **SHA-256**: For password verification hashing with separate iteration count

### Key Storage Strategy
1. Master password is never stored
2. Cryptographically random 16-byte salt stored in DataStore
3. 256-bit vault key derived from master password + salt at runtime
4. Vault key kept in memory during active sessions only
5. All sensitive data encrypted before database storage

### Secure Operations
* Unique Initialization Vector (IV) for each field encryption
* Automatic clipboard clearing after credential copying
* Session timeout and automatic logout
* Secure memory wiping for sensitive temporary variables

---

## 7. 📂 Project Structure

### shared/
Contains platform-independent business logic:
* `data/`: Database entities, DAOs, repositories, crypto engine
* `domain/`: Business models and use cases  
* `ui/`: ViewModels and shared UI utilities
* `di/`: Koin dependency injection modules

### shared/src/commonMain
Universal code shared across all platforms:
* Data models (`CredentialField`, `PasswordEntry`)
* Repository interfaces and implementations
* ViewModel base classes
* Cryptographic abstractions

### shared/src/androidMain, iosMain, jvmMain
Platform-specific implementations:
* `CryptoEngine.*.kt`: Native crypto implementations
* `ClipboardManager.*.kt`: Platform clipboard access
* `DatabaseFactory.*.kt`: Platform-specific database setup

---

## 8. 🔄 Data Flow

### UI → ViewModel → Repository → DataSource
1. **User Interaction**: Compose UI components trigger ViewModel methods
2. **State Management**: ViewModel maintains reactive state flows with coroutines
3. **Business Logic**: ViewModel coordinates with Repository for CRUD operations
4. **Data Access**: Repository uses DAO to interact with Room database
5. **Encryption/Decryption**: CryptoEngine handles all cryptographic operations
6. **Persistence**: Room stores encrypted data with integrity constraints

### Reactive Data Streams
All data is exposed through Kotlin Flows, enabling:
* Real-time UI updates through state observation
* Search functionality with live filtering
* Memory-efficient data handling during screen transitions

---

## 9. 🧩 Key Components Breakdown

### ViewModels
* `PassworldViewModel`: Manages main password list screen with search and filtering
* `UnlockViewModel`: Handles authentication flow and session management
* `AddEditViewModel`: Coordinates credential creation and editing

### Repositories
* `PasswordRepository`: Single source of truth for vault operations, manages CRUD with transactional safety

### Crypto Engine
* `CryptoEngine`: Platform-expect class with actual implementations for AES encryption/decryption, PBKDF2 key derivation
* `PassworldSession`: Runtime session management for vault key lifecycle
* `ExportCrypto`: Specialized encryption for backup package handling

### Data Models
* `PasswordEntry`: Represents a credential category (e.g., "GitHub Account")
* `CredentialField`: Individual encrypted fields (username, password) associated with an entry
* `EntryWithFields`: Relationship model combining entries with their fields

---

## 10. 📱 Platform-Specific Code

### Android
* Uses Android Keystore System for secure key storage
* Implements Android BiometricPrompt for authentication
* Utilizes AndroidX ClipboardManager for secure copy operations
* Integrates with Android Activity lifecycle for biometric callbacks

### iOS
* Leverages iOS Security Framework for key management
* Implements LAContext/LocalAuthentication for biometrics
* Uses iOS Keychain for secure storage of derived keys

### Desktop (JVM)
* Implements standard Java cryptography (javax.crypto)
* Uses system clipboard with platform-specific considerations

### Web (JS/WASM)
* Adapts to browser security model limitations
* Uses WebCrypto API for cryptographic operations

---

## 11. 🚀 Setup & Run Instructions

### Prerequisites
* Android Studio Iguana or newer (with KMP plugin)
* JDK 17 or higher (automatically managed by Gradle Toolchain)
* For iOS: macOS with Xcode 15+

### Building the Project
```bash
# Clean previous builds
./gradlew clean

# Run Android app
./gradlew :androidApp:assembleDebug

# Run Desktop app
./gradlew :composeApp:run

# Run Web app (JS)
./gradlew :composeApp:jsBrowserDevelopmentRun

# Run Backend server
./gradlew :server:run

# Run all tests
./gradlew allTests
```

### First-Time Setup
1. Clone the repository
2. Open in Android Studio
3. Wait for Gradle sync to complete (downloads dependencies automatically)
4. Run desired target platform configuration

---

## 12. ⚠️ Limitations / Missing Features

### Current Incomplete Features
* iOS targets are commented out in build files
* WebAssembly support is experimental
* No automatic synchronization between devices
* Limited third-party password manager importers
* Clipboard auto-clear timing not configurable
* No two-factor authentication (2FA) token storage
* Team/shared vault functionality absent

### Identified Issues
* Some UI components are commented out (PassworldListScreen)
* Several TODO markers in the codebase suggesting unfinished features
* Limited platform coverage (iOS currently disabled)

---

## 13. 🔮 Future Improvements

### Security Enhancements
* Implement hardware-backed keystore utilization on all platforms
* Add support for YubiKey and other hardware authenticators
* Integrate password strength analyzer
* Add encrypted notes and document storage

### Feature Expansion
* Enable full iOS support with proper Xcode integration
* Implement cloud synchronization with end-to-end encryption
* Add password breach monitoring and alert systems
* Develop browser extensions for autofill capabilities

### Performance Optimizations
* Implement database indexing for faster search operations
* Add lazy loading for large credential collections
* Optimize memory footprint for low-end devices
* Implement caching strategies for frequently accessed credentials

### Architecture Improvements
* Add offline-first synchronization patterns
* Implement comprehensive error recovery mechanisms
* Add more granular permission controls
* Integrate analytics for user experience improvements