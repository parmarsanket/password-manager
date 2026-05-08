# 🔐 Passworld Manager

> **Offline-first password and credential vault** built with Kotlin Multiplatform and Compose Multiplatform. Store flexible credentials—websites, bank accounts, SIM details, or custom secrets—with military-grade AES-256-GCM encryption. Unlock via master password, fingerprint, or Windows Hello. No cloud. No sync. Just local security.

<p align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.3-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android](https://img.shields.io/badge/Android-minSdk%2024%20%7C%20targetSdk%2036-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Gradle](https://img.shields.io/badge/Gradle-9.5.0-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Ktor](https://img.shields.io/badge/Ktor-3.4.2-087CFA)](https://ktor.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

</p>

## 📚 Quick Navigation

- [What is it?](#-what-is-it)
- [Complete Feature Matrix](#-complete-feature-matrix)
- [Build & Run](#-how-to-build--run-on-each-platform)
- [Project Structure](#-project-structure-deep-dive)
- [Complete Architecture](#-complete-architecture)
- [Getting Started](#-getting-started-in-5-minutes)
- [Tech Stack](#tech-stack)
- [Security Architecture](#-security-architecture)
- [Core Workflows](#-core-workflows)
- [Testing & Quality](#-testing--quality)
- [Developer Quick Start](#-developer-quick-start)
- [Future Improvements](#-future-improvements-priority-order)
- [Implementation Notes](#-implementation-notes)

---

## 🎯 What is it?

**Passworld Manager** is a production-ready credential vault for individuals who need **private, local-only password storage**. Unlike cloud password managers, every credential stays encrypted on your device—no accounts, no cloud, no sync.

### Why this approach?
- ✅ Complete privacy—your data never leaves your device
- ✅ Works offline—no internet required
- ✅ Flexible schema—store websites, bank accounts, SIM codes, or anything custom
- ✅ Multiple unlock methods—master password + biometric + Windows Hello
- ✅ Portable—export/import encrypted backups with a separate backup password
- ✅ Cross-platform—Android, Desktop (Windows/Mac/Linux), iOS, Web (scaffolded)

**Tech foundation**: Kotlin Multiplatform + Compose Multiplatform with AGP 9.0.1, Kotlin 2.3, Gradle 9.1.

---

---

## 🏁 How to Build & Run on Each Platform

### 🤖 Android

**From Android Studio (easiest):**
```
1. Open project
2. Wait for Gradle sync (first time: 3-5 minutes)
3. Select emulator or device
4. Click ▶️ Run
```

**From terminal:**
```bash
# Debug APK
./gradlew :androidApp:assembleDebug
# Output: androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Release APK
./gradlew :androidApp:assembleRelease
```

---

### 🖥️ Desktop (Windows / macOS / Linux)

**From Android Studio:**
```
1. Run config dropdown → select "composeApp"
2. Click ▶️ Run
```

**From terminal:**
```bash
./gradlew :composeApp:run
```

**Native installers:**
```bash
# Windows → .msi
./gradlew :composeApp:packageMsi

# macOS → .dmg
./gradlew :composeApp:packageDmg

# Linux → .deb
./gradlew :composeApp:packageDeb

# Output: composeApp/build/compose/binaries/
```

---

### 🌐 Web (JavaScript)

```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
# Opens http://localhost:8080
```

**Production:**
```bash
./gradlew :composeApp:jsBrowserProductionWebpack
# Output: composeApp/build/dist/js/productionExecutable/
```

---

### 🌐 Web (WebAssembly)

⚠️ Experimental

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

---

### 🍎 iOS

**Option A — Xcode (recommended):**
```
1. Open iosApp/iosApp.xcodeproj in Xcode
2. Select iPhone simulator or device
3. Click ▶️ Run
```

**Option B — Android Studio:**
```
1. Install Kotlin Multiplatform plugin
2. Select iOS run config
3. Click ▶️ Run
```

**For physical device:** Edit `iosApp/Configuration/Config.xcconfig`:
```
TEAM_ID=YOUR_APPLE_TEAM_ID
```

---

### ⚙️ Server (Ktor Backend)

```bash
./gradlew :server:run
# Server at http://localhost:8080
```

> Note: Vault doesn't require server; it's a sample only.

---

## 📁 Project Structure Deep Dive

```
📦 password-manager/
 ┣ 📂 androidApp/
 ┃ ┣ 📂 src/main/kotlin/com/sanket/tools/passwordmanager/
 ┃ ┃ ┣ MainActivity.kt          ← Android entry
 ┃ ┃ ┗ PasswordManagerApp.kt    ← Koin init
 ┃ ┣ 📂 src/main/res/
 ┃ ┃ ┗ values/strings.xml
 ┃ ┗ 📄 build.gradle.kts
 ┃
 ┣ 📂 composeApp/                ← ★ Main Compose UI
 ┃ ┣ 📂 src/commonMain/kotlin/   ← Shared UI (Android + Desktop)
 ┃ ┃ ┣ App.kt                   ← Root composable
 ┃ ┃ ┣ 📂 theme/                ← Material 3 + dynamic colors
 ┃ ┃ ┃ ┣ Theme.kt
 ┃ ┃ ┃ ┗ Color.kt
 ┃ ┃ ┣ 📂 ui/
 ┃ ┃ ┃ ┣ component/             ← Reusable components
 ┃ ┃ ┃ ┃ ┣ VaultCredentialCard.kt
 ┃ ┃ ┃ ┃ ┣ FieldItem.kt
 ┃ ┃ ┃ ┃ ┣ VaultSearchField.kt
 ┃ ┃ ┃ ┃ ┣ VaultEntryBadge.kt
 ┃ ┃ ┃ ┃ ┗ ...more
 ┃ ┃ ┃ ┣ layout/                ← Adaptive layouts
 ┃ ┃ ┃ ┃ ┣ AdaptiveLayout.kt
 ┃ ┃ ┃ ┃ ┣ CollapsiblePortraitLayout.kt
 ┃ ┃ ┃ ┃ ┣ TwoPaneLayout.kt
 ┃ ┃ ┃ ┃ ┗ SinglePaneLayout.kt
 ┃ ┃ ┃ ┣ screen/
 ┃ ┃ ┃ ┃ ┣ unlock/
 ┃ ┃ ┃ ┃ ┃ ┣ UnlockScreen.kt
 ┃ ┃ ┃ ┃ ┃ ┣ UnlockFormCard.kt
 ┃ ┃ ┃ ┃ ┃ ┗ UnlockLayouts.kt
 ┃ ┃ ┃ ┃ ┗ vault/
 ┃ ┃ ┃ ┃ ┃ ┣ VaultDashboardScreen.kt
 ┃ ┃ ┃ ┃ ┃ ┣ VaultDashboardDialogs.kt
 ┃ ┃ ┃ ┃ ┃ ┣ VaultDashboardSupport.kt
 ┃ ┃ ┃ ┃ ┃ ┣ VaultDetailDialog.kt
 ┃ ┃ ┃ ┃ ┃ ┣ VaultEditorDialog.kt
 ┃ ┃ ┃ ┃ ┃ ┣ VaultSearchField.kt
 ┃ ┃ ┃ ┃ ┃ ┣ hero/
 ┃ ┃ ┃ ┃ ┃ ┣ layout/
 ┃ ┃ ┃ ┃ ┃ ┣ list/
 ┃ ┃ ┃ ┃ ┃ ┗ sidebar/
 ┃ ┣ 📂 src/jvmMain/kotlin/      ← Desktop entry
 ┃ ┃ ┗ main.kt
 ┃ ┣ 📂 src/iosMain/             ← iOS scaffolded
 ┃ ┣ 📂 src/jsMain/              ← Web scaffolded (commented)
 ┃ ┗ 📄 build.gradle.kts
 ┃
 ┣ 📂 shared/                    ← ★ Business logic (no UI)
 ┃ ┣ 📂 src/commonMain/kotlin/
 ┃ ┃ ┣ 📂 data/
 ┃ ┃ ┃ ┣ crypto/                ← AES-GCM encryption
 ┃ ┃ ┃ ┃ ┣ CryptoEngine.kt
 ┃ ┃ ┃ ┃ ┣ BiometricManager.kt  (expect/actual)
 ┃ ┃ ┃ ┃ ┣ KeystoreManager.kt   (expect/actual)
 ┃ ┃ ┃ ┃ ┗ PassworldSession.kt
 ┃ ┃ ┃ ┣ db/                    ← Room persistence
 ┃ ┃ ┃ ┃ ┣ PasswordEntry.kt
 ┃ ┃ ┃ ┃ ┣ CredentialField.kt
 ┃ ┃ ┃ ┃ ┣ PasswordDao.kt
 ┃ ┃ ┃ ┃ ┣ AppDatabase.kt
 ┃ ┃ ┃ ┃ ┗ DatabaseFactory.kt   (expect/actual)
 ┃ ┃ ┃ ┣ export/                ← Backup/restore
 ┃ ┃ ┃ ┃ ┣ ExportCrypto.kt
 ┃ ┃ ┃ ┃ ┣ ExportManager.kt
 ┃ ┃ ┃ ┃ ┣ ImportManager.kt
 ┃ ┃ ┃ ┃ ┗ BackupFileGateway.kt (expect/actual)
 ┃ ┃ ┃ ┣ prefs/
 ┃ ┃ ┃ ┃ ┣ PassworldPrefs.kt
 ┃ ┃ ┃ ┃ ┣ PassworldPrefsFactory.kt (expect/actual)
 ┃ ┃ ┃ ┃ ┗ ClipboardManager.kt  (expect/actual)
 ┃ ┃ ┃ ┗ repository/
 ┃ ┃ ┃ ┃ ┗ PasswordRepository.kt
 ┃ ┃ ┣ 📂 di/
 ┃ ┃ ┃ ┗ AppModule.kt           ← Koin modules
 ┃ ┃ ┣ 📂 domain/
 ┃ ┃ ┃ ┗ model/
 ┃ ┃ ┃ ┃ ┣ CredentialItem.kt
 ┃ ┃ ┃ ┃ ┣ DecryptedField.kt
 ┃ ┃ ┃ ┃ ┗ ...more
 ┃ ┃ ┗ 📂 ui/viewmodel/
 ┃ ┃ ┃ ┣ UnlockViewModel.kt
 ┃ ┃ ┃ ┣ PassworldViewModel.kt
 ┃ ┃ ┃ ┣ AddEditViewModel.kt
 ┃ ┃ ┃ ┗ ...state classes
 ┃ ┃
 ┃ ┣ 📂 src/androidMain/kotlin/  ← Android implementations
 ┃ ┃ ┣ CryptoEngine.android.kt
 ┃ ┃ ┣ KeystoreManager.android.kt
 ┃ ┃ ┣ BiometricManager.android.kt
 ┃ ┃ ┣ DatabaseFactory.android.kt
 ┃ ┃ ┣ ClipboardManager.android.kt
 ┃ ┃ ┣ BackupFileGateway.android.kt
 ┃ ┃ ┣ PassworldPrefsFactory.android.kt
 ┃ ┃ ┗ ActivityProvider.android.kt
 ┃ ┃
 ┃ ┣ 📂 src/jvmMain/kotlin/      ← Desktop implementations
 ┃ ┃ ┣ CryptoEngine.jvm.kt
 ┃ ┃ ┣ KeystoreManager.jvm.kt
 ┃ ┃ ┣ BiometricManager.jvm.kt
 ┃ ┃ ┣ DatabaseFactory.jvm.kt
 ┃ ┃ ┣ ClipboardManager.jvm.kt
 ┃ ┃ ┗ BackupFileGateway.jvm.kt
 ┃ ┃
 ┃ ┣ 📂 src/iosMain/             ← iOS placeholders (inactive)
 ┃ ┣ 📂 schemas/                 ← Room schema exports
 ┃ ┗ 📄 build.gradle.kts
 ┃
 ┣ 📂 iosApp/                    ← Xcode project (inactive)
 ┃ ┣ 📂 iosApp/
 ┃ ┃ ┗ MainViewController.kt
 ┃ ┗ 📂 Configuration/
 ┃ ┃ ┗ Config.xcconfig
 ┃
 ┣ 📂 server/                    ← Ktor backend sample
 ┃ ┣ 📂 src/main/kotlin/
 ┃ ┃ ┗ Application.kt
 ┃ ┣ 📂 src/test/kotlin/
 ┃ ┃ ┗ ApplicationTest.kt
 ┃ ┗ 📄 build.gradle.kts
 ┃
 ┣ 📂 gradle/
 ┃ ┣ 📄 libs.versions.toml       ← Version catalog
 ┃ ┗ 📂 wrapper/
 ┃ ┃ ┗ gradle-wrapper.properties
 ┃
 ┣ 📄 build.gradle.kts
 ┣ 📄 settings.gradle.kts
 ┣ 📄 gradle.properties
 ┣ 📄 gradlew / gradlew.bat      ← ★ Always use this
 ┣ 📄 rename_project.bat
 ┣ 📄 rename_project.sh
 ┣ 📄 rename_project.ps1
 ┣ 📄 README.md
 ┣ 📄 PRD.md
 ┗ 📄 LICENSE
```

---

## 🏗️ Complete Architecture

### Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         PASSWORD MANAGER                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │              PRESENTATION LAYER (Compose UI)                   │   │
│  ├────────────────────────────────────────────────────────────────┤   │
│  │  UnlockScreen  │  VaultDashboardScreen  │  VaultEditorDialog  │   │
│  │  (session-less)│  (if session exists)   │  (modal dialogs)    │   │
│  └────────────────────────────────────────────────────────────────┘   │
│           │                      │                      │               │
│           └──────────────────────┼──────────────────────┘               │
│                                  ↓                                      │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │           VIEWMODEL LAYER (State Management)                   │   │
│  ├────────────────────────────────────────────────────────────────┤   │
│  │  UnlockViewModel  │  PassworldViewModel  │  AddEditViewModel  │   │
│  │  - Crypto ops    │  - Vault state       │  - Field state      │   │
│  │  - Session mgmt  │  - Search/sort       │  - Validation       │   │
│  │  - Biometric     │  - Export/import     │  - Add/edit/delete  │   │
│  └────────────────────────────────────────────────────────────────┘   │
│           │                      │                      │               │
│           └──────────────────────┼──────────────────────┘               │
│                                  ↓                                      │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │            DOMAIN LAYER (Business Logic)                       │   │
│  ├────────────────────────────────────────────────────────────────┤   │
│  │  PasswordRepository  │  CryptoEngine  │  ExportManager         │   │
│  │  - CRUD operations   │  - Encryption  │  - Backup/restore      │   │
│  │  - Validation        │  - Decryption  │  - JSON serialization  │   │
│  │                      │  - Hashing     │                        │   │
│  └────────────────────────────────────────────────────────────────┘   │
│           │                      │                      │               │
│           └──────────────────────┼──────────────────────┘               │
│                                  ↓                                      │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │            DATA LAYER (Persistence & Security)                 │   │
│  ├────────────────────────────────────────────────────────────────┤   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │   │
│  │  │  Room Database   │  │  DataStore Prefs │  │  OS Keystore │ │   │
│  │  │  - SQLite        │  │  - Master hash   │  │  - Vault key │ │   │
│  │  │  - Encrypted DBs │  │  - User settings │  │  - Wrapped   │ │   │
│  │  │                  │  │  - Theme         │  │              │ │   │
│  │  └──────────────────┘  └──────────────────┘  └──────────────┘ │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                  ↓                                      │
│  ┌────────────────────────────────────────────────────────────────┐   │
│  │        PLATFORM LAYER (expect/actual implementations)          │   │
│  ├────────────────────────────────────────────────────────────────┤   │
│  │  Android     │    Desktop (JVM)    │    iOS         │   Web    │   │
│  │  - Keystore  │    - Windows Cred   │    - Keychain  │   - N/A  │   │
│  │  - Biometric │    - Hello          │    - Touch ID  │          │   │
│  │  - Backup    │    - File system    │    - Face ID   │          │   │
│  └────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Encryption Architecture

```
USER ENTERS MASTER PASSWORD
              ↓
      PBKDF2-SHA256
    (200k iterations)
              ↓
     ┌────────────────┐
     │  Master Key    │
     │  (32 bytes)    │
     └────────────────┘
              ↓
     ┌─────────────────────────┐
     │ Derive Vault Key        │  ← Stored separately in OS Keystore
     │ HMAC-SHA256(nonce)      │     (wrapped/encrypted)
     └─────────────────────────┘
              ↓
     EACH ENCRYPTED FIELD
          ↓         ↓
      Plaintext  IV (12 bytes)
                ↓
         AES-256-GCM
              ↓
      Ciphertext + Tag
```

### Module Relationships

```
┌─────────────────────────────────────────────────────────────┐
│                  composeApp (UI Layer)                      │
│                  - Compose Multiplatform                    │
│                  - Navigation: UnlockScreen ↔ VaultScreen   │
│                  - Material 3 theme                         │
└────────────────────┬────────────────────────────────────────┘
                     │ depends on
┌────────────────────▼────────────────────────────────────────┐
│                   shared (Logic Layer)                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ data/                                                │   │
│  │ ├─ crypto/   (AES, PBKDF2, Keystore)                │   │
│  │ ├─ db/       (Room, SQLite)                         │   │
│  │ ├─ export/   (Backup, restore)                      │   │
│  │ ├─ prefs/    (DataStore, clipboard)                 │   │
│  │ └─ repository/ (CRUD facade)                        │   │
│  │ domain/       (Models, interfaces)                  │   │
│  │ ui/viewmodel/ (State management)                    │   │
│  │ di/           (Koin configuration)                  │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │ depends on
        ┌────────────┴────────────┐
        │                         │
┌───────▼──────────┐    ┌────────▼────────┐
│ androidApp       │    │ iosApp / server │
│ (entry point,    │    │ (native entry)  │
│  manifests, res) │    │                 │
└──────────────────┘    └─────────────────┘
```

---

## ✅ Complete Feature Matrix

| Feature | Status | Notes |
|---|---|---|
| **Authentication** |
| Master password setup | ✅ Implemented | PBKDF2-HMAC-SHA256, 200k iterations |
| Master password login | ✅ Implemented | Verifier comparison with SHA-256 |
| Android biometric unlock | ✅ Implemented | Fingerprint or device credential |
| Windows Hello unlock | ✅ Implemented | Desktop JVM via WinRT |
| Session timeout / auto-lock | ✅ Implemented | 5-minute inactivity timeout |
| **Credentials** |
| Add/edit/delete entries | ✅ Implemented | Full CRUD support |
| Freeform custom fields | ✅ Implemented | Any label + secret/non-secret |
| Secret reveal/hide | ✅ Implemented | Masked in list, reveal in detail |
| Copy to clipboard | ✅ Implemented | Platform-specific clipboard managers |
| Clipboard auto-clear | ⏳ Not implemented | Security gap—TODO |
| Search by site/app name | ✅ Implemented | Case-insensitive contains match |
| Quick templates | ✅ Implemented | Website, Bank, SIM templates |
| **Backup & Recovery** |
| Encrypted export | ✅ Implemented | AES-256-GCM with backup password |
| Encrypted import | ✅ Implemented | Appends entries, re-encrypts with device key |
| ZIP backup | ⏳ Not implemented | Currently JSON format only |
| **UI/UX** |
| Adaptive mobile/tablet/desktop | ✅ Implemented | Single-pane, two-pane, collapsing hero |
| Keyboard unlock flow | ✅ Implemented | IME actions + arrow navigation |
| Dark/light theme | ✅ Implemented | Material 3 + MaterialKolor dynamic |
| Settings screen | ⏳ Not implemented | Prefs exist; no UI for them |
| **Advanced** |
| Password generator | ⏳ Not implemented | TODO |
| Password strength meter | ⏳ Not implemented | TODO |
| Android Autofill | ⏳ Not implemented | TODO |
| Navigation routes (NavHost) | ⏳ Not implemented | Uses session-based switching instead |
| Categories | ⏳ Not implemented | No category entity |

---

## 🚀 Getting Started in 5 Minutes

### Prerequisites
- **Android Studio** (Narwhal 2025.1 or newer) — nothing else needed
- **No JDK install required** — Gradle Wrapper auto-downloads everything
- **No Gradle install required** — uses embedded wrapper

### Step 1: Clone

```bash
git clone https://github.com/parmarsanket/password-manager
cd password-manager
```

### Step 2: Rename Project (Optional)

If you want to customize the app name and package name:

**🪟 Windows:**
```
Double-click rename_project.bat
Follow the prompts
```

**🍎 macOS / 🐧 Linux:**
```bash
chmod +x rename_project.sh
./rename_project.sh
```

**What gets renamed:**
- Project name in `settings.gradle.kts`
- Package names in all `.kt` files
- App display name in `strings.xml`
- iOS/Android/Desktop build configs
- And more...

### Step 3: Open & Run

1. Open `File → Open` → select project folder in Android Studio
2. Wait for Gradle sync (first time: 3-5 minutes)
3. Select an emulator or device from toolbar
4. Click ▶️ **Run**

Done! ✅

---

## 📦 What's Inside: Module Overview

| Module | Purpose | Platforms |
|---|---|---|
| `androidApp` | Android app shell + manifest | Android |
| `composeApp` | Shared Compose UI + Desktop entry | Android, Desktop, Web (scaffolded) |
| `shared` | Business logic, crypto, DB, repos, ViewModels | All platforms |
| `server` | Ktor backend (sample only, not used by vault) | JVM |

---

## 🛠️ Tech Stack

| Component | Version | Notes |
|---|---|---|
| **Language** | Kotlin 2.3.21 | Modern, null-safe, multiplatform |
| **Build** | Gradle 9.5.0 | Wrapper only—no install needed |
| **Android** | AGP 9.2.1, minSdk 24, targetSdk 36 | Supports Android 7.0+ |
| **Compose** | 1.10.3 (Multiplatform) | Shared UI across platforms |
| **Material 3** | 1.10.0-alpha05 | Modern Material Design + themes |
| **Database** | Room 2.8.4 + SQLite 2.6.2 | Local persistence with migrations |
| **DI** | Koin 4.2.1 | Service locator for multiplatform |
| **Crypto** | `javax.crypto` (native) | AES-256-GCM, PBKDF2-SHA256 |
| **Coroutines** | 1.10.2 | Async operations, structured concurrency |
| **Serialization** | kotlinx-serialization 1.11.0 | JSON export/import |
| **Server** | Ktor 3.4.2 | Backend sample (not required for vault) |
| **JVM target** | Java 21 | Desktop builds |

### ⚡ Zero Setup

This project uses **Gradle Wrapper** + **Foojay Toolchain**:
- First build automatically downloads **Gradle 9.5**
- First build automatically downloads **JDK 21**
- No manual JDK or Gradle installation needed
- Works on Windows, Mac, Linux

---

## 🏁 Getting Started

### Step 1 — Clone This Repo

```bash
git clone https://github.com/parmarsanket/Compose-Multiplatform-Template-AGP-9.git
```

### Step 2 — Rename to Your Project (See section below)

### Step 3 — Open in Android Studio → Click ▶️ Run

---

## ✏️ How to Change Project Name & Package Name

This template includes **rename scripts** that do everything automatically.

### 🪟 On Windows

```
1. Open the project folder in File Explorer
2. Double-click  rename_project.bat
3. A window opens showing:

   Detected Existing Project Name:  Password Manager
   Detected Existing Package Name:  com.sanket.tools.passwordmanager

4. Type your NEW project name, press Enter
5. Type your NEW package name, press Enter
6. Done! Open in Android Studio and build.
```

**Example input:**
```
Enter NEW Project Name: MyAwesomeApp
Enter NEW Package Name: com.mycompany.awesomeapp
```

### 🍎 On macOS / 🐧 On Linux

```bash
# Make the script executable (only once):
chmod +x rename_project.sh

# Run it:
./rename_project.sh
```

It will show:
```
Detected Existing Project Name: Password Manager
Detected Existing Package Name: com.sanket.tools.passwordmanager

Enter NEW Project Name: MyAwesomeApp
Enter NEW Package Name: com.mycompany.awesomeapp
```

### What the script changes automatically:

| What | Where |
|---|---|
| `rootProject.name` | `settings.gradle.kts` |
| `namespace` | All `build.gradle.kts` files |
| `applicationId` | `androidApp/build.gradle.kts` |
| `package` declarations | All `.kt` files |
| `import` statements | All `.kt` files |
| App display name | `strings.xml` |
| iOS bundle ID | `Config.xcconfig` |
| iOS product name | `Config.xcconfig` |
| Desktop package name | `composeApp/build.gradle.kts` |
| Server group name | `server/build.gradle.kts` |
| Kotlin source folders | Moved to match new package |

### After renaming, in Android Studio:

```
File → Sync Project with Gradle Files
Build → Clean Project
Build → Rebuild Project
```

---

## 📱 How to Build & Run on Each Platform

---

### 🤖 Android

**What you need:** Android Studio (Narwhal 2025.1 or newer)

**Steps:**
```
1. Open project in Android Studio
2. Wait for Gradle sync to finish (first time takes a few minutes)
3. Select an emulator or connected device from the toolbar
4. Click ▶️ Run
```

**Or from terminal:**
```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# APK location:
# androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

**Or build release APK:**
```bash
./gradlew :androidApp:assembleRelease
```

---

### 🖥️ Desktop (Windows / macOS / Linux)

**What you need:** Nothing extra! Just the project.

**Run from Android Studio:**
```
1. In the Run Configurations dropdown (top toolbar), select "composeApp"
2. Click ▶️ Run
```

**Run from terminal:**
```bash
# Launch the desktop app directly
./gradlew :composeApp:run
```

**Create native installer:**
```bash
# Windows → .msi installer
./gradlew :composeApp:packageMsi

# macOS → .dmg installer
./gradlew :composeApp:packageDmg

# Linux → .deb package
./gradlew :composeApp:packageDeb
```

**Installer output location:**
```
composeApp/build/compose/binaries/
```

---

### 🌐 Web (JavaScript)

**What you need:** Nothing extra! Just a modern browser.

**Run from terminal:**
```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

**What happens:**
```
1. Gradle compiles your Kotlin code to JavaScript
2. A dev server starts automatically
3. Opens your browser at  http://localhost:8080
4. You can see your app running in the browser!
```

**Build production bundle:**
```bash
./gradlew :composeApp:jsBrowserProductionWebpack

# Output: composeApp/build/dist/js/productionExecutable/
```

---

### 🌐 Web (WebAssembly / Wasm)

**What you need:** A modern browser (Chrome 119+, Firefox 120+, Safari 18.2+)

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

> ⚠️ **Note:** Wasm support is experimental. For production web apps, use the JS target above.

---

### 🍎 iOS

**What you need:** macOS + Xcode 16+

**Option A — Build from Xcode (recommended):**
```
1. Open  iosApp/iosApp.xcodeproj  in Xcode
2. Select a simulator (e.g. iPhone 16)
3. Click ▶️ Run
```

**Option B — Build from Android Studio:**
```
1. Install the "Kotlin Multiplatform" plugin in Android Studio
   (Settings → Plugins → search "Kotlin Multiplatform")
2. Select the iOS run configuration from the dropdown
3. Click ▶️ Run
```

**For physical device:** Edit `iosApp/Configuration/Config.xcconfig`:
```
TEAM_ID=YOUR_APPLE_TEAM_ID
```

---

### ⚙️ Server (Ktor Backend)

**What you need:** Nothing extra!

```bash
./gradlew :server:run
```

**What happens:**
```
Server starts at  http://localhost:8080
```

---

## 📁 Project Structure

```
📦 project-root/
 ┣ 📂 androidApp/              ← Android app entry point
 ┃ ┣ 📂 src/main/kotlin/       ← MainActivity.kt
 ┃ ┗ 📄 build.gradle.kts
 ┃
 ┣ 📂 composeApp/              ← Shared Compose UI (★ main code here)
 ┃ ┣ 📂 src/
 ┃ ┃ ┣ 📂 commonMain/          ← ★ Shared UI code (App.kt)
 ┃ ┃ ┣ 📂 androidMain/         ← Android-specific UI
 ┃ ┃ ┣ 📂 iosMain/             ← iOS-specific UI
 ┃ ┃ ┣ 📂 jvmMain/             ← Desktop entry point
 ┃ ┃ ┣ 📂 jsMain/              ← Web JS entry point
 ┃ ┃ ┗ 📂 wasmJsMain/          ← Web Wasm entry point
 ┃ ┗ 📄 build.gradle.kts
 ┃
 ┣ 📂 shared/                  ← Shared business logic (no UI)
 ┃ ┣ 📂 src/
 ┃ ┃ ┣ 📂 commonMain/          ← ★ Platform-independent logic
 ┃ ┃ ┣ 📂 androidMain/         ← Android expect/actual
 ┃ ┃ ┣ 📂 iosMain/             ← iOS expect/actual
 ┃ ┃ ┣ 📂 jvmMain/             ← JVM expect/actual
 ┃ ┃ ┗ 📂 jsMain/              ← JS expect/actual
 ┃ ┗ 📄 build.gradle.kts
 ┃
 ┣ 📂 iosApp/                  ← iOS Xcode project
 ┃ ┣ 📂 iosApp/                ← Swift source files
 ┃ ┗ 📂 Configuration/         ← Config.xcconfig (Team ID here)
 ┃
 ┣ 📂 server/                  ← Ktor backend server
 ┃
 ┣ 📂 gradle/
 ┃ ┣ 📄 libs.versions.toml     ← ★ All dependency versions here
 ┃ ┗ 📂 wrapper/               ← Gradle wrapper (auto-downloads Gradle)
 ┃
 
 
 ┣ 📄 rename_project.bat       ← 🪟 Windows rename script
 ┣ 📄 rename_project.sh        ← 🍎🐧 macOS/Linux rename script
 ┣ 📄 rename_project.ps1       ← PowerShell logic (used by .bat)
 ┗ 📄 gradlew / gradlew.bat    ← ★ Always use this, NOT system Gradle
```

---

---

## 🔐 Security Architecture

### Encryption Details

Every credential field is encrypted with **AES-256-GCM**:
- **Key:** Derived from master password via PBKDF2-HMAC-SHA256 (200,000 iterations)
- **IV:** Fresh random 12 bytes per field
- **Auth tag:** 128 bits
- **Storage:** Base64(IV + ciphertext + auth tag)

```
Master Password
    ↓ PBKDF2-HMAC-SHA256 (200k iterations, 16-byte salt)
    ↓
256-bit Vault Key (stored in memory during session)
    ↓ AES-256-GCM
    ↓
Encrypted Field Values (stored in Room)
```

### What Gets Encrypted

✅ **Encrypted:**
- Credential field values (passwords, PINs, codes)

❌ **Not Encrypted (metadata only):**
- Field labels (e.g., "Bank PIN")
- Site/app names (needed for search)
- Creation timestamps

### Authentication Methods

| Method | Flow | Platform |
|---|---|---|
| **Master Password** | PBKDF2 derive → compare verifier | All |
| **Biometric (Android)** | Fingerprint/device credential → load saved key | Android |
| **Windows Hello** | WinRT UserConsentVerifier → load saved key | Windows Desktop |
| **Auto-lock** | 5-min timeout, keyboard/pointer events tracked | All |

### Device Key Storage

| Platform | Storage | Protection |
|---|---|---|
| Android | Android Keystore | AES-GCM wrapped |
| Desktop | PKCS12 file | OS-username-derived password |
| iOS | Placeholder | Target inactive |

### Import/Export Security

- **Backup password:** Separate from master password
- **Backup encryption:** AES-256-GCM with fresh salt per export
- **Import behavior:** Re-encrypts all fields with current device vault key
- **Portable:** Backup file has no master password or vault key—safe to share securely

### Known Security Gaps (Should Fix)

| Item | Issue | Mitigation |
|---|---|---|
| Clipboard auto-clear | Secrets remain in system clipboard after copy | Copy-to-clipboard with 30-60s timer (TODO) |
| Android backup | OS backup may copy sensitive files | Add backup rules or set `allowBackup="false"` |
| Android Keystore binding | Wrapping key not user-auth-required | Configure with user-auth-required flags |
| Desktop secure storage | Weaker than OS-native | Use DPAPI (Windows), Keychain (macOS), libsecret (Linux) |
| Biometric-only unlock | Requires saved vault key | OK security-wise; just convenience unlock |

---

## 🏗️ Architecture Layers

### High-Level Stack

```
┌────────────────────────────────────────┐
│  Platform Entry                        │
│  - PasswordManagerApp (Android)        │
│  - main.kt (Desktop)                   │
└────────────┬─────────────────────────┘
             │
             v
     ┌───────────────────┐
     │  Koin DI + Modules│  (dependency injection)
     └───────┬───────────┘
             │
             v
     ┌───────────────────────┐
     │  Compose App Shell    │  (session check)
     └───────┬───────────────┘
        ┌────┴────┐
        │          │
        v          v
    UnlockScreen  VaultDashboard
       (UI)          (UI)
```

### State Management Layers

| Layer | Responsibility | Examples |
|---|---|---|
| **Session** | Vault key in memory, timeout tracking | `PassworldSession` |
| **ViewModels** | Business logic, user actions | `UnlockViewModel`, `PassworldViewModel` |
| **Repository** | Database CRUD (no crypto) | `PasswordRepository` |
| **Database** | Encrypted persistence | Room + SQLite |
| **UI State** | Compose recomposition | `rememberSaveable`, `StateFlow` |

### Data Flow Example (Search)

```
User types query
  ↓
PassworldViewModel._searchQuery.update()
  ↓
PassworldViewModel.items (combine):
  • Room.getAllEntries() Flow
  • _searchQuery StateFlow
  • session.passworldKey StateFlow
  ↓
Filter by site name (case-insensitive)
  ↓
Decrypt all matching fields with session key
  ↓
Emit List<CredentialItem>
  ↓
VaultDashboardScreen collects state
  ↓
Compose recomposes UI with results
```

### Dependency Injection

**Koin modules in `AppModule.kt`:**

| Module | Provides |
|---|---|
| `databaseModule` | `AppDatabase`, `PasswordDao` |
| `cryptoModule` | `CryptoEngine`, `PassworldSession`, `ExportCrypto` |
| `repositoryModule` | `PasswordRepository`, `ExportManager`, `ImportManager` |
| `viewModelModule` | `UnlockViewModel`, `PassworldViewModel`, `AddEditViewModel` |
| `prefsModule` | `PassworldPrefs`, `BiometricManager`, `KeystoreManager` |

**Platform-specific bindings:**

| Platform | File | Bindings |
|---|---|---|
| Android | `PasswordManagerApp.kt` | `BiometricManager`, `KeystoreManager`, `ClipboardManager`, `BackupFileGateway` |
| Desktop | `main.kt` | Same interfaces, JVM implementations |

---

## 💾 Core Workflows

### First Launch (Setup)

```
1. App opens → App.kt checks PassworldSession.passworldKey
2. Key is null → UnlockScreen shows
3. UnlockViewModel checks PassworldPrefs.isSetupComplete()
4. Returns false → show "Create Master Password" form
5. User enters password + confirmation
6. UnlockViewModel.setupMasterPassword() on Dispatchers.Default:
   - generateSalt(16 bytes)
   - deriveVaultKey(password, salt) [PBKDF2 200k]
   - createPasswordVerifier(vaultKey)
   - saveMasterSecrets(salt, verifier) to prefs
   - KeystoreManager.saveVaultKey(vaultKey)
7. PassworldSession.start(vaultKey)
8. App() observes passworldKey != null → show VaultDashboardScreen ✅
```

### User Login (Returning)

```
1. App opens → check session key (null)
2. Show UnlockScreen in login mode
3. User enters master password
4. UnlockViewModel.login(password):
   - Load salt + verifier from prefs
   - Derive key: deriveVaultKey(password, salt)
   - Compare: verifyPassword(verifier, derivedKey)
   - If match: PassworldSession.start(derivedKey)
5. Room Flow queries trigger
6. PassworldViewModel decrypts entries
7. VaultDashboardScreen renders ✅
```

### Add Credential

```
1. User taps FAB
2. activeDialog = VaultDialog.AddEntry
3. AddEditViewModel.prepareNewEntry()
4. VaultEditorDialog opens with empty form
5. User fills: site, badge, fields (with labels + values)
6. Optional: Apply template (Website/Bank/SIM)
7. Tap Save
8. AddEditViewModel.save():
   - For each field:
     encryptedValue = CryptoEngine.encryptField(value, sessionKey)
   - PasswordRepository.saveEntry(header, fields)
9. Room DAO inserts PasswordEntry + CredentialField rows
10. Room Flow emits → PassworldViewModel decrypts → UI recomposes ✅
11. Snackbar: "Entry added" ✅
```

### Export (Backup)

```
1. User taps Export in vault
2. Show BackupPasswordDialog
3. User enters backup password
4. PassworldViewModel.exportVault(backupPassword):
   - ExportManager reads all Room entries
   - Decrypt each field with session key
   - Build ExportVault JSON
   - Generate fresh 16-byte salt
   - Derive backup key: PBKDF2(backupPassword, freshSalt)
   - Encrypt JSON: ExportCrypto.encrypt(json, backupKey)
   - Create ExportPackage {encryptedData, meta}
5. BackupFileGateway (AWT/Intent) shows file picker
6. User selects location
7. Write JSON file ✅
8. Snackbar: "Exported to..."
```

### Import (Restore)

```
1. User taps Import
2. BackupFileGateway shows file picker
3. User selects .json backup
4. BackupPasswordDialog asks for backup password
5. PassworldViewModel.importVault(password, fileContent):
   - Parse ExportPackage JSON
   - Extract salt from metadata
   - Derive backup key: PBKDF2(password, extractedSalt)
   - Decrypt: ExportCrypto.decrypt(encryptedData, backupKey)
   - Parse ExportVault JSON
   - For each entry:
     - Re-encrypt fields with session key
     - PasswordRepository.saveEntry()
6. Room emits updates → UI recomposes ✅
7. Snackbar: "Imported N entries"
```

---

## 📊 Feature Details

### Unlock Modes

✅ **Master Password:** Text entry, PBKDF2 derivation
✅ **Biometric (Android):** Fingerprint or device credential
✅ **Windows Hello (Desktop):** WinRT UserConsentVerifier
⏳ **Password Generator:** Not implemented
⏳ **Password Strength Meter:** Not implemented

### Credential Storage

✅ **Flexible Fields:** Any label, mark as secret or not
✅ **Templates:** Website (Email, Username, Password), Bank (Account Number, MPIN, TPIN, ATM PIN), SIM (Mobile Number, PUK, MNP)
✅ **Search:** Case-insensitive contains match
✅ **Copy to Clipboard:** One-tap with platform managers
⏳ **Categories:** Not implemented (stored as metadata only)

### Adaptive UI

| Device Type | Layout |
|---|---|
| Phone Portrait | Single-pane with collapsible hero |
| Phone Landscape | Compact sidebar + content pane |
| Tablet Portrait | Single-pane constrained width |
| Tablet Landscape / Desktop | Two-pane with expandable sidebar |

### Keyboard Shortcuts

- **Setup/Login:** IME Done submits password
- **Unlock:** Arrow keys navigate, Tab focuses
- **Vault:** Search field uses ImeAction.Search
- **Delete:** Confirmation dialog required

---

## 🧪 Testing & Quality

### Current Test Coverage

| Area | Status |
|---|---|
| Crypto tests | ⏳ Minimal—needs unit tests |
| Repository tests | ⏳ None |
| ViewModel tests | ⏳ None |
| UI tests | ⏳ None |
| Import/export tests | ⏳ None |
| Server endpoint | ✅ Basic test exists |

### Recommended Test Coverage (To Add)

- Crypto round-trip (encrypt/decrypt same value)
- Wrong password/key failures
- PBKDF2 deterministic output
- Repository CRUD operations
- ViewModel state transitions
- Import/export round trips
- Session timeout + lock
- Delete confirmation flows
- Security regression: no plaintext field values in DB

### Building & Running Tests

```bash
./gradlew allTests
./gradlew :shared:commonTest
./gradlew :server:test
```

---

## 🚀 Developer Quick Start

### Requirements

| Tool | Version | Notes |
|---|---|---|
| **Android Studio** | 2025.1+ | Narwhal or newer |
| **JDK** | 21 | Auto-downloaded by Gradle |
| **Gradle** | 9.5.0 | Auto-downloaded by Gradle Wrapper |
| **Android SDK** | minSdk 24, compileSdk 36 | Set in Gradle configs |

### Common Commands

```bash
# Build
./gradlew clean                    # Clean all outputs
./gradlew :androidApp:assembleDebug # Android debug APK
./gradlew :composeApp:run          # Desktop app

# Package
./gradlew :composeApp:packageMsi   # Windows installer
./gradlew :composeApp:packageDmg   # macOS installer
./gradlew :composeApp:packageDeb   # Linux package

# Test
./gradlew allTests                 # All tests
./gradlew :shared:commonTest       # Shared lib tests

# Utilities
./gradlew --stop                   # Stop Gradle daemon
./gradlew clean --refresh-dependencies # Force re-download
```

### Where to Write Code

| Goal | Path |
|---|---|
| Shared UI | `composeApp/src/commonMain/kotlin/` |
| Android specific | `androidApp/src/main/kotlin/` |
| Desktop specific | `composeApp/src/jvmMain/kotlin/` |
| Business logic (all platforms) | `shared/src/commonMain/kotlin/` |
| Platform-specific logic | `shared/src/<platform>Main/kotlin/` |
| Database schema | `shared/src/*/...data/db/` |
| Crypto (expect/actual) | `shared/src/*/...data/crypto/` |

### Coding Conventions

- **Kotlin style:** Use `kotlin.code.style=official`
- **State:** Prefer Kotlin `StateFlow` over mutable state
- **Crypto:** Always in ViewModels/managers, never in DAO/repository
- **Platform code:** Use `expect/actual` in `shared` module
- **Composables:** Prefer parameter-driven + state holders pattern

### Development Workflow

1. **Create feature branch:** `feature/my-feature`
2. **Update code** in appropriate module
3. **Run relevant Gradle task:** e.g., `./gradlew :composeApp:run`
4. **Test on multiple platforms** if applicable
5. **Commit with message** + details
6. **Open PR** with description of changes

### Areas That Need Careful Attention

⚠️ **Be careful with changes to:**
- `CryptoEngine` — Can make existing vaults undecryptable
- `PassworldPrefs` — Salt/verifier compatibility affects unlock
- `KeystoreManager` — Key migration can lock users out
- Room schema — Migrations not yet implemented
- Import/export format — Backup compatibility depends on version

---

## 🎯 Future Improvements (Priority Order)

### 🔴 P0 (Critical)

- [ ] Clipboard auto-clear timeout (30-60 seconds after copy)
- [ ] Android backup hardening: `android:allowBackup="false"` or backup rules
- [ ] Add comprehensive tests (crypto, repository, import/export)
- [ ] Room schema migrations (currently at v1, no migration path)

### 🟡 P1 (Important)

- [ ] Bind Android Keystore to user authentication
- [ ] Use OS-native secure storage (DPAPI, Keychain, libsecret)
- [ ] Password generator + strength meter
- [ ] Import merge policy (current: append only)
- [ ] Settings screen (timeout, theme, backup options)
- [ ] Explicit app-pause lock on Android

### 🟢 P2 (Nice to Have)

- [ ] Android Autofill service integration
- [ ] Audit log + password change history
- [ ] Complete iOS implementation
- [ ] Re-enable Web targets (JS/Wasm)

### 💡 P3 (Future Vision)

- [ ] Optional encrypted cloud sync (multi-device convenience)
- [ ] Threat modeling + penetration testing
- [ ] Hardware security key support

---

## 📝 Implementation Notes

- **UI name:** "Passworld Manager" (but Android string still says "Password Manager")
- **Package:** `com.sanket.tools.passwordmanager`
- **Database:** `password_manager.db` (Room, SQLite)
- **Server:** Ktor sample only—not used by vault for sync
- **Navigation:** `navigation-compose` declared but not used (uses session-based switching instead)
- **Export format:** JSON with AES-256-GCM encryption (not ZIP)
- **First build time:** 3-5 minutes (downloads Gradle, JDK, dependencies)

---

## 🔗 Related Documentation

- **Architecture & Design:** See PRD.md for exhaustive technical details
- **UI/UX:** Material 3 + MaterialKolor dynamic theming
- **Crypto:** AES-256-GCM + PBKDF2-SHA256 + platform keystores
- **Database:** Room 2.8.4 with SQLite 2.6.2

---

## ✏️ Quick Reference: Where to Write Code

| Goal | Location |
|---|---|
| Shared UI screens & components | `composeApp/src/commonMain/kotlin/` |
| Android-only code | `androidApp/src/main/kotlin/` |
| iOS-only Swift code | `iosApp/iosApp/` |
| Business logic (all platforms) | `shared/src/commonMain/kotlin/` |
| Platform-specific logic | `shared/src/<platform>Main/kotlin/` |
| Server API endpoints | `server/src/main/kotlin/` |
| Images & shared resources | `composeApp/src/commonMain/composeResources/` |
| Android resources | `androidApp/src/main/res/` |
| Version catalog | `gradle/libs.versions.toml` |

---

## 📄 License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

**Questions? Issues?** Check the [PRD.md](PRD.md) for comprehensive technical documentation.