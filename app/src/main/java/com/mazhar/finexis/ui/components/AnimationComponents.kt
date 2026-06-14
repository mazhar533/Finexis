package com.mazhar.finexis.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
fun FadeInSlideUp(
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) {
            delay(delayMillis.toLong())
        }
        visibleState.targetState = true
    }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = tween(durationMillis = 450)) +
                slideInVertically(
                    initialOffsetY = { 50 }, // slide up from 50px below
                    animationSpec = tween(durationMillis = 450)
                )
    ) {
        content()
    }
}

@Composable
fun StaggeredItem(
    index: Int,
    content: @Composable () -> Unit
) {
    FadeInSlideUp(
        delayMillis = index * 80, // 80ms stagger delay per item
        content = content
    )
}
