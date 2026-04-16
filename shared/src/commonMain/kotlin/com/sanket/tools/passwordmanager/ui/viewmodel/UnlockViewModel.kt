package com.sanket.tools.passwordmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket.tools.passwordmanager.data.crypto.AuthResult
import com.sanket.tools.passwordmanager.data.crypto.BiometricManager
import com.sanket.tools.passwordmanager.data.crypto.CryptoEngine
import com.sanket.tools.passwordmanager.data.crypto.KeystoreManager
import com.sanket.tools.passwordmanager.data.crypto.PassworldSession
import com.sanket.tools.passwordmanager.data.prefs.PassworldPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles setup on first launch and unlock for returning users.
 */
class UnlockViewModel(
    private val cryptoEngine: CryptoEngine,
    private val keystore: KeystoreManager,
    private val biometric: BiometricManager,
    private val session: PassworldSession,
    private val prefs: PassworldPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnlockUiState())
    val uiState: StateFlow<UnlockUiState> = _uiState.asStateFlow()

    fun refreshStatus() {
        checkAppStatus()
    }

    fun clearFeedback() {
        _uiState.update { current ->
            val defaultText = defaultSupportingText(current.mode, current.isBiometricAvailable)
            if (current.errorMessage == null && current.supportingText == defaultText) {
                current
            } else {
                current.copy(
                    errorMessage = null,
                    supportingText = defaultText
                )
            }
        }
    }

    fun tryBiometricUnlock() {
        val currentState = _uiState.value
        if (currentState.mode != UnlockMode.Login || currentState.activeAction != null) return

        viewModelScope.launch {
            if (!currentState.isBiometricAvailable) {
                _uiState.update {
                    it.copy(errorMessage = "Biometric unlock is not available on this device.")
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    activeAction = UnlockAction.Biometric,
                    errorMessage = null,
                    supportingText = "Approve the system prompt with your fingerprint, face, or device PIN."
                )
            }

            when (
                val result = biometric.authenticate(
                    title = "Unlock Passworld",
                    subtitle = "Use your device verification to access your vault"
                )
            ) {
                AuthResult.Success -> {
                    val key = withContext(Dispatchers.Default) {
                        runCatching { keystore.loadVaultKey() }.getOrNull()
                    }

                    if (key != null) {
                        session.start(key)
                        _uiState.update {
                            it.copy(activeAction = null, errorMessage = null, isUnlocked = true)
                        }
                    } else {
                        _uiState.value = idleState(
                            mode = UnlockMode.Login,
                            biometricAvailable = false,
                            biometricLabel = currentState.biometricLabel,
                            errorMessage = "Biometric unlock is configured, but the secure vault key is missing. Use your master password once to restore it."
                        )
                    }
                }

                AuthResult.Canceled -> {
                    _uiState.value = idleState(
                        mode = UnlockMode.Login,
                        biometricAvailable = currentState.isBiometricAvailable,
                        biometricLabel = currentState.biometricLabel,
                        supportingText = "Biometric unlock was canceled. Try again or continue with your master password."
                    )
                }

                is AuthResult.Failure -> {
                    _uiState.value = idleState(
                        mode = UnlockMode.Login,
                        biometricAvailable = currentState.isBiometricAvailable,
                        biometricLabel = currentState.biometricLabel,
                        errorMessage = result.error.ifBlank { "Biometric unlock failed." },
                        supportingText = "Try biometric again or use your master password."
                    )
                }

                AuthResult.NotAvailable -> {
                    _uiState.value = idleState(
                        mode = UnlockMode.Login,
                        biometricAvailable = currentState.isBiometricAvailable,
                        biometricLabel = currentState.biometricLabel,
                        errorMessage = "Device verification is not ready on this device. Check your biometric or Windows Hello setup and try again."
                    )
                }
            }
        }
    }

    fun setupMasterPassword(password: String, confirmPassword: String) {
        if (_uiState.value.mode != UnlockMode.Setup || _uiState.value.activeAction != null) return

        when {
            password.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Master password is required.") }
                return
            }

            password.length < 8 -> {
                _uiState.update { it.copy(errorMessage = "Use at least 8 characters for the master password.") }
                return
            }

            password != confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(activeAction = UnlockAction.Password, errorMessage = null)
            }

            try {
                val passworldKey = withContext(Dispatchers.Default) {
                    val salt = cryptoEngine.generateSalt()
                    val vaultKey = cryptoEngine.deriveVaultKey(password, salt)
                    val verifierHash = cryptoEngine.createPasswordVerifier(vaultKey)

                    prefs.saveMasterSecrets(salt, verifierHash)
                    keystore.saveVaultKey(vaultKey)
                    vaultKey
                }

                session.start(passworldKey)
                _uiState.update {
                    it.copy(activeAction = null, errorMessage = null, isUnlocked = true)
                }
            } catch (e: Exception) {
                _uiState.value = idleState(
                    mode = UnlockMode.Setup,
                    errorMessage = "Unable to create your master password. ${e.message ?: "Please try again."}"
                )
            }
        }
    }

    fun login(password: String) {
        val currentState = _uiState.value
        if (currentState.mode != UnlockMode.Login || currentState.activeAction != null) return

        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Master password is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(activeAction = UnlockAction.Password, errorMessage = null)
            }

            try {
                val unlockKey = withContext(Dispatchers.Default) {
                    val salt = prefs.getSalt()
                    val storedHash = prefs.getPasswordHash()

                    if (salt == null || storedHash == null) {
                        throw IllegalStateException("Stored unlock data is corrupted.")
                    }

                    val derivedVaultKey = cryptoEngine.deriveVaultKey(password, salt)
                    val currentVerifier = cryptoEngine.createPasswordVerifier(derivedVaultKey)
                    val isCurrentVerifier = currentVerifier.secureEquals(storedHash)
                    val isLegacyVerifier = if (isCurrentVerifier) {
                        false
                    } else {
                        cryptoEngine.verifyPassword(password, salt, storedHash)
                    }

                    if (!isCurrentVerifier && !isLegacyVerifier) {
                        return@withContext null
                    }

                    if (isLegacyVerifier) {
                        prefs.saveMasterSecrets(salt, currentVerifier)
                    }

                    val storedVaultKey = runCatching { keystore.loadVaultKey() }.getOrNull()
                    val sessionKey = if (storedVaultKey != null && storedVaultKey.contentEquals(derivedVaultKey)) {
                        storedVaultKey
                    } else {
                        keystore.saveVaultKey(derivedVaultKey)
                        derivedVaultKey
                    }

                    sessionKey
                }

                if (unlockKey == null) {
                    _uiState.value = idleState(
                    mode = UnlockMode.Login,
                    biometricAvailable = currentState.isBiometricAvailable,
                    biometricLabel = currentState.biometricLabel,
                    errorMessage = "Incorrect master password."
                )
                return@launch
            }

                session.start(unlockKey)
                _uiState.update {
                    it.copy(activeAction = null, errorMessage = null, isUnlocked = true)
                }
            } catch (e: Exception) {
                _uiState.value = idleState(
                    mode = UnlockMode.Login,
                    biometricAvailable = currentState.isBiometricAvailable,
                    biometricLabel = currentState.biometricLabel,
                    errorMessage = "Unlock failed. ${e.message ?: "Please try again."}"
                )
            }
        }
    }

    private fun checkAppStatus() {
        viewModelScope.launch {
            if (session.isUnlocked) {
                _uiState.update { it.copy(isUnlocked = true) }
                return@launch
            }

            val isSetup = withContext(Dispatchers.Default) {
                prefs.isSetupComplete().first()
            }

            if (!isSetup) {
                _uiState.value = idleState(mode = UnlockMode.Setup)
            } else {
                val biometricAvailable = withContext(Dispatchers.Default) {
                    shouldShowDeviceAuthOption()
                }
                _uiState.value = idleState(
                    mode = UnlockMode.Login,
                    biometricAvailable = biometricAvailable,
                    biometricLabel = biometric.authenticationLabel()
                )
            }
        }
    }

    private fun shouldShowDeviceAuthOption(): Boolean {
        return runCatching {
            biometric.shouldOfferAuthentication() && keystore.loadVaultKey() != null
        }.getOrDefault(false)
    }

    private fun idleState(
        mode: UnlockMode,
        biometricAvailable: Boolean = false,
        biometricLabel: String = biometric.authenticationLabel(),
        errorMessage: String? = null,
        supportingText: String = defaultSupportingText(mode, biometricAvailable)
    ): UnlockUiState {
        return UnlockUiState(
            mode = mode,
            isBiometricAvailable = biometricAvailable,
            biometricLabel = biometricLabel,
            supportingText = supportingText,
            errorMessage = errorMessage
        )
    }

    private fun defaultSupportingText(
        mode: UnlockMode,
        biometricAvailable: Boolean
    ): String {
        return when (mode) {
            UnlockMode.Loading -> "Checking secure access..."
            UnlockMode.Setup -> "Create a master password to secure the vault on this device."
            UnlockMode.Login -> {
                if (biometricAvailable) {
                    "Unlock with your master password or use fingerprint, face, or your device PIN."
                } else {
                    "Enter your master password to unlock the vault."
                }
            }
        }
    }

    private fun String.secureEquals(other: String): Boolean {
        return encodeToByteArray().contentEquals(other.encodeToByteArray())
    }
}

data class UnlockUiState(
    val mode: UnlockMode = UnlockMode.Loading,
    val activeAction: UnlockAction? = null,
    val isBiometricAvailable: Boolean = false,
    val biometricLabel: String = "biometric",
    val supportingText: String = "Checking secure access...",
    val errorMessage: String? = null,
    val isUnlocked: Boolean = false
) {
    val isBusy: Boolean
        get() = activeAction != null
}

enum class UnlockMode {
    Loading,
    Setup,
    Login
}

enum class UnlockAction {
    Password,
    Biometric
}
