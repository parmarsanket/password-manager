package com.sanket.tools.passwordmanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.sanket.tools.passwordmanager.data.crypto.PassworldSession
import com.sanket.tools.passwordmanager.theme.AppTheme
import com.sanket.tools.passwordmanager.ui.screen.UnlockScreen
import com.sanket.tools.passwordmanager.ui.screen.VaultDashboardScreen
import com.sanket.tools.passwordmanager.ui.viewmodel.AddEditViewModel
import com.sanket.tools.passwordmanager.ui.viewmodel.PassworldViewModel
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val session: PassworldSession = koinInject()
    val unlockViewModel: UnlockViewModel = koinInject()
    val passworldViewModel: PassworldViewModel = koinInject()
    val addEditViewModel: AddEditViewModel = koinInject()
    val passworldKey by session.passworldKey.collectAsState()
    var hasUnlockedInThisSession by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        unlockViewModel.refreshStatus()

        while (true) {
            delay(30000)
            session.checkTimeout()
        }
    }

    LaunchedEffect(passworldKey) {
        if (passworldKey != null) {
            hasUnlockedInThisSession = true
        } else if (hasUnlockedInThisSession) {
            hasUnlockedInThisSession = false
            unlockViewModel.refreshStatus(autoPromptBiometric = false)
        }
    }

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(session) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            session.recordActivity()
                        }
                    }
                }
                .onPreviewKeyEvent {
                    session.recordActivity()
                    false
                }
        ) {
            if (passworldKey == null) {
                UnlockScreen(
                    viewModel = unlockViewModel,
                    onSuccess = {}
                )
            } else {
                VaultDashboardScreen(
                    passworldViewModel = passworldViewModel,
                    addEditViewModel = addEditViewModel,
                    onLogout = { passworldViewModel.logout() }
                )
            }
        }
    }
}
