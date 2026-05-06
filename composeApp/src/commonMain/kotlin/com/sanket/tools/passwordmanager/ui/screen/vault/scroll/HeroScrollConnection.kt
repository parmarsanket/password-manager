package com.sanket.tools.passwordmanager.ui.screen.vault.scroll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

fun Modifier.collapseByProgress(progress: Float): Modifier = this
    .clipToBounds()
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        // Natural height × (1 - progress): full → 0 automatically
        val collapsedHeight = (placeable.height * (1f - progress)).roundToInt()
        layout(placeable.width, collapsedHeight) {
            placeable.placeRelative(0, 0)
        }
    }

@Composable
fun rememberHeroScrollConnection(state: HeroScrollState) =
    remember {
        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {

                val deltaY = available.y

                // 👇 collapse on scroll up
                if (deltaY < 0) {
                    val consumed = state.consumeScroll(deltaY)
                    state.requestSettle()
                    return Offset(0f, consumed)
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {

                val deltaY = available.y

                // 👇 expand on scroll down
                if (deltaY > 0) {
                    val consumedY = state.consumeScroll(deltaY)
                    state.requestSettle()
                    return Offset(0f, consumedY)
                }

                return Offset.Zero
            }
        }
    }
