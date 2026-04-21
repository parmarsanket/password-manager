package com.sanket.tools.passwordmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val errorMessage = uiState.errorMessage

    val passwordFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }
    val buttonFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(uiState.isUnlocked) {
        if (uiState.isUnlocked) {
            onSuccess()
        }
    }
    // Re-focus the password field whenever biometric goes idle (cancelled / failed / initial load).
    // The boolean flips true→false when biometric ends, triggering this effect.
    LaunchedEffect(uiState.activeAction == UnlockAction.Biometric) {
        if (uiState.activeAction != UnlockAction.Biometric) {
            passwordFocus.requestFocus()
        }
    }

    // When an error appears (wrong password / passwords don't match), always
    // snap focus back to the first password field so the user can retype immediately.
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            passwordFocus.requestFocus()
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
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .widthIn(max = layout.formMaxWidth),
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = 10.dp,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .onPreviewKeyEvent{event ->
                                if (event.type == KeyEventType.KeyDown) {
                                    when (event.key) {

                                        // 🔥 ENTER KEY
//                                        Key.Enter -> {
//                                            if (!uiState.isBusy) {
//                                                if (uiState.mode == UnlockMode.Setup) {
//                                                    viewModel.setupMasterPassword(password, confirmPassword)
//                                                    focusManager.moveFocus(FocusDirection.Enter)
//
//                                                } else {
//                                                    viewModel.login(password)
//                                                    focusManager.moveFocus(FocusDirection.Enter)
//                                                }
//                                            }
//                                            true
//                                        }

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

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = errorMessage,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
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
                                            confirmFocus.requestFocus()
                                        } else {
                                            viewModel.login(password)
                                        }
                                    }
                                }
                            ),
                            label = { Text("Master Password") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(passwordFocus),
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
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
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    viewModel.clearFeedback()
                                },
                                // Enter on confirmPassword field submits the setup form
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (!uiState.isBusy) {
                                            viewModel.setupMasterPassword(password, confirmPassword)
                                        }
                                    }
                                ),
                                label = { Text("Confirm Password") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(confirmFocus),
                                visualTransformation = if (passwordVisible) {
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
                                    viewModel.setupMasterPassword(password, confirmPassword)
                                } else {
                                    viewModel.login(password)
                                }
                            },
                            modifier = Modifier
                                .focusRequester(buttonFocus)
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
        }
    }
}
