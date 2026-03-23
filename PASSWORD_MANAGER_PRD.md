# 🔐 Password Manager — KMP PRD (Final)

> **One-Line Principle:**
> _"Password creates the key, biometric unlocks it, data stays encrypted."_

---

## 1. Project Overview

Build a **secure, cross-platform Password Manager** using **Kotlin Multiplatform (KMP)** targeting **Android**, **Desktop (JVM)**, and **iOS**.

### Existing Stack
| Component | Library |
|---|---|
| UI | Compose Multiplatform 1.10.3 |
| Database | Room 2.8.4 + SQLite Bundled 2.6.2 |
| DI | Koin 4.2.0 |
| Build | AGP 9.0.1 · Kotlin 2.3.20 · KSP 2.3.5 |
| Targets | Android · JVM Desktop · iOS |

### Design Goals
- 🔐 **Strong encryption** — AES-256-GCM, hardware-backed key storage
- ⚡ **Easy UX** — Biometric daily unlock, no password typing every time
- 📦 **Secure cross-device backup** — NATO 15-word Export Key

---

## 2. Core Features

| # | Feature | Priority |
|---|---|---|
| 1 | Freeform credential storage (any number of custom fields) | P0 |
| 2 | AES-256-GCM field-level encryption | P0 |
| 3 | Master password (first launch) → PBKDF2 → Vault Key | P0 |
| 4 | Biometric unlock (Fingerprint / Face ID) for daily use | P0 |
| 5 | Auto-lock after 5–10 min timeout | P0 |
| 6 | Add / Edit / Delete credential entries | P0 |
| 7 | Export vault → encrypted ZIP with 15-word NATO key | P0 |
| 8 | Import vault → decrypt ZIP → re-encrypt with local Vault Key | P0 |
| 9 | Search / filter entries | P1 |
| 10 | Copy field to clipboard (auto-clear after 30 s) | P1 |
| 11 | Show / hide secret field values | P1 |
| 12 | Category templates (Website, Bank, SIM, Custom) | P1 |
| 13 | Password strength indicator | P2 |
| 14 | Random secure password generator | P2 |

---

## 3. Data Model — Flexible Freeform Fields

Each vault entry has a **dynamic list of fields** the user defines freely — no fixed email/username/password columns. One entry can hold `Email + Password`, another can hold `Customer ID + MPIN + TPIN + ATM PIN`.

### 3.1 Room Entity — `PasswordEntry`
```kotlin
@Entity(tableName = "password_entries")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteOrApp: String,   // plain text — e.g. "GitHub", "HDFC Bank"
    val iconEmoji: String,   // optional emoji — e.g. "🏦", "💻"
    val createdAt: Long,     // epoch ms
    val updatedAt: Long      // epoch ms
)
```

### 3.2 Room Entity — `CredentialField`
```kotlin
@Entity(
    tableName = "credential_fields",
    foreignKeys = [ForeignKey(
        entity = PasswordEntry::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("entryId")]
)
data class CredentialField(
    @PrimaryKey(autoGenerate = true) val fieldId: Long = 0,
    val entryId: Long,           // FK → PasswordEntry.id
    val fieldLabel: String,      // plain text — "Email", "TPIN", "MNP", "ATM PIN"
    val encryptedValue: String,  // AES-256-GCM encrypted, Base64(IV + ciphertext + tag)
    val isSecret: Boolean,       // true = show ●●●● by default
    val sortOrder: Int
)
```

### 3.3 DAO Relation — `EntryWithFields`
```kotlin
data class EntryWithFields(
    @Embedded val entry: PasswordEntry,
    @Relation(parentColumn = "id", entityColumn = "entryId")
    val fields: List<CredentialField>
)
```

### 3.4 What is Plain vs Encrypted in the DB

| Column | Visible? | Reason |
|---|---|---|
| `siteOrApp` | ✅ Plain text | Needed for search |
| `fieldLabel` | ✅ Plain text | Needed for UI display |
| `iconEmoji` | ✅ Plain text | UI only |
| `timestamps` | ✅ Plain text | Metadata only |
| `encryptedValue` | ❌ Encrypted | Password, PIN, Email — all sensitive values |

### 3.5 Real-World Examples

| Entry | Field Label | Value (decrypted) | isSecret |
|---|---|---|---|
| GitHub | Email | sanket@gmail.com | false |
| GitHub | Password | abc123! | true |
| HDFC Bank | Customer ID | 123456 | false |
| HDFC Bank | MPIN | 9876 | true |
| HDFC Bank | TPIN | 4521 | true |
| HDFC Bank | ATM PIN | 1234 | true |
| Jio SIM | Mobile Number | 9876543210 | false |
| Jio SIM | MNP Code | JIOMNP12 | true |

### 3.6 Category Templates

| Template | Fields Pre-filled |
|---|---|
| **Website / App** | Email, Username, Password |
| **Bank Account** | Account Number, MPIN, TPIN, ATM PIN |
| **SIM Card** | Mobile Number, PUK Code, MNP Port Code |
| **Custom** | Empty — user adds fields freely |

---

## 4. Security Design

### 4.1 Vault Key Creation (First Launch)

```
User enters Master Password
        │
        ▼
PBKDF2-HMAC-SHA256 (200,000 iterations + random 16-byte Salt)
        │
        ▼
Vault Key (256-bit AES Key)
        │
        ├──► Encrypted by Android Keystore (hardware-backed)
        │         └── Stored securely on device
        │
        └──► Used immediately to encrypt/decrypt Room DB
```

### 4.2 What is Stored & Where

| Item | Storage Location | Plain or Encrypted? |
|---|---|---|
| Salt | DataStore / Preferences | Plain (not secret) |
| Password Hash | DataStore | Hashed (for verification) |
| Vault Key | Android Keystore / iOS Keychain | Encrypted by hardware |
| Master Password | ❌ Never stored | — |
| Raw Vault Key in DB | ❌ Never stored | — |

### 4.3 Field Encryption Format

```
Each encryptedValue =
    Base64( IV [12 bytes] ║ AES-GCM Ciphertext ║ Auth Tag [16 bytes] )

Rules:
  ✅ Same Vault Key for all fields
  ✅ Different random IV for every single field
  ❌ Never reuse an IV
```

### 4.4 Memory Handling

```
Vault Key lives ONLY in RAM during active session.

Cleared when:
  → App closed
  → App killed
  → Session timeout reached (5–10 min)
  → Device screen off (optional)
```

---

## 5. App Unlock System

### First Launch
```
User sets Master Password
→ PBKDF2 generates Vault Key
→ Vault Key encrypted and stored in Keystore
→ User enrolled in biometric for future unlocks
```

### Daily Use (Normal)
```
User opens app
→ Biometric prompt (Fingerprint / Face ID)
→ Keystore releases Vault Key to RAM
→ App decrypts Room DB → Show vault
```

### App Killed / RAM Cleared
```
Vault Key wiped from RAM
→ On next open: Biometric prompt again
→ Keystore releases key again
```

### Biometric Fails (e.g. 5 attempts)
```
Fall back to Master Password
→ PBKDF2 re-derives Vault Key
→ Verify against stored hash
→ Unlock vault
```

### Unlock Flow Summary

| Scenario | Action |
|---|---|
| First launch | Master Password (setup) |
| Normal use | Biometric |
| App restart | Biometric |
| Biometric failure | Master Password (fallback) |
| Session timeout | Biometric |

---

## 6. Session Management

- Keep vault unlocked for **5–10 minutes** of inactivity
- After timeout → **auto lock** 🔒
- Going to background → optional auto lock (configurable)
- Vault Key wiped from RAM on lock

---

## 7. Export / Import System

### Two Key System

| Key | Purpose | Lives Where |
|---|---|---|
| **Vault Key** | Encrypts all DB data | RAM (session) + Keystore |
| **Export Key** | Encrypts the backup ZIP | 15 NATO words (user's brain) |

> ⚠️ These are **completely separate keys**. The Vault Key is NEVER put inside the ZIP file.

### Export Flow

```
1. Biometric verify → get Vault Key from Keystore
2. Decrypt all DB entries into plain text (in RAM only)
3. Serialize to JSON (ExportSchema v2)
4. Generate 15 random NATO words → show to user
5. User confirms words are written down
6. Derive Export Key = SHA-256(15 words concatenated lowercase)
7. Encrypt JSON using AES-256-CBC + Export Key
8. Save as vault_YYYYMMDD_HHMMSS.zip
```

**ZIP Contents:**
```
backup.zip
 ├── data.enc     ← AES encrypted JSON
 └── meta.json    ← version, exportedAt, app version (NO KEY inside)
```

### Import Flow

```
1. User selects ZIP file
2. User types the 15 NATO words
3. Derive Export Key from those words
4. Decrypt data.enc → plain text JSON
5. Parse JSON entries
6. Re-encrypt each field using THIS device's Vault Key
7. Insert into local Room DB
8. Show success count
```

**Why cross-device import works:**
> The ZIP uses the NATO word key (not the phone's hardware key), so **any device** with the app can import the ZIP as long as the user knows the 15 words.

### Export JSON Schema

```json
{
  "version": 2,
  "exportedAt": 1711234567890,
  "entries": [
    {
      "siteOrApp": "GitHub",
      "iconEmoji": "💻",
      "createdAt": 1711234000000,
      "fields": [
        { "label": "Email",    "value": "sanket@gmail.com", "isSecret": false, "order": 0 },
        { "label": "Password", "value": "abc123!",          "isSecret": true,  "order": 1 }
      ]
    },
    {
      "siteOrApp": "HDFC Bank",
      "iconEmoji": "🏦",
      "createdAt": 1711234100000,
      "fields": [
        { "label": "Customer ID", "value": "123456", "isSecret": false, "order": 0 },
        { "label": "MPIN",        "value": "9876",   "isSecret": true,  "order": 1 },
        { "label": "TPIN",        "value": "4521",   "isSecret": true,  "order": 2 },
        { "label": "ATM PIN",     "value": "1234",   "isSecret": true,  "order": 3 }
      ]
    }
  ]
}
```

### NATO Phonetic Alphabet (26 words, pick 15 randomly)

```
Alpha, Bravo, Charlie, Delta, Echo, Foxtrot, Golf, Hotel, India,
Juliet, Kilo, Lima, Mike, November, Oscar, Papa, Quebec, Romeo,
Sierra, Tango, Uniform, Victor, Whiskey, X-ray, Yankee, Zulu
```

---

## 8. Security Rules

| Rule | Status |
|---|---|
| Never store Master Password | ❌ Never |
| Never store raw Vault Key in DB | ❌ Never |
| Never generate random key on every launch | ❌ Never |
| Never use site name as encryption key | ❌ Never |
| Never store Export Key inside ZIP | ❌ Never |
| Never store plain text sensitive values | ❌ Never |
| Always derive Vault Key from Master Password + Salt | ✅ Always |
| Always use random IV per field | ✅ Always |
| Always store Vault Key in hardware Keystore | ✅ Always |
| Always wipe Vault Key from RAM on lock | ✅ Always |
| Always re-encrypt on import with local Vault Key | ✅ Always |

---

## 9. Architecture

```
composeApp/src/commonMain/kotlin/com/sanket/tools/passwordmanager/
├── data/
│   ├── db/
│   │   ├── PasswordEntry.kt          (@Entity — vault header)
│   │   ├── CredentialField.kt        (@Entity — freeform fields)
│   │   ├── PasswordDao.kt            (@Dao + EntryWithFields)
│   │   └── AppDatabase.kt            (@Database + expect/actual builder)
│   ├── crypto/
│   │   ├── CryptoEngine.kt           (expect/actual AES-256-GCM + PBKDF2)
│   │   ├── KeystoreManager.kt        (expect/actual Keystore/Keychain)
│   │   ├── BiometricManager.kt       (expect/actual biometric prompt)
│   │   ├── NatoKeyGenerator.kt       (15-word random NATO key)
│   │   └── ExportCrypto.kt           (ZIP encrypt/decrypt with NATO key)
│   ├── repository/
│   │   └── PasswordRepository.kt
│   └── export/
│       ├── ExportManager.kt
│       └── ImportManager.kt
├── domain/model/
│   └── CredentialItem.kt             (decrypted domain model)
├── ui/
│   ├── screen/
│   │   ├── SetupScreen.kt            (first launch — set master password)
│   │   ├── UnlockScreen.kt           (biometric + password fallback)
│   │   ├── VaultListScreen.kt
│   │   ├── AddEditScreen.kt          (freeform fields + templates)
│   │   ├── DetailScreen.kt           (view / copy / show-hide)
│   │   ├── ExportScreen.kt           (show NATO key + save ZIP)
│   │   └── ImportScreen.kt           (enter NATO key + pick ZIP)
│   └── viewmodel/
│       ├── UnlockViewModel.kt
│       ├── VaultViewModel.kt
│       ├── AddEditViewModel.kt
│       └── ExportImportViewModel.kt
└── di/
    └── AppModule.kt
```

### Platform-specific (`expect/actual`)

| Interface | Android | JVM Desktop | iOS |
|---|---|---|---|
| `CryptoEngine` | `javax.crypto` | `javax.crypto` | CommonCrypto |
| `KeystoreManager` | Android Keystore | Windows DPAPI / Keychain | Secure Enclave |
| `BiometricManager` | BiometricPrompt API | Windows Hello / Password dialog | LocalAuthentication |
| `DatabaseBuilder` | Room + Context | Room JVM path | NSDocumentDirectory |
| `FileManager` | ContentResolver | java.io.File | NSFileManager |
| `ClipboardManager` | Android clipboard | AWT Toolkit | UIPasteboard |

---

## 10. Screen Flow

```
App Launch
    │
    ├── First time? ──► [Setup Screen] (set master password + biometric enrol)
    │
    └── Returning? ──► [Unlock Screen]
                           ├── Biometric ✅ ──► [Vault List]
                           └── Fallback password ──► [Vault List]

[Vault List Screen]
    ├──► [+ FAB] ──► [Add/Edit Screen]
    │                   └── Pick template → fill fields → + Add Field button
    ├──► [entry tap] ──► [Detail Screen] → copy / show-hide / edit / delete
    ├──► [⋮ menu] ──► [Export Screen] → show 15 NATO words → save ZIP
    └──► [⋮ menu] ──► [Import Screen] → enter words → pick ZIP → merge
```

---

## 11. Koin DI Modules

```kotlin
val databaseModule = module {
    single { buildAppDatabase(get()) }
    single { get<AppDatabase>().passwordDao() }
}

val cryptoModule = module {
    single { CryptoEngine() }
    single { KeystoreManager() }
    single { BiometricManager() }
    single { NatoKeyGenerator() }
    single { ExportCrypto(get()) }
}

val repositoryModule = module {
    single { PasswordRepository(get(), get()) }
    single { ExportManager(get(), get()) }
    single { ImportManager(get(), get(), get()) }
}

val viewModelModule = module {
    viewModel { UnlockViewModel(get(), get()) }
    viewModel { VaultViewModel(get()) }
    viewModel { AddEditViewModel(get()) }
    viewModel { ExportImportViewModel(get(), get()) }
}
```

---

## 12. Gradle Dependencies to Add

```toml
# libs.versions.toml
[versions]
kotlinx-serialization = "1.9.0"

[libraries]
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-coroutines-core    = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
koin-viewmodel             = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
androidx-biometric         = { module = "androidx.biometric:biometric", version = "1.2.0-alpha05" }

[plugins]
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

---

## 13. Verification Plan

| Test | What to verify |
|---|---|
| Unit: `CryptoEngine` | Encrypt → Decrypt returns original string |
| Unit: `NatoKeyGenerator` | Returns exactly 15 unique words |
| Unit: `ExportCrypto` | Round-trip ZIP encrypt → decrypt → equals original |
| Unit: Key Storage | Vault Key survives app restart via Keystore |
| Integration: Room | Insert encrypted entry → query → decrypt → correct values |
| UI: First launch | Setup password → vault opens |
| UI: Biometric unlock | Fingerprint → vault opens |
| UI: Fallback | Biometric fail → password → vault opens |
| UI: Timeout | 10 min idle → auto lock |
| UI: Add entry (template) | Pick Bank → MPIN/TPIN appear → save → show in list |
| UI: Freeform field | Add "Social Security No" field → save → shows ●●●● |
| UI: Copy + auto-clear | Copy password → verify clipboard clears after 30 s |
| UI: Export | ZIP created → cannot open without NATO words |
| UI: Import same device | Import own ZIP → entries match |
| UI: Import cross-device | Import on laptop → entries match |
| UI: Wrong NATO key | Shows decryption error, no data inserted |

---

## 14. Future Enhancements (v2+)

- Autofill service (Android) / Password AutoFill (iOS)
- Cloud encrypted backup (Google Drive / iCloud)
- Password generator
- Password strength checker
- Biometric fallback PIN
- Multi-device sync
- Password history / audit log

---

## ✅ Status

| Item | Status |
|---|---|
| Architecture | ✅ Finalized |
| Security Model | ✅ Finalized |
| Data Model | ✅ Finalized |
| Export / Import Design | ✅ Finalized |
| Unlock / Biometric Flow | ✅ Finalized |
| Ready for Implementation | ✅ Yes |
