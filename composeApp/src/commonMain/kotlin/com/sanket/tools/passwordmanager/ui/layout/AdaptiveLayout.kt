package com.sanket.tools.passwordmanager.ui.layout

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AdaptiveWidthClass {
    Compact,
    Medium,
    Expanded
}

@Immutable
data class AdaptiveLayoutSpec(
    val widthClass: AdaptiveWidthClass,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val contentMaxWidth: Dp,
    val formMaxWidth: Dp,
    val listMinCellSize: Dp,
    val paneSpacing: Dp,
    val useTwoPaneDetail: Boolean
)

fun adaptiveLayoutSpec(maxWidth: Dp): AdaptiveLayoutSpec = when {
    maxWidth < 600.dp -> AdaptiveLayoutSpec(
        widthClass = AdaptiveWidthClass.Compact,
        horizontalPadding = 16.dp,
        verticalPadding = 16.dp,
        contentMaxWidth = 560.dp,
        formMaxWidth = 440.dp,
        listMinCellSize = 260.dp,
        paneSpacing = 16.dp,
        useTwoPaneDetail = false
    )
    maxWidth < 960.dp -> AdaptiveLayoutSpec(
        widthClass = AdaptiveWidthClass.Medium,
        horizontalPadding = 24.dp,
        verticalPadding = 20.dp,
        contentMaxWidth = 760.dp,
        formMaxWidth = 520.dp,
        listMinCellSize = 280.dp,
        paneSpacing = 20.dp,
        useTwoPaneDetail = false
    )
    else -> AdaptiveLayoutSpec(
        widthClass = AdaptiveWidthClass.Expanded,
        horizontalPadding = 32.dp,
        verticalPadding = 24.dp,
        contentMaxWidth = 1120.dp,
        formMaxWidth = 560.dp,
        listMinCellSize = 320.dp,
        paneSpacing = 24.dp,
        useTwoPaneDetail = true
    )
}
