package com.sanket.tools.passwordmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket.tools.passwordmanager.data.crypto.AuthResult
import com.sanket.tools.passwordmanager.data.crypto.BiometricManager
import com.sanket.tools.passwordmanager.data.crypto.CryptoEngine
import com.sanket.tools.passwordmanager.data.crypto.KeystoreManager
import com.sanket.tools.passwordmanager.data.crypto.PassworldSession
import com.sanket.tools.passwordmanager.data.prefs.PassworldPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Handles Setup (first launch) and Unlock (returning).
 */
class UnlockViewModel(
    private val cryptoEngine: CryptoEngine,
    private val keystore: KeystoreManager,
    private val biometric: BiometricManager,
    private val session: PassworldSession,
    private val prefs: PassworldPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow<UnlockUiState>(UnlockUiState.Loading)
    val uiState: StateFlow<UnlockUiState> = _uiState.asStateFlow()

    init {
        checkAppStatus()
    }

    fun refreshStatus() {
        checkAppStatus()
    }

    private fun checkAppStatus() {
        viewModelScope.launch {
            if (session.isUnlocked) {
                _uiState.value = UnlockUiState.Success
                return@launch
            }

            val isSetup = prefs.isSetupComplete().first()
            if (!isSetup) {
                _uiState.value = UnlockUiState.NeedsSetup
            } else {
                _uiState.value = UnlockUiState.NeedsLogin
                // Automatically try biometric on start if available
                tryBiometricUnlock()
            }
        }
    }

    fun tryBiometricUnlock() {
        viewModelScope.launch {
            if (biometric.canAuthenticate()) {
                val result = biometric.authenticate(
                    title = "Unlock Passworld",
                    subtitle = "Use biometric to access your credentials"
                )
                if (result is AuthResult.Success) {
                    val key = keystore.loadVaultKey()
                    if (key != null) {
                        session.start(key)
                        _uiState.value = UnlockUiState.Success
                    }
                }
            }
        }
    }

    fun setupMasterPassword(password: String) {
        viewModelScope.launch {
            _uiState.value = UnlockUiState.Processing
            val salt = cryptoEngine.generateSalt()
            val passworldKey = cryptoEngine.deriveVaultKey(password, salt)
            val passHash = cryptoEngine.hashPassword(password, salt)
            
            prefs.saveMasterSecrets(salt, passHash)
            keystore.saveVaultKey(passworldKey)
            session.start(passworldKey)
            
            _uiState.value = UnlockUiState.Success
        }
    }

    fun login(password: String) {
        viewModelScope.launch {
            _uiState.value = UnlockUiState.Processing
            try {
                val salt = prefs.getSalt()
                val storedHash = prefs.getPasswordHash()
                
                if (salt == null || storedHash == null) {
                    _uiState.value = UnlockUiState.Error("Data corrupted.")
                    return@launch
                }

                val isValid = cryptoEngine.verifyPassword(password, salt, storedHash)
                if (isValid) {
                    val key = keystore.loadVaultKey() ?: cryptoEngine.deriveVaultKey(password, salt)
                    if (keystore.loadVaultKey() == null) keystore.saveVaultKey(key)
                    
                    session.start(key)
                    _uiState.value = UnlockUiState.Success
                } else {
                    _uiState.value = UnlockUiState.Error("Incorrect password")
                }
            } catch (e: Exception) {
                _uiState.value = UnlockUiState.Error("Unlock failed: ${e.message}")
            }
        }
    }
}

sealed class UnlockUiState {
    object Loading : UnlockUiState()
    object NeedsSetup : UnlockUiState()
    object NeedsLogin : UnlockUiState()
    object Processing : UnlockUiState()
    object Success : UnlockUiState()
    data class Error(val message: String) : UnlockUiState()
}
