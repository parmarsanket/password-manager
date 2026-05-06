package com.sanket.tools.passwordmanager.ui.screen.unlock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveLayoutSpec
import com.sanket.tools.passwordmanager.ui.layout.AdaptivePosture
import com.sanket.tools.passwordmanager.ui.layout.AdaptiveWidthClass
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockAction
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockMode
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockUiState
import com.sanket.tools.passwordmanager.ui.viewmodel.UnlockViewModel

@Composable
internal fun UnlockFormCard(
    layout: AdaptiveLayoutSpec,
    headlineStyle: androidx.compose.ui.text.TextStyle,
    uiState: UnlockUiState,
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
