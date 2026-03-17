package com.sanket.tools.passwordmanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Password Manager",
    ) {
        App()
    }
}