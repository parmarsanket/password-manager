package com.sanket.tools.passwordmanager.data.crypto

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.minutes

/**
 * Session manager that holds the Passworld Key ONLY in RAM.
 * Implements auto-lock timeout logic.
 */
class PassworldSession {

    private val _passworldKey = MutableStateFlow<ByteArray?>(null)
    val passworldKey: StateFlow<ByteArray?> = _passworldKey.asStateFlow()

    private var lastActivityTime: Long = 0
    private val timeoutMillis = 5.minutes.inWholeMilliseconds

    val isUnlocked: Boolean get() = _passworldKey.value != null

    fun start(key: ByteArray) {
        _passworldKey.value = key
        recordActivity()
    }

    /**
     * Call this on every user interaction to keep the session alive.
     */
    fun recordActivity() {
        lastActivityTime = currentTimeMillis()
    }

    /**
     * Check if the session should be locked due to inactivity.
     * Should be called periodically or on app foreground.
     */
    fun checkTimeout() {
        if (_passworldKey.value == null) return
        
        val now = currentTimeMillis()
        if (now - lastActivityTime > timeoutMillis) {
            stop()
        }
    }

    fun stop() {
        _passworldKey.value?.fill(0)
        _passworldKey.value = null
        lastActivityTime = 0
    }
}
