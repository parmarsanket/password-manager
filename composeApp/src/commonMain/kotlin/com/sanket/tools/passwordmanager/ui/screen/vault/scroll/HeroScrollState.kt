package com.sanket.tools.passwordmanager.ui.screen.vault.scroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class HeroScrollState(
    val maxCollapsePx: Float,
    private val scope: CoroutineScope,
) {
    var offset by mutableFloatStateOf(0f)
        private set

    val progress: Float
        get() = (-offset / maxCollapsePx).coerceIn(0f, 1f)

    private val isSettled: Boolean
        get() = progress <= 0.01f || progress >= 0.99f

    private var settleJob: Job? = null

    /**
     * Consume [delta] pixels of scroll for hero collapse/expand.
     * Cancels any in-flight settle animation so the user stays in control.
     */
    fun consumeScroll(delta: Float): Float {
        settleJob?.cancel()
        settleJob = null
        val newOffset = (offset + delta).coerceIn(-maxCollapsePx, 0f)
        val consumed = newOffset - offset
        offset = newOffset
        return consumed
    }

    /**
     * Debounced snap-to-boundary.  After 150 ms of scroll-idle the hero
     * animates to the nearest boundary (fully expanded or fully collapsed).
     * A new scroll event cancels the pending settle via [consumeScroll].
     */
    fun requestSettle() {
        settleJob?.cancel()
        if (isSettled) return          // already at a boundary
        settleJob = scope.launch {
            delay(150)                 // debounce — wait for scroll to stop
            if (isSettled) return@launch
            val target = if (progress < 0.5f) 0f else -maxCollapsePx
            val anim = Animatable(offset)
            anim.animateTo(
                targetValue   = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
            ) {
                // Update offset on every animation frame
                offset = value
            }
        }
    }
}
