package com.sanket.tools.passwordmanager.ui.screen.unlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue

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
