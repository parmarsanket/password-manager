package com.sanket.tools.passwordmanager.ui.screen.unlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import com.sanket.tools.passwordmanager.ui.layout.AdaptivePosture
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.layout.adaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockAction
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockViewModel

@Composable
fun UnlockScreen(
    viewModel: UnlockViewModel,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val screenState = rememberUnlockScreenState()
    val focusManager = LocalFocusManager.current
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(windowInfo.isWindowFocused) {
        if (windowInfo.isWindowFocused && uiState.activeAction != UnlockAction.Biometric) {
            screenState.requestFocusAndHandleKeyboard()
        }
    }

    LaunchedEffect(uiState.isUnlocked) {
        if (uiState.isUnlocked) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState.activeAction == UnlockAction.Biometric) {
        if (uiState.activeAction != UnlockAction.Biometric) {
            screenState.requestFocusAndHandleKeyboard()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            screenState.requestFocusAndHandleKeyboard()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val layout = adaptiveLayoutSpec(maxWidth, maxHeight)
            LaunchedEffect(maxWidth, maxHeight) {
                if (uiState.activeAction != UnlockAction.Biometric) {
                    screenState.requestFocusAndHandleKeyboard()
                }
            }
            val headlineStyle = if (layout.widthClass == AdaptiveWidthClass.Expanded) {
                MaterialTheme.typography.headlineLarge
            } else {
                MaterialTheme.typography.headlineMedium
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .imePadding()
                    .padding(
                        horizontal = layout.horizontalPadding,
                        vertical = layout.verticalPadding
                    )
            ) {
                val formContent: @Composable () -> Unit = {
                    UnlockFormCard(
                        layout = layout,
                        headlineStyle = headlineStyle,
                        uiState = uiState,
                        screenState = screenState,
                        focusManager = focusManager,
                        viewModel = viewModel,
                    )
                }

                when (layout.posture) {
                    AdaptivePosture.TabletLandscape -> UnlockTwoPaneLayout(
                        layout = layout,
                        headlineStyle = headlineStyle,
                        formContent = formContent,
                    )
                    AdaptivePosture.PhoneLandscape -> UnlockPhoneLandscapeLayout(
                        layout = layout,
                        formContent = formContent,
                    )
                    AdaptivePosture.PhonePortrait,
                    AdaptivePosture.TabletPortrait -> UnlockSinglePaneLayout(
                        layout = layout,
                        formContent = formContent,
                    )
                }
            }
        }
    }
}
