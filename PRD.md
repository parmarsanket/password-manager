# Passworld Manager

## GitHub-Ready Badges

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.3-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android](https://img.shields.io/badge/Android-minSdk%2024%20%7C%20targetSdk%2036-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Gradle](https://img.shields.io/badge/Gradle-9.5.0-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Ktor](https://img.shields.io/badge/Ktor-3.4.2-087CFA)](https://ktor.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## Overview

Passworld Manager is an offline-first password and credential vault built with Kotlin Multiplatform and Compose Multiplatform. It stores flexible credential entries such as websites, bank accounts, SIM details, and any user-defined secure fields. Sensitive field values are encrypted locally before they are saved, and the app unlocks through a master password or platform device verification when available.

| Area | Current implementation |
|---|---|
| Product type | Local password manager / credential vault |
| Primary users | Individuals who want private local storage for passwords, PINs, account IDs, and recovery codes |
| Main problem solved | Securely storing varied credential records without forcing every entry into a fixed username/password schema |
| Active app targets | Android and Desktop JVM |
| Scaffolded / inactive targets | iOS, JS, and Wasm source files exist, but iOS and web targets are disabled or not fully wired in Gradle |
| Storage model | Local Room database plus platform preferences / keystore |
| Network requirement | None for the vault experience |

The project currently favors a local-first architecture: all core vault operations happen on-device, and the Ktor server module is a minimal sample endpoint rather than a sync backend.

---

## Feature Matrix

| Feature | Status | Implementation evidence |
|---|---:|---|
| First-run master password setup | Implemented | `UnlockViewModel.setupMasterPassword`, `UnlockMode.Setup` |
| Master password login | Implemented | `UnlockViewModel.login` |
| Android biometric / device credential unlock | Implemented | `BiometricManager.android.kt`, `BiometricPrompt`, `BIOMETRIC_STRONG or DEVICE_CREDENTIAL` |
| Desktop Windows Hello unlock | Implemented for Windows Desktop JVM | `BiometricManager.jvm.kt`, WinRT `UserConsentVerifier` via PowerShell |
| Session timeout / auto-lock | Implemented | `PassworldSession`, 5 minute timeout, 30 second check loop in `App.kt` |
| Add/edit/delete credential entries | Implemented | `AddEditViewModel`, `PasswordRepository`, `VaultEditorDialog`, `VaultDetailDialog` |
| Freeform credential fields | Implemented | `CredentialField`, `FieldState`, `FieldItem` |
| Secret reveal/hide | Implemented | `VaultDetailFieldCard`, `FieldItem`, `PasswordVisualTransformation` |
| Copy to clipboard | Implemented | `ClipboardManager.android.kt`, `ClipboardManager.jvm.kt` |
| Clipboard auto-clear | Not implemented | No timer or clear operation exists after copy |
| Search | Implemented | `PassworldViewModel.onSearch`, `VaultSearchField` |
| Categories as stored metadata | Not implemented | No category entity or column exists |
| Quick templates | Implemented | `CategoryTemplate.WEBSITE`, `BANK`, `SIM` in `AddEditViewModel` |
| Encrypted export/import | Implemented | `ExportCrypto`, `ExportManager`, `ImportManager`, `BackupFileGateway` |
| ZIP backup | Not implemented in current file gateway | Current gateways write/read JSON `ExportPackage` files |
| Navigation routes | Not implemented | Navigation dependency exists, but `App.kt` uses session-gated screen switching instead of `NavHost` |
| Adaptive mobile/tablet/desktop UI | Implemented | `adaptiveLayoutSpec`, single-pane/two-pane/sidebar layouts |
| Keyboard unlock flow | Implemented | `UnlockFormCard`, `UnlockScreenState`, arrow key focus movement, IME actions |
| Settings screen | Not implemented | `PassworldPrefs` stores setup metadata only; no settings UI |
| Password generator / strength meter | Not implemented | No generator or strength classes found |
| Android Autofill | Not implemented | No Autofill service or manifest service entry |
| Tests | Minimal | Placeholder common tests and one Ktor root endpoint test |

---

## Features

### Authentication

Passworld Manager has two authentication modes: first-run setup and returning-user login. The app decides which mode to show by reading `PassworldPrefs.isSetupComplete()` from preferences.

| Detail | Implementation |
|---|---|
| What it does | Creates or verifies the user's master password before exposing vault data |
| Why it exists | The master password derives the AES vault key used to encrypt and decrypt credential fields |
| User flow | Launch app -> setup/login screen -> enter master password -> vault key starts a `PassworldSession` -> vault dashboard appears |
| Technical implementation | PBKDF2-HMAC-SHA256 derives a 256-bit key from password + 16-byte salt; a verifier is stored in preferences |
| Related files | `UnlockViewModel.kt`, `CryptoEngine.kt`, `PassworldPrefs.kt`, `PassworldSession.kt`, `UnlockScreen.kt` |

First launch flow:

```text
App launch
  -> UnlockViewModel.refreshStatus()
  -> PassworldPrefs.isSetupComplete() == false
  -> UnlockMode.Setup
  -> user enters password and confirmation
  -> CryptoEngine.generateSalt()
  -> CryptoEngine.deriveVaultKey(password, salt)
  -> CryptoEngine.createPasswordVerifier(vaultKey)
  -> PassworldPrefs.saveMasterSecrets(salt, verifier)
  -> KeystoreManager.saveVaultKey(vaultKey)
  -> PassworldSession.start(vaultKey)
  -> VaultDashboardScreen
```

Returning login flow:

```text
User enters master password
  -> load salt + verifier from PassworldPrefs
  -> derive vault key with CryptoEngine
  -> compare verifier
  -> restore/migrate secure stored vault key if needed
  -> PassworldSession.start(sessionKey)
  -> render vault
```

The login path also includes legacy verifier support through `CryptoEngine.verifyPassword(...)`. If a legacy stored hash is accepted, the app upgrades it by saving the newer verifier format.

### Biometric And Device Verification Unlock

Device verification exists as a convenience unlock path after a vault key has already been saved by the master password flow.

| Platform | Behavior |
|---|---|
| Android | Uses `androidx.biometric.BiometricPrompt` with strong biometric or device credential |
| Desktop JVM on Windows | Uses Windows Hello through WinRT `UserConsentVerifier` invoked by hidden PowerShell scripts |
| Desktop JVM on non-Windows | `BiometricManager.shouldOfferAuthentication()` returns false |
| iOS | Placeholder file only; iOS target is not active |

Technical flow:

```text
Login mode
  -> shouldShowDeviceAuthOption()
  -> biometric.shouldOfferAuthentication()
  -> keystore.loadVaultKey() != null
  -> show biometric/device verification button
  -> biometric.authenticate(...)
  -> on success, load key from KeystoreManager
  -> PassworldSession.start(key)
```

Related files:

| Responsibility | File |
|---|---|
| Common auth contract | `shared/src/commonMain/.../data/crypto/BiometricManager.kt` |
| Android prompt | `shared/src/androidMain/.../data/crypto/BiometricManager.android.kt` |
| Windows Hello bridge | `shared/src/jvmMain/.../data/crypto/BiometricManager.jvm.kt` |
| Activity handoff for Android prompt | `ActivityProvider.android.kt`, `MainActivity.kt` |
| UI trigger | `UnlockFormCard.kt` |

### Session Locking

`PassworldSession` holds the vault key only in memory while the app is unlocked. The session has a 5 minute inactivity timeout.

| Detail | Implementation |
|---|---|
| Timeout duration | `5.minutes` in `PassworldSession` |
| Timeout check interval | Every 30 seconds in `App.kt` |
| Activity tracking | Root `Box` records pointer events and preview key events |
| Lock action | `session.stop()` fills the current key byte array with zeroes and clears the `StateFlow` |
| UI reaction | `App.kt` observes `session.passworldKey`; null key returns to `UnlockScreen` |

There is no immediate background-lock policy in the current Android lifecycle code. `MainActivity` tracks activity availability for prompts and file operations, while timeout enforcement stays in the Compose root.

### Password Management

Credentials are modeled as an entry header plus any number of encrypted fields. This is more flexible than a fixed username/password table because the same editor can store website accounts, bank PINs, SIM PUK codes, customer IDs, and custom secrets.

| User capability | Implementation |
|---|---|
| Add a credential | FAB opens `VaultDialog.AddEntry`, `AddEditViewModel.prepareNewEntry()` loads default fields |
| Edit a credential | Detail dialog calls `AddEditViewModel.loadEntry(entryId)` |
| Delete a credential | Detail dialog shows confirmation before `PassworldViewModel.deleteEntry(entryId)` |
| Add custom field | `AddEditViewModel.addField()` |
| Remove field | `AddEditViewModel.removeField(index)` |
| Mark secret/non-secret | `FieldState.isSecret`, stored as `CredentialField.isSecret` |
| Entry badge | User-provided short badge or fallback first letter / lock icon |

Technical implementation:

```text
VaultEditorDialog
  -> AddEditViewModel.save()
  -> normalize fields
  -> encrypt every field value with CryptoEngine.encryptField(...)
  -> PasswordRepository.saveEntry(...) or updateEntry(...)
  -> Room DAO persists password_entries + credential_fields
```

Related classes:

| Layer | Classes |
|---|---|
| Database | `PasswordEntry`, `CredentialField`, `EntryWithFields`, `PasswordDao`, `AppDatabase` |
| Repository | `PasswordRepository` |
| ViewModel | `AddEditViewModel`, `AddEditUiState`, `FieldState` |
| UI | `VaultDashboardScreen`, `VaultEditorDialog`, `FieldItem`, `VaultCredentialCard`, `VaultDetailDialog` |

### Search

Search filters entries by `siteOrApp` using a case-insensitive contains match in `PassworldViewModel.items`.

```kotlin
rawEntries.filter { it.entry.siteOrApp.contains(query, ignoreCase = true) }
```

The search field is shared across mobile, tablet, and desktop layouts through `VaultSearchField`. It uses `ImeAction.Search` and clears focus when the user submits from the keyboard.

Related files:

| File | Role |
|---|---|
| `PassworldViewModel.kt` | Owns `_searchQuery` and combines it with repository data |
| `VaultSearchField.kt` | Material 3 search input |
| `VaultExpandedSidebar.kt` | Desktop/tablet sidebar search |
| `CollapsiblePortraitLayout.kt` | Mobile portrait search row |
| `VaultPhoneLandscapeSidebar.kt` | Short landscape search |

### Templates

The app does not persist categories as data. Instead, it provides quick templates in the new-entry dialog.

| Template | Fields inserted |
|---|---|
| Website | Email, Username, Password |
| Bank | Account Number, MPIN, TPIN, ATM PIN |
| SIM | Mobile Number, PUK Code, MNP Code |

`AddEditViewModel.applyTemplate(...)` avoids adding duplicate labels that are already present in the editor state. Templates only appear for new entries, not during edit mode.

### Secret Reveal And Clipboard Copy

Secret fields are masked by default in the list and detail dialog. Users can reveal a secret field in `VaultDetailFieldCard`, and every detail field has a copy action.

| Behavior | Implementation |
|---|---|
| Masking in list | `VaultCredentialCard` shows bullet characters for secret preview values |
| Detail reveal state | `rememberSaveable(field.fieldId, field.label)` |
| Copy on Android | Android `ClipboardManager` with `ClipData.newPlainText(...)` |
| Copy on Desktop JVM | AWT `Toolkit.getDefaultToolkit().systemClipboard` |
| Clipboard clearing | Not implemented |

Security note: because clipboard auto-clear is not currently implemented, copied secrets remain in the system clipboard until overwritten by the OS or another app.

### Export And Import

The current backup implementation writes an encrypted JSON package, not a ZIP file. The package contains AES-GCM encrypted vault JSON plus metadata that includes a fresh export salt.

| Detail | Implementation |
|---|---|
| Export password | Entered in `BackupPasswordDialog`; used to derive a backup encryption key |
| Backup key derivation | `CryptoEngine.deriveVaultKey(backupPassword, freshSalt)` |
| Backup encryption | `ExportCrypto.encrypt(...)` using AES-256-GCM through `CryptoEngine.encryptField(...)` |
| Backup format | Pretty-printed JSON `ExportPackage(encryptedData, meta)` |
| Android file picker | `Intent.ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT` via `BackupFileActivityBridge` |
| Desktop file picker | AWT `FileDialog` |
| Import behavior | Appends imported entries and re-encrypts every field with the current device vault key |

Export flow:

```text
User chooses Export
  -> BackupPasswordDialog asks for backup password
  -> PassworldViewModel.exportVault(password)
  -> ExportManager reads Room entries
  -> decrypt field values using active PassworldSession key
  -> serialize ExportVault JSON
  -> ExportCrypto encrypts JSON with backup-password-derived key
  -> BackupFileGateway writes ExportPackage JSON
```

Import flow:

```text
User chooses Import
  -> BackupPasswordDialog asks for backup password
  -> BackupFileGateway reads ExportPackage JSON
  -> ExportCrypto decrypts encryptedData
  -> ImportManager parses ExportVault
  -> each imported field is encrypted with this device's active vault key
  -> PasswordRepository.saveEntry(...) appends records
```

Related files:

| Area | Files |
|---|---|
| Crypto package format | `ExportCrypto.kt`, `ExportPackage`, `ExportMeta`, `ExportVault`, `ExportEntry`, `ExportField` |
| Export business logic | `ExportManager.kt` |
| Import business logic | `ImportManager.kt` |
| Platform file IO | `BackupFileGateway.android.kt`, `BackupFileGateway.jvm.kt`, `BackupFileActivityBridge.android.kt` |
| UI flow | `VaultDashboardSupport.kt`, `VaultDashboardDialogs.kt` |

### Adaptive Layout

The UI uses a custom adaptive layout system rather than adding a separate window-size dependency. `adaptiveLayoutSpec(maxWidth, maxHeight)` evaluates both width and height so a rotated phone is not mistaken for a tablet.

| Posture | Width / height rule | Vault layout |
|---|---|---|
| PhonePortrait | Width < 600 dp | Single column with collapsible hero |
| PhoneLandscape | Width >= 600 dp and height < 480 dp | Compact sidebar/action strip plus content pane |
| TabletPortrait | 600-839 dp wide and height >= 480 dp | Single-column layout with larger content sizing |
| TabletLandscape | Width >= 840 dp and height >= 480 dp | Two-pane layout with sidebar and adaptive grid |

Related files:

| File | Role |
|---|---|
| `AdaptiveLayout.kt` | Breakpoints, posture detection, spacing, max widths |
| `SinglePaneLayout.kt` | Single-column vault layout wrapper |
| `CollapsiblePortraitLayout.kt` | Mobile portrait collapsing hero layout |
| `TwoPaneLayout.kt` | Sidebar plus content pane |
| `VaultSidebar.kt` | Expanded/collapsed sidebar behavior |
| `UnlockLayouts.kt` | Adaptive unlock screen layouts |

### Desktop Support

Desktop JVM is a first-class active target. It starts Koin before opening a Compose Desktop `Window` titled `Passworld Manager`.

| Desktop capability | Implementation |
|---|---|
| App entry point | `composeApp/src/jvmMain/.../main.kt` |
| Database path | `%LOCALAPPDATA%/PassworldManager/password_manager.db` on Windows, `~/.passworldmanager` elsewhere |
| Preferences | `SimpleDesktopPrefs` properties file to avoid DataStore locking issues in packaged builds |
| Keystore | PKCS12 file `.password_manager_keystore.p12` in app data directory |
| Device auth | Windows Hello bridge on Windows only |
| Backup picker | AWT `FileDialog` |
| Native packages | DMG, MSI, and DEB configured in `compose.desktop.nativeDistributions` |

### Keyboard Navigation

The unlock flow contains explicit keyboard handling:

| Interaction | File |
|---|---|
| Password field submits login on IME Done | `UnlockFormCard.kt` |
| Setup password Done moves to confirmation field | `UnlockFormCard.kt` |
| Confirmation Done creates vault access | `UnlockFormCard.kt` |
| Arrow up/down moves focus through unlock controls | `UnlockFormCard.kt` |
| Window focus restores password focus | `UnlockScreen.kt`, `UnlockScreenState.kt` |
| Any key event records session activity | `App.kt` |

The vault search field also uses `ImeAction.Search`. Desktop-like keyboard affordances are strongest on the unlock screen; the vault editor and dialogs mostly rely on standard Material text fields and buttons.

### Offline Support

The vault does not require a backend service. Room, DataStore/properties, keystore storage, import/export, search, and unlock all run locally. The `server` module is present but only exposes a sample root route:

```text
GET / -> "Ktor: Hello, <platform>!"
```

No sync, account system, cloud backup, or remote API is implemented.

### Animations And Motion

Motion is implemented through Compose animation APIs and Material 3 Expressive theme support.

| Motion | Implementation |
|---|---|
| Dynamic theme animation | `DynamicMaterialExpressiveTheme(..., animate = true)` |
| Sidebar width animation | `animateDpAsState` with spring in `VaultSidebar` |
| Sidebar content transition | `AnimatedContent`, `fadeIn`, `fadeOut`, `SizeTransform` |
| Mobile hero collapse | `HeroScrollState`, nested scroll, spring settle animation |
| Hero effects | Alpha, translation, scale, and height collapse derived from scroll progress |

---

## Architecture

### High-Level Architecture Diagram

```text
Android App / Desktop JVM App
        |
        v
composeApp common UI
  - App()
  - UnlockScreen
  - VaultDashboardScreen
        |
        v
shared ViewModels
  - UnlockViewModel
  - PassworldViewModel
  - AddEditViewModel
        |
        v
shared domain + data layer
  - CredentialItem / DecryptedField
  - PasswordRepository
  - ExportManager / ImportManager
        |
        v
Room DAO + Crypto + Preferences + Keystore
  - PasswordDao / AppDatabase
  - CryptoEngine expect/actual
  - KeystoreManager expect/actual
  - PassworldPrefs
```

### Architecture Summary Table

| Layer | Responsibility | Key files |
|---|---|---|
| App entry | Platform startup and dependency registration | `PasswordManagerApp.kt`, `MainActivity.kt`, `composeApp/src/jvmMain/.../main.kt` |
| UI shell | Chooses unlock or vault based on session key | `App.kt` |
| Screens | Compose UI for unlock and vault workflows | `UnlockScreen.kt`, `VaultDashboardScreen.kt` |
| State holders | Collect ViewModel flows, own UI dialog state, wire events | `VaultDashboardScreen.kt`, `UnlockScreenState.kt` |
| ViewModels | Business-facing state and user actions | `UnlockViewModel`, `PassworldViewModel`, `AddEditViewModel` |
| Domain model | Decrypted in-memory UI model | `CredentialItem`, `DecryptedField` |
| Repository | Single source of truth for Room CRUD | `PasswordRepository` |
| Persistence | Room entities, DAO, DB factory | `PasswordEntry`, `CredentialField`, `PasswordDao`, `AppDatabase`, `DatabaseFactory` |
| Security | Crypto, keystore, session, device verification | `CryptoEngine`, `KeystoreManager`, `PassworldSession`, `BiometricManager` |
| Import/export | Backup package creation, encryption, file IO | `ExportCrypto`, `ExportManager`, `ImportManager`, `BackupFileGateway` |
| Dependency injection | Koin modules and platform bindings | `AppModule.kt`, `PasswordManagerApp.kt`, desktop `main.kt` |

### Project Structure

The Gradle project includes four modules:

| Module | Purpose | Current status |
|---|---|---|
| `androidApp` | Android application shell and manifest | Active |
| `composeApp` | Shared Compose Multiplatform UI plus Desktop entry | Active for Android/desktop UI; JS/Wasm blocks are commented |
| `shared` | Business logic, data, crypto contracts, repositories, ViewModels | Active for Android and JVM |
| `server` | Minimal Ktor server sample | Active but not used by the vault |

### MVVM Usage

The project uses a pragmatic MVVM structure:

| ViewModel | Responsibilities |
|---|---|
| `UnlockViewModel` | Determines setup/login mode, validates passwords, derives vault key, handles biometric unlock, updates unlock UI state |
| `PassworldViewModel` | Combines repository entries, search query, and session key into decrypted `CredentialItem` UI models; handles delete, logout, export/import calls |
| `AddEditViewModel` | Owns editor state, loads/decrypts existing entries, applies templates, encrypts values before saving |

ViewModels expose `StateFlow` and keep suspend work inside `viewModelScope`. Compose screens collect state with `collectAsState()`.

### State Management

State is split by lifetime:

| State type | Used for | Examples |
|---|---|---|
| `StateFlow` | Business/UI state from ViewModels | `UnlockUiState`, `PassworldViewModel.items`, `AddEditUiState` |
| `MutableStateFlow` | ViewModel-owned mutable state | `_searchQuery`, `_uiState`, `_passworldKey` |
| `remember` | Ephemeral composition state | snackbar host, coroutine scope, sidebar expanded state |
| `rememberSaveable` | UI state that should survive configuration changes | `VaultDialog`, dialog passwords, secret reveal state, unlock text field state |
| `derivedStateOf` | Computed values that should not recalculate unnecessarily | selected item, total secret count, hero collapse values |

### Navigation Structure

The project declares `navigation-compose`, but the current app does not define `NavHost`, routes, or destination composables. Navigation is session-gated:

```text
App()
  -> collect PassworldSession.passworldKey
  -> if key == null: UnlockScreen
  -> else: VaultDashboardScreen
```

Within the vault, flows are dialog-driven through the sealed `VaultDialog` interface:

```text
VaultDialog
  - AddEntry
  - EditEntry(entryId)
  - ViewEntry(entryId)
  - ExportBackup
  - ImportBackup
```

### Repository Pattern

`PasswordRepository` sits between ViewModels and `PasswordDao`. It intentionally does not encrypt or decrypt values; encryption is handled by ViewModels through `CryptoEngine` before calling the repository. This separation keeps database access simple and keeps cryptographic responsibilities explicit.

Write behavior:

| Operation | Repository behavior |
|---|---|
| Save new entry | Insert `PasswordEntry`, then insert fields with the generated entry ID |
| Update entry | Update header, delete old fields, insert fresh fields |
| Delete entry | Delete header; `CredentialField` rows are removed by Room foreign key cascade |
| Clear all | `dao.clearAll()` exists but is not used by current import UI |

### Data Flow

```text
Room Flow<List<EntryWithFields>>
  + search query StateFlow
  + session key StateFlow
        |
        v
PassworldViewModel.items combine(...)
        |
        v
decrypt each field with CryptoEngine
        |
        v
StateFlow<List<CredentialItem>>
        |
        v
VaultDashboardScreen -> VaultContentPane -> VaultCredentialCard
```

No decrypted credential field is written back to Room. Decrypted values exist in memory as `String` values in `CredentialItem`, editor state, detail dialogs, and temporary export JSON.

### Event Handling

The vault dashboard follows a clear state-holder pattern:

```text
VaultDashboardScreen
  - collects ViewModel state
  - owns snackbar state
  - owns active VaultDialog
  - injects BackupFileGateway and ClipboardManager
  - passes plain values and callbacks to VaultDashboardContent

VaultDashboardContent
  - stateless UI
  - calls callbacks
  - renders layout and dialogs
```

This makes most UI components reusable and easier to preview because they do not call Koin or collect flows directly.

### Dependency Injection

Koin provides the app graph.

Common modules in `AppModule.kt`:

| Module | Provides |
|---|---|
| `databaseModule` | `AppDatabase`, `PasswordDao` |
| `cryptoModule` | `CryptoEngine`, `PassworldSession`, `ExportCrypto` |
| `repositoryModule` | `PassworldPrefs`, `PasswordRepository`, `ExportManager`, `ImportManager` |
| `viewModelModule` | `PassworldViewModel`, `UnlockViewModel`, `AddEditViewModel` |

Platform modules:

| Platform | File | Platform bindings |
|---|---|---|
| Android | `PasswordManagerApp.kt` | `ActivityProvider`, `BackupFileActivityBridge`, `DatabaseFactory(context)`, `KeystoreManager(context)`, `PassworldPrefsFactory(context)`, `ClipboardManager(context)`, `BiometricManager(context, activityProvider)`, `BackupFileGateway(...)` |
| Desktop JVM | `composeApp/src/jvmMain/.../main.kt` | `DatabaseFactory`, `KeystoreManager`, `PassworldPrefsFactory`, `ClipboardManager`, `BiometricManager`, `BackupFileGateway` |

`initKoin(platformModule)` guards against starting Koin more than once by checking `GlobalContext.getOrNull()`.

---

## UI/UX Design

### Material 3 And Theme

The UI is built with Compose Material 3 and MaterialKolor.

| Detail | Implementation |
|---|---|
| Theme entry | `AppTheme` |
| Dynamic theme engine | `DynamicMaterialExpressiveTheme` |
| Seed color | `SeedColor = Color(0xFF535A97)` |
| Palette style | `PaletteStyle.Vibrant` |
| Color spec | `ColorSpec.SpecVersion.SPEC_2025` |
| Motion | `MotionScheme.expressive()` |
| Dark theme | `isSystemInDarkTheme()` |

The codebase uses Material components such as `Scaffold`, `Surface`, `Card`, `OutlinedTextField`, `SuggestionChip`, `FloatingActionButton`, `AlertDialog`, and `FilledTonalButton`.

### Responsive UI

The most important UI decision is that width and height are evaluated together. This prevents a phone in landscape from receiving a desktop/tablet two-pane experience that would be too tall for the available height.

```text
BoxWithConstraints
  -> adaptiveLayoutSpec(maxWidth, maxHeight)
  -> posture + spacing + max widths + two-pane decision
  -> screen-specific layout branch
```

### Vault Layout Behavior

| Device/window posture | UI behavior |
|---|---|
| Phone portrait | Collapsible hero at top, search row, list below |
| Phone landscape | Compact action/search sidebar plus content pane |
| Tablet portrait | Single-column constrained layout with larger sizing |
| Tablet landscape / Desktop | Expandable sidebar plus adaptive staggered grid |

The credential list uses a single `LazyColumn` on compact width and a `LazyVerticalStaggeredGrid` on medium/expanded width.

### Unlock Layout Behavior

| Posture | Unlock layout |
|---|---|
| Phone portrait / tablet portrait | Centered form card |
| Phone landscape | Compact brand strip plus form |
| Tablet landscape / desktop | Branding panel plus form card |

The unlock form uses `imePadding()` and scrollable content so the form remains usable when the software keyboard is visible.

### Accessibility Support

Current accessibility support comes mainly from standard Material components and text labels:

| Supported | Details |
|---|---|
| Text field labels | Master Password, Confirm Password, Site or app, Badge, Backup password |
| Action descriptions | FAB has `Add entry`; copy buttons use `Copy`; rail buttons use labels such as Import, Export, Lock |
| Keyboard actions | IME Done/Search support on key fields |
| Touch targets | Material buttons and icon buttons provide standard target sizing |
| Visual masking | Secret values are masked and can be revealed intentionally |

Known accessibility gaps:

| Gap | Recommended improvement |
|---|---|
| Several reveal/hide icons use `contentDescription = null` | Add "Show secret" / "Hide secret" descriptions |
| Some icon + text buttons set icon descriptions to null | Usually acceptable because visible text labels the button, but verify with screen readers |
| No custom semantic headings or traversal order | Add semantics for large dialogs and cards if accessibility testing requires it |
| Error text is visible but not explicitly announced | Add live region semantics for unlock and backup errors |

---

## Security

### Security Architecture

```text
Master password
  -> PBKDF2-HMAC-SHA256, 200,000 iterations, 16-byte salt
  -> 256-bit vault key
  -> stored in memory during session
  -> wrapped/stored by platform KeystoreManager for device verification unlock
  -> encrypts/decrypts every CredentialField.encryptedValue
```

### Encryption Approach

`CryptoEngine` is an `expect` class with Android and JVM actual implementations using `javax.crypto`.

| Property | Current value |
|---|---|
| Field encryption | AES/GCM/NoPadding |
| Key length | 256 bits |
| IV size | 12 bytes |
| Auth tag | 128 bits |
| Stored field format | Base64(IV + ciphertext + auth tag) |
| Salt size | 16 bytes |
| Vault key derivation | PBKDF2WithHmacSHA256, 200,000 iterations |
| Legacy password hash | PBKDF2WithHmacSHA256, 100,000 iterations |
| Current password verifier | SHA-256 of derived vault key |

Every field encryption call generates a fresh random IV through `SecureRandom`.

### Stored Data Classification

| Data | Storage | Protection |
|---|---|---|
| `PasswordEntry.siteOrApp` | Room | Plain text metadata for display/search |
| `PasswordEntry.iconEmoji` | Room | Plain text UI metadata |
| `PasswordEntry.createdAt/updatedAt` | Room | Plain text metadata |
| `CredentialField.fieldLabel` | Room | Plain text label for UI |
| `CredentialField.encryptedValue` | Room | AES-GCM encrypted |
| Master salt | DataStore/properties | Base64 plain text, not secret |
| Master verifier | DataStore/properties | Hash/verifier string |
| Vault key | RAM during session | ByteArray in `PassworldSession` |
| Saved vault key | Platform secure storage abstraction | Android Keystore-wrapped bytes or Desktop PKCS12 entry |
| Master password | Not stored | Used only during setup/login derivation |

### Keystore Strategy

| Platform | Current implementation |
|---|---|
| Android | Creates an AES key in `AndroidKeyStore`, wraps the vault key with AES-GCM, stores wrapped key + IV in `SharedPreferences` |
| Desktop JVM | Stores vault key as a PKCS12 `SecretKeyEntry` in app data directory, protected by a password derived from the OS username |
| iOS | Placeholder only; target inactive |

Security nuance: Android device verification is enforced by the app before loading the saved vault key. The Android wrapping key is not configured in code with `setUserAuthenticationRequired(...)`, so a production hardening pass should bind keystore key usage to user authentication if the intended threat model requires it.

### Biometric Security

Biometric unlock does not replace the master password. It is a gate that allows the app to load the previously saved vault key from platform storage.

```text
Biometric / Windows Hello success
  -> KeystoreManager.loadVaultKey()
  -> PassworldSession.start(key)
```

If the secure key is missing, `UnlockViewModel` falls back to master password login and saves the derived key again after successful login.

### Session Security

`PassworldSession.stop()` attempts to reduce key lifetime by filling the in-memory key byte array with zeroes before clearing the `StateFlow`. This protects the vault key object held by the session, but decrypted `String` values may still exist temporarily in Compose state, domain models, or export/import buffers until garbage collected.

### Backup Security

Export/import uses a separate backup password and fresh salt for each export. The backup file stores:

```text
ExportPackage JSON
  encryptedData: AES-GCM encrypted ExportVault JSON
  meta: JSON string containing version, exportedAt, salt, appVersion
```

The export salt is not secret; it is required to derive the backup key during import. The backup file does not contain the master password or the vault key.

### Security Checklist

| Item | Status |
|---|---:|
| Master password is not stored | Pass |
| Sensitive credential values are encrypted before Room persistence | Pass |
| AES-GCM uses a fresh random IV per field | Pass |
| Vault key is held only while session is unlocked | Pass |
| Session key is zero-filled on lock | Pass |
| PBKDF2 is used for key derivation | Pass |
| Device verification unlock requires a saved vault key | Pass |
| Import re-encrypts data with local vault key | Pass |
| Clipboard auto-clear | Gap |
| Android manifest backup hardening | Gap: `android:allowBackup="true"` is currently set |
| Android Keystore auth-bound key usage | Gap: wrapping key is not configured with user-auth-required flags |
| Desktop secure storage hardening | Gap: PKCS12 + username-derived password is weaker than OS-native DPAPI/macOS Keychain/libsecret |
| Explicit background lock on app pause | Gap |
| Dedicated security tests | Gap |

---

## Technical Stack

| Category | Technology / version | Where declared |
|---|---|---|
| Language | Kotlin 2.3.21 | `gradle/libs.versions.toml` |
| Build wrapper | Gradle 9.5.0 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 9.2.1 | `gradle/libs.versions.toml` |
| Compose Multiplatform | 1.10.3 | `gradle/libs.versions.toml` |
| Compose Material 3 | 1.10.0-alpha05 | `gradle/libs.versions.toml` |
| MaterialKolor | 5.0.0-alpha07 | `gradle/libs.versions.toml` |
| Lifecycle ViewModel | 2.10.0 | `gradle/libs.versions.toml` |
| Coroutines | 1.10.2 | `gradle/libs.versions.toml` |
| Room | 2.8.4 | `gradle/libs.versions.toml` |
| SQLite bundled driver | 2.6.2 | `gradle/libs.versions.toml` |
| KSP | 2.3.5 | `gradle/libs.versions.toml` |
| Koin | 4.2.1 | `gradle/libs.versions.toml` |
| DataStore Preferences | 1.2.1 | `gradle/libs.versions.toml` |
| Kotlin serialization | 1.11.0 | `gradle/libs.versions.toml` |
| Navigation Compose | 2.9.2 | Declared dependency; not used for current navigation |
| AndroidX Biometric | 1.2.0-alpha05 | Android biometric implementation |
| Ktor | 3.4.2 | `server` module |
| Logging | Logback 1.5.32 | `server` module |
| Android SDK | min 24, target 36, compile 36 | Gradle config |
| JVM toolchain | 21 | `composeApp` and `androidApp` Gradle config |

---

## Folder Structure

```text
Password-Manager/
  androidApp/
    src/main/
      AndroidManifest.xml
      kotlin/com/sanket/tools/passwordmanager/
        PasswordManagerApp.kt
        MainActivity.kt
      res/
        values/strings.xml

  composeApp/
    src/commonMain/kotlin/com/sanket/tools/passwordmanager/
      App.kt
      theme/
        Theme.kt
        Color.kt
      ui/
        layout/
          AdaptiveLayout.kt
        component/
          EmptyState.kt
          FieldItem.kt
          TemplateChip.kt
          VaultEntryBadge.kt
          VaultFieldPreview.kt
          VaultSearchField.kt
          VaultStatChip.kt
        screen/unlock/
          UnlockScreen.kt
          UnlockScreenState.kt
          UnlockLayouts.kt
          UnlockFormCard.kt
        screen/vault/
          VaultDashboardScreen.kt
          VaultDashboardSupport.kt
          VaultDashboardDialogs.kt
          hero/
          layout/
          list/
          scroll/
          sidebar/
    src/jvmMain/kotlin/
      main.kt
    src/iosMain/kotlin/
      MainViewController.kt
    src/webMain/kotlin/
      main.kt

  shared/
    src/commonMain/kotlin/com/sanket/tools/passwordmanager/
      Constants.kt
      Greeting.kt
      Platform.kt
      data/
        crypto/
        db/
        export/
        prefs/
        repository/
      di/
        AppModule.kt
      domain/model/
        CredentialItem.kt
      ui/
        util/
        viewmodel/
    src/androidMain/kotlin/
      platform actuals for crypto, DB, prefs, export, clipboard
    src/jvmMain/kotlin/
      platform actuals for desktop crypto, DB, prefs, export, clipboard
    src/iosMain/kotlin/
      inactive placeholder actuals
    schemas/
      Room schema export

  server/
    src/main/kotlin/com/sanket/tools/passwordmanager/
      Application.kt
    src/test/kotlin/
      ApplicationTest.kt

  gradle/
    libs.versions.toml
    wrapper/gradle-wrapper.properties

  build.gradle.kts
  settings.gradle.kts
  gradle.properties
  PRD.md
```

Folder responsibilities:

| Folder | Purpose |
|---|---|
| `androidApp` | Android application class, Android activity, manifest, app resources |
| `composeApp/src/commonMain` | Shared Compose UI used by Android and Desktop |
| `composeApp/src/jvmMain` | Desktop application entry point |
| `shared/src/commonMain` | Platform-independent business logic, data models, ViewModels, crypto contracts |
| `shared/src/androidMain` | Android actual implementations for secure storage, DB, biometric, file picker, clipboard |
| `shared/src/jvmMain` | Desktop actual implementations for secure storage, DB, Windows Hello, file picker, clipboard |
| `shared/src/iosMain` | Placeholder inactive iOS actual files |
| `server` | Minimal Ktor server sample |
| `gradle` | Version catalog and wrapper configuration |

---

## Core Workflows

### App Launch Flow

```text
Android:
  PasswordManagerApp.onCreate()
    -> initKoin(android platform module)
  MainActivity.onCreate()
    -> enableEdgeToEdge()
    -> ActivityProvider.setCurrentActivity(this)
    -> setContent { App() }

Desktop:
  main()
    -> initKoin(desktop platform module)
    -> Window(title = "Passworld Manager") { App() }

App():
  -> collect PassworldSession.passworldKey
  -> refresh UnlockViewModel status
  -> start timeout loop
  -> render UnlockScreen or VaultDashboardScreen
```

### Unlock Flow

```text
UnlockScreen
  -> collect UnlockUiState
  -> render setup/login form
  -> user submits password
  -> UnlockViewModel validates input
  -> crypto work runs on Dispatchers.Default
  -> PassworldSession receives key
  -> App switches to VaultDashboardScreen
```

### Password Creation Flow

```text
User taps FAB
  -> activeDialog = VaultDialog.AddEntry
  -> AddEditViewModel.prepareNewEntry()
  -> VaultEditorDialog opens
  -> user edits site, badge, fields, templates
  -> Save
  -> AddEditViewModel.save()
  -> encrypt field values
  -> PasswordRepository.saveEntry()
  -> Snackbar: "Entry added."
```

### Password Retrieval Flow

```text
User taps a credential card
  -> activeDialog = VaultDialog.ViewEntry(entryId)
  -> selectedItem derived from decrypted items
  -> VaultDetailDialog opens
  -> fields are shown masked or plain based on isSecret
  -> user can reveal/copy/edit/delete
```

### Biometric Flow

```text
Returning user
  -> Login mode visible immediately
  -> background availability check runs
  -> if supported and saved key exists, show device verification
  -> optional auto-prompt runs
  -> AuthResult.Success loads vault key
  -> PassworldSession.start(key)
```

### Search Flow

```text
User types in VaultSearchField
  -> PassworldViewModel.onSearch(query)
  -> _searchQuery StateFlow updates
  -> combine(repository entries, search query, session key)
  -> filter by siteOrApp
  -> decrypt matching entries
  -> UI recomposes list/grid
```

### Adaptive Layout Behavior

```text
BoxWithConstraints
  -> maxWidth + maxHeight
  -> adaptiveLayoutSpec(...)
  -> widthClass + posture
  -> choose:
       PhonePortrait: CollapsiblePortraitLayout
       PhoneLandscape: TwoPaneLayout with compact sidebar
       TabletPortrait: SinglePaneLayout
       TabletLandscape/Desktop: TwoPaneLayout with expandable sidebar
```

---

## Performance Optimizations

| Optimization | Where | Why it matters |
|---|---|---|
| Room `Flow` queries | `PasswordDao.getAllEntries()` | UI reacts to database changes without manual refresh |
| `stateIn(..., SharingStarted.WhileSubscribed(5000))` | `PassworldViewModel.items` | Keeps decrypted list state active only while observed |
| Background crypto work | `UnlockViewModel` with `Dispatchers.Default` | Avoids blocking UI during PBKDF2 and key operations |
| Lazy list | `VaultContentPane` compact layout | Efficient single-column rendering |
| Lazy staggered grid | `VaultContentPane` medium/expanded layout | Efficient adaptive card grid |
| Stable item keys | `items(..., key = { it.entryId })` | Helps Compose preserve item identity |
| `derivedStateOf` | selected item, secret count, hero collapse values | Avoids redundant calculations during recomposition |
| `@Stable` / `@Immutable` state classes | `HeroScrollState`, `HeroCollapseUiState`, `AdaptiveLayoutSpec` | Communicates stable state contracts to Compose |
| Stateless UI split | `VaultDashboardContent` | Reduces direct ViewModel reads in child components |
| Nested scroll consumption | `HeroScrollState` + `rememberHeroScrollConnection` | Smooth collapsible hero without over-scrolling the list |

Recomposition strategy:

```text
State holders collect flows
  -> compute small derived values
  -> pass immutable/plain values down
  -> child composables render from parameters
  -> lazy containers use stable keys
```

Current performance tradeoff: `PassworldViewModel.items` decrypts all fields for all search-matching entries whenever repository data, search query, or session key changes. This is simple and acceptable for small local vaults, but large vaults could benefit from decrypt-on-detail or cached decrypted previews.

---

## Developer Guide

### Requirements

| Tool | Requirement |
|---|---|
| IDE | Android Studio with Kotlin Multiplatform / Compose support recommended |
| JDK | Gradle toolchain resolves JDK 21 where configured |
| Android SDK | compileSdk 36, minSdk 24 |
| Gradle | Use the wrapper only |

### Build And Run

Windows PowerShell:

```powershell
.\gradlew.bat --stop
.\gradlew.bat clean
.\gradlew.bat :androidApp:assembleDebug
.\gradlew.bat :composeApp:run
.\gradlew.bat :server:run
.\gradlew.bat allTests
```

macOS/Linux:

```bash
./gradlew --stop
./gradlew clean
./gradlew :androidApp:assembleDebug
./gradlew :composeApp:run
./gradlew :server:run
./gradlew allTests
```

Useful outputs:

| Command | Output |
|---|---|
| `:androidApp:assembleDebug` | Android debug APK |
| `:composeApp:run` | Desktop app window |
| `:composeApp:packageMsi` | Windows installer |
| `:composeApp:packageDmg` | macOS installer |
| `:composeApp:packageDeb` | Linux package |
| `:server:run` | Ktor server on port `8080` |

### Coding Style

| Area | Project convention |
|---|---|
| Kotlin style | `kotlin.code.style=official` |
| Dependency versions | Centralized in `gradle/libs.versions.toml` |
| Gradle usage | Always use wrapper scripts, not system Gradle |
| UI state | Prefer state holders and parameter-driven child composables |
| Crypto boundary | Encrypt/decrypt in ViewModels or managers, not in DAO/repository |
| Platform APIs | Use `expect/actual` in `shared` for crypto, DB, prefs, export, clipboard |
| UI components | Keep reusable components in `composeApp/src/commonMain/.../ui/component` |

### Contribution Guide

Recommended workflow:

```text
1. Create a focused branch.
2. Keep changes scoped to one feature or fix.
3. Update tests when changing crypto, storage, import/export, or ViewModel behavior.
4. Run the relevant Gradle task before opening a PR.
5. Document user-visible behavior changes in PRD.md or README.md.
```

Suggested branch naming:

| Branch type | Example |
|---|---|
| Feature | `feature/password-generator` |
| Bug fix | `fix/clipboard-clear-timeout` |
| Security | `security/android-backup-rules` |
| Documentation | `docs/prd-refresh` |
| Refactor | `refactor/vault-dialog-state` |

### Areas To Be Careful With

| Area | Reason |
|---|---|
| `CryptoEngine` | Changes can make existing vault data undecryptable |
| `PassworldPrefs` | Salt/verifier compatibility affects unlock |
| `KeystoreManager` | Platform key migration can lock users out |
| Room schema | Version is currently `1`; migrations are not implemented |
| Import/export schema | Backup compatibility depends on `ExportVault.version` and metadata |
| Session handling | Key lifetime and auto-lock are security-sensitive |

---

## Testing Strategy

### Existing Tests

| Module | File | Current coverage |
|---|---|---|
| `shared` | `SharedCommonTest.kt` | Placeholder arithmetic assertion |
| `composeApp` | `ComposeAppCommonTest.kt` | Placeholder arithmetic assertion |
| `server` | `ApplicationTest.kt` | Verifies `GET /` returns OK and greeting text |

### Recommended Test Coverage

| Area | Tests to add |
|---|---|
| Crypto | AES-GCM round trip, wrong key failure, salt uniqueness, PBKDF2 deterministic output for same input |
| Preferences | Save/load salt and verifier, setup complete flow |
| Repository | Save/update/delete entries and cascading fields |
| ViewModels | Setup validation, login success/failure, search filtering, template application |
| Import/export | Export/import round trip, wrong backup password failure, append behavior |
| Session | Timeout, activity refresh, key zeroing behavior |
| UI | Unlock keyboard actions, dialog state restoration, delete confirmation |
| Security regression | Ensure no plaintext field values are stored in `credential_fields.encryptedValue` |

---

## Future Improvements

These improvements are not present in the current implementation but are realistic next steps.

| Priority | Improvement | Reason |
|---:|---|---|
| P0 | Set `android:allowBackup="false"` or define secure backup rules | Prevent OS backup from copying sensitive app files unintentionally |
| P0 | Add clipboard auto-clear | Reduce exposure after copying passwords |
| P0 | Add crypto/repository/import-export tests | Protect against vault data loss and security regressions |
| P0 | Add Room migrations before schema changes | Avoid destructive upgrades |
| P1 | Bind Android Keystore wrapping key to user authentication | Strengthen biometric/device credential security boundary |
| P1 | Use OS-native Desktop secure storage | Replace username-derived PKCS12 protection with DPAPI, macOS Keychain, or libsecret |
| P1 | Add password generator and strength meter | Common password manager expectation |
| P1 | Add import merge/duplicate policy | Current import appends entries without deduplication |
| P1 | Add Settings screen | Make timeout, backup, theme, and security options visible |
| P1 | Add explicit background lock option | Improves security for mobile multitasking |
| P2 | Add Android Autofill service | Makes vault useful outside the app |
| P2 | Add audit fields and password history | Helps users track changes |
| P2 | Re-enable and complete iOS implementation | iOS files are placeholders today |
| P2 | Re-enable and wire web targets if needed | JS/Wasm targets are currently commented and not Koin-wired |
| P3 | Optional encrypted cloud sync | Multi-device convenience without abandoning local encryption |

---

## Implementation Notes

- The current product name used in UI is `Passworld Manager`; the Android string resource still says `Password Manager`.
- The package name is `com.sanket.tools.passwordmanager`.
- The Room database name is `password_manager.db`.
- The active vault does not use the Ktor server for sync or authentication.
- `navigation-compose` is declared but not used for runtime navigation.
- `NatoKeyGenerator.kt` is intentionally a placeholder; NATO-word backup is not implemented.
- The export implementation uses backup-password-derived AES-GCM encryption and JSON `ExportPackage` files.
