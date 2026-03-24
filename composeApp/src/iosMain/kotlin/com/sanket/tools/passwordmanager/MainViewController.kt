// ── iOS NOT ACTIVE — this project targets Android + Desktop JVM only ──
package com.sanket.tools.passwordmanager

import androidx.compose.ui.window.ComposeUIViewController

// If iOS is re-enabled in future — also call initKoin() here
fun MainViewController() = ComposeUIViewController { App() }