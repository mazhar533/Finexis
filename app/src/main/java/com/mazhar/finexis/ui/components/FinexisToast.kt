package com.mazhar.finexis.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mazhar.finexis.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FinexisToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    isError: Boolean = false,
    durationMillis: Long = 3000L
) {
    val density = LocalDensity.current
    
    // Toast bounds for off-screen and bounce targets
    val startY = with(density) { -150.dp.toPx() }
    val downY = with(density) { 15.dp.toPx() }
    
    val translationY = remember { Animatable(startY) }
    val alpha = remember { Animatable(0f) }
    
    // Track internal composition to support exit transition states
    var isComposed by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            isComposed = true
            // Run entry animation: fade in and slide down with bouncy overshoot spring
            kotlinx.coroutines.coroutineScope {
                launch {
                    alpha.animateTo(1f, animationSpec = tween(200))
                }
                launch {
                    translationY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = 0.55f, // Bouncy overshoot
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            }
        } else if (isComposed) {
            // Run exit animation: dip down slightly first, then slide up and fade out
            kotlinx.coroutines.coroutineScope {
                // 1. Dip down slightly
                translationY.animateTo(
                    targetValue = downY,
                    animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing)
                )
                // 2. Slide up and fade out in parallel
                launch {
                    alpha.animateTo(0f, animationSpec = tween(250))
                }
                launch {
                    translationY.animateTo(
                        targetValue = startY,
                        animationSpec = tween(durationMillis = 280, easing = FastOutLinearInEasing)
                    )
                }
            }
            isComposed = false
        }
    }

    // Auto dismiss timer
    LaunchedEffect(visible, message) {
        if (visible) {
            delay(durationMillis.milliseconds)
            onDismiss()
        }
    }

    if (isComposed) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .zIndex(999f)
                .graphicsLayer {
                    this.translationY = translationY.value
                    this.alpha = alpha.value
                }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isError) Color(0xFFEF4444) else Color(0xFF00A86B)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isError) R.drawable.icon_verify
                                 else R.drawable.icon_verify
                        ),
                        contentDescription = if (isError) "Error" else "Success",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
