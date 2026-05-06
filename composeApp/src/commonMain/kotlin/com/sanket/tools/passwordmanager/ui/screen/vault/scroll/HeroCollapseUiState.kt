package com.sanket.tools.passwordmanager.ui.screen.vault.scroll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.util.lerp

@Stable
class HeroCollapseUiState(
    heroState: HeroScrollState,
    maxCollapsePx: Float,
) {
    val progress by derivedStateOf { heroState.progress }

    val heroAlpha by derivedStateOf {
        val p = heroState.progress
        1f - (p * p)
    }

    val heroTranslationY by derivedStateOf {
        -(heroState.progress * maxCollapsePx * 0.4f)
    }

    val heroScale by derivedStateOf {
        lerp(1f, 0.50f, heroState.progress)
    }

    val overflowAlpha by derivedStateOf {
        ((heroState.progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
    }

}

@Composable
fun rememberHeroCollapseUiState(
    heroState: HeroScrollState,
    maxCollapsePx: Float,
): HeroCollapseUiState = remember(heroState, maxCollapsePx) {
    HeroCollapseUiState(heroState, maxCollapsePx)
}
