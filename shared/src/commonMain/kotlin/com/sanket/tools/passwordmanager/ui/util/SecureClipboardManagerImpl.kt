package com.sanket.tools.passwordmanager.ui.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.sanket.tools.passwordmanager.data.crypto.currentTimeMillis

class SecureClipboardManagerImpl(
    private val clipboardManager: ClipboardManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : SecureClipboardManager {

    private val _securityState = MutableStateFlow(ClipboardSecurityState())
    override val securityState: StateFlow<ClipboardSecurityState> = _securityState.asStateFlow()

    private var clearJob: Job? = null

    init {
        clipboardManager.onCleared = {
            clearJob?.cancel()
            _securityState.update { ClipboardSecurityState() }
        }
    }

    override suspend fun copySecure(
        label: String,
        text: String,
        isSensitive: Boolean,
        autoClearMillis: Long,
        oneTimePaste: Boolean
    ) {
        clearJob?.cancel()

        clipboardManager.copyToClipboard(label, text, isSensitive)

        // Industry-level protection: Schedule a platform-independent clear
        if (autoClearMillis > 0) {
            clipboardManager.scheduleClear(autoClearMillis)
        }

        _securityState.update {
            ClipboardSecurityState(
                isSensitive = isSensitive,
                remainingSeconds = (autoClearMillis / 1000).toInt(),
                isOneTimePaste = oneTimePaste,
                label = label
            )
        }

        if (autoClearMillis > 0) {
            startClearTimer(autoClearMillis)
        }
    }

    override suspend fun clearClipboard() {
        clearJob?.cancel()
        clipboardManager.clearClipboard()
        _securityState.update { ClipboardSecurityState() }
    }

    override suspend fun onAppForegrounded() {
        if (_securityState.value.isOneTimePaste) {
            clearClipboard()
        }
    }

    private fun startClearTimer(timeoutMillis: Long) {
        clearJob = scope.launch(Dispatchers.Default) {
            val startTime = currentTimeMillis()
            var remaining = timeoutMillis
            
            while (remaining > 0) {
                // Update UI state every second
                _securityState.update { it.copy(remainingSeconds = (remaining / 1000).toInt()) }
                
                // Use smaller delays for better precision and to stay "active"
                delay(500) 
                
                val elapsed = currentTimeMillis() - startTime
                remaining = timeoutMillis - elapsed
            }
            
            // Ensure we clear on the main thread for UI/Platform consistency
            withContext(Dispatchers.Main) {
                clearClipboard()
            }
        }
    }
}
