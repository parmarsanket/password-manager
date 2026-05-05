package com.sanket.tools.passwordmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.platform.SoftwareKeyboardController
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptivePosture
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.layout.adaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockAction
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockMode
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
    // Re-focus the password field whenever biometric goes idle (cancelled / failed / initial load).
    // The boolean flips true→false when biometric ends, triggering this effect.
    LaunchedEffect(uiState.activeAction == UnlockAction.Biometric) {
        if (uiState.activeAction != UnlockAction.Biometric) {
            screenState.requestFocusAndHandleKeyboard()
        }
    }

    // When an error appears (wrong password / passwords don't match), always
    // snap focus back to the first password field so the user can retype immediately.
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            screenState.requestFocusAndHandleKeyboard()
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
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
                when (layout.posture) {
                    // ── Tablet landscape / Desktop — two-pane: branding + form ──
                    AdaptivePosture.TabletLandscape -> UnlockTwoPaneLayout(
                        layout = layout,
                        headlineStyle = headlineStyle,
                        formContent = {
                            UnlockFormCard(
                                layout = layout,
                                headlineStyle = headlineStyle,
                                uiState = uiState,
                                screenState = screenState,
                                focusManager = focusManager,
                                viewModel = viewModel,
                            )
                        }
                    )

                    // ── Phone landscape — horizontal layout, compact branding strip ──
                    AdaptivePosture.PhoneLandscape -> UnlockPhoneLandscapeLayout(
                        layout = layout,
                        formContent = {
                            UnlockFormCard(
                                layout = layout,
                                headlineStyle = headlineStyle,
                                uiState = uiState,
                                screenState = screenState,
                                focusManager = focusManager,
                                viewModel = viewModel,
                            )
                        }
                    )

                    // ── Phone portrait / Tablet portrait — centered card ──
                    AdaptivePosture.PhonePortrait,
                    AdaptivePosture.TabletPortrait -> UnlockSinglePaneLayout(
                        layout = layout,
                        formContent = {
                            UnlockFormCard(
                                layout = layout,
                                headlineStyle = headlineStyle,
                                uiState = uiState,
                                screenState = screenState,
                                focusManager = focusManager,
                                viewModel = viewModel,
                            )
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Layout strategies
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Phone portrait / Tablet portrait — centered single card.
 */
@Composable
private fun UnlockSinglePaneLayout(
    layout: AdaptiveLayoutSpec,
    formContent: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.widthIn(max = layout.formMaxWidth)) {
            formContent()
        }
    }
}

/**
 * Phone landscape — compact branding strip on the left, form on the right.
 * Screen is short so the branding is minimal (icon + title only).
 */
@Composable
private fun UnlockPhoneLandscapeLayout(
    layout: AdaptiveLayoutSpec,
    formContent: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(layout.paneSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Compact branding strip ──────────────────────────────────────
        Column(
            modifier = Modifier
                .width(layout.compactSidebarWidth)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp).size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Passworld",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Manager",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Form card ───────────────────────────────────────────────────
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.widthIn(max = layout.formMaxWidth)) {
                formContent()
            }
        }
    }
}

/**
 * Tablet landscape / Desktop — decorative branding panel on the left,
 * unlock form card on the right.
 */
@Composable
private fun UnlockTwoPaneLayout(
    layout: AdaptiveLayoutSpec,
    headlineStyle: androidx.compose.ui.text.TextStyle,
    formContent: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(layout.paneSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Branding sidebar ────────────────────────────────────────────
        Surface(
            modifier = Modifier
                .width(layout.sidebarWidth)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(layout.horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.padding(22.dp).size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Passworld Manager",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Your passwords, secured everywhere.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── Form card ───────────────────────────────────────────────────
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.widthIn(max = layout.formMaxWidth)) {
                formContent()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Shared unlock form card — extracted so all postures reuse the same form
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UnlockFormCard(
    layout: AdaptiveLayoutSpec,
    headlineStyle: androidx.compose.ui.text.TextStyle,
    uiState: com.sanket.tools.passwordmanager.ui.viewmodel.UnlockUiState,
    screenState: UnlockScreenState,
    focusManager: androidx.compose.ui.focus.FocusManager,
    viewModel: UnlockViewModel,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 10.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            // 🔽 ARROW DOWN
                            Key.DirectionDown -> {
                                focusManager.moveFocus(FocusDirection.Down)
                                true
                            }
                            // 🔼 ARROW UP
                            Key.DirectionUp -> {
                                focusManager.moveFocus(FocusDirection.Up)
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Icon — only shown in single-pane (two-pane has the sidebar branding)
            if (layout.posture == AdaptivePosture.PhonePortrait ||
                layout.posture == AdaptivePosture.TabletPortrait
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(18.dp)
                            .size(if (layout.widthClass == AdaptiveWidthClass.Compact) 28.dp else 32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Passworld Manager",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = if (uiState.mode == UnlockMode.Setup) {
                    "Create your master password"
                } else {
                    "Unlock your vault"
                },
                style = headlineStyle,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = uiState.supportingText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    uiState.errorMessage?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = screenState.password,
                onValueChange = {
                    screenState.password = it
                    viewModel.clearFeedback()
                },
                // keyboardOptions MUST declare ImeAction.Done so the IME
                // shows the "Done" action key AND so onDone fires on Enter.
                // Without this, keyboardActions.onDone is never called.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (!uiState.isBusy) {
                            if (uiState.mode == UnlockMode.Setup) {
                                // In Setup mode, move to confirm-password field
                                screenState.confirmFocus.requestFocus()
                            } else {
                                viewModel.login(screenState.password.text)
                            }
                        }
                    }
                ),
                label = { Text("Master Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(screenState.passwordFocus),
                visualTransformation = if (screenState.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { screenState.passwordVisible = !screenState.passwordVisible }) {
                        Icon(
                            imageVector = if (screenState.passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = null
                        )
                    }
                },
                singleLine = true,
                enabled = !uiState.isBusy,
                isError = uiState.errorMessage != null
            )

            if (uiState.mode == UnlockMode.Setup) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = screenState.confirmPassword,
                    onValueChange = {
                        screenState.confirmPassword = it
                        viewModel.clearFeedback()
                    },
                    // Enter on confirmPassword field submits the setup form
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!uiState.isBusy) {
                                viewModel.setupMasterPassword(screenState.password.text, screenState.confirmPassword.text)
                            }
                        }
                    ),
                    label = { Text("Confirm Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(screenState.confirmFocus),
                    visualTransformation = if (screenState.passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    enabled = !uiState.isBusy,
                    isError = uiState.errorMessage != null
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // .focusable() lets Compose render the focus-ring highlight when
            // this button is reached via ↓/↑ arrow-key navigation.
            Button(
                onClick = {
                    if (uiState.mode == UnlockMode.Setup) {
                        viewModel.setupMasterPassword(screenState.password.text, screenState.confirmPassword.text)
                    } else {
                        viewModel.login(screenState.password.text)
                    }
                },
                modifier = Modifier
                    .focusRequester(screenState.buttonFocus)
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isBusy && uiState.mode != UnlockMode.Loading
            ) {
                if (uiState.activeAction == UnlockAction.Password) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.mode == UnlockMode.Setup) {
                            "Create vault access"
                        } else {
                            "Unlock with master password"
                        }
                    )
                }
            }

            if (uiState.mode == UnlockMode.Login && uiState.isBiometricAvailable) {
                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Or use device verification",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { viewModel.tryBiometricUnlock() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isBusy
                ) {
                    if (uiState.activeAction == UnlockAction.Biometric) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Unlock with ${uiState.biometricLabel}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "On supported phones and laptops, this can use fingerprint, face, or the system PIN.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  State Holder
// ─────────────────────────────────────────────────────────────────────────────

@Stable
class UnlockScreenState(
    passwordInitial: TextFieldValue,
    confirmPasswordInitial: TextFieldValue,
    passwordVisibleInitial: Boolean,
    isInitialKeyboardShownInitial: Boolean,
    val passwordFocus: FocusRequester,
    val confirmFocus: FocusRequester,
    val buttonFocus: FocusRequester,
    private val keyboardController: SoftwareKeyboardController?,
) {
    var password by mutableStateOf(passwordInitial)
    var confirmPassword by mutableStateOf(confirmPasswordInitial)
    var passwordVisible by mutableStateOf(passwordVisibleInitial)
    var isInitialKeyboardShown by mutableStateOf(isInitialKeyboardShownInitial)

    fun requestFocusAndHandleKeyboard() {
        try {
            passwordFocus.requestFocus()
            if (isInitialKeyboardShown) {
                keyboardController?.hide()
            } else {
                isInitialKeyboardShown = true
            }
        } catch (e: Exception) {
            // Ignore if not initialized
        }
    }

    companion object {
        fun Saver(
            passwordFocus: FocusRequester,
            confirmFocus: FocusRequester,
            buttonFocus: FocusRequester,
            keyboardController: SoftwareKeyboardController?
        ): Saver<UnlockScreenState, *> = listSaver(
            save = {
                listOf(
                    it.password.text,
                    it.confirmPassword.text,
                    it.passwordVisible,
                    it.isInitialKeyboardShown
                )
            },
            restore = {
                UnlockScreenState(
                    passwordInitial = TextFieldValue(it[0] as String),
                    confirmPasswordInitial = TextFieldValue(it[1] as String),
                    passwordVisibleInitial = it[2] as Boolean,
                    isInitialKeyboardShownInitial = it[3] as Boolean,
                    passwordFocus = passwordFocus,
                    confirmFocus = confirmFocus,
                    buttonFocus = buttonFocus,
                    keyboardController = keyboardController
                )
            }
        )
    }
}

@Composable
fun rememberUnlockScreenState(
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    passwordFocus: FocusRequester = remember { FocusRequester() },
    confirmFocus: FocusRequester = remember { FocusRequester() },
    buttonFocus: FocusRequester = remember { FocusRequester() }
): UnlockScreenState {
    return rememberSaveable(
        keyboardController, passwordFocus, confirmFocus, buttonFocus,
        saver = UnlockScreenState.Saver(passwordFocus, confirmFocus, buttonFocus, keyboardController)
    ) {
        UnlockScreenState(
            passwordInitial = TextFieldValue(""),
            confirmPasswordInitial = TextFieldValue(""),
            passwordVisibleInitial = false,
            isInitialKeyboardShownInitial = false,
            passwordFocus = passwordFocus,
            confirmFocus = confirmFocus,
            buttonFocus = buttonFocus,
            keyboardController = keyboardController
        )
    }
}
