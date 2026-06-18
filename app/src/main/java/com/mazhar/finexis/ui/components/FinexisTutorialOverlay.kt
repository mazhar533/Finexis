package com.mazhar.finexis.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.finexis.ui.theme.FinexisPrimary
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.roundToInt

data class TutorialStep(
    val title: String,
    val description: String,
    val stepKey: String
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FinexisTutorialOverlay(
    activeStepIndex: Int,
    steps: List<TutorialStep>,
    targetRect: Rect?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeStepIndex !in steps.indices) return

    val currentStep = steps[activeStepIndex]
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Screen dimensions in Px
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    var overlayPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var overlayHeightPx by remember { mutableFloatStateOf(screenHeightPx) }
    var isPositioned by remember { mutableStateOf(false) }

    val localTargetRect = remember(targetRect, overlayPositionInRoot) {
        targetRect?.translate(-overlayPositionInRoot.x, -overlayPositionInRoot.y)
    }

    var tooltipHeightPx by remember { mutableIntStateOf(0) }
    val tooltipYOffset = remember(localTargetRect, tooltipHeightPx, overlayHeightPx, density) {
        val spacingPx = with(density) { 20.dp.toPx() }
        if (localTargetRect == null) {
            // Default center fallback
            (overlayHeightPx / 2f) - (tooltipHeightPx / 2f)
        } else {
            val spaceBelow = overlayHeightPx - localTargetRect.bottom
            val spaceAbove = localTargetRect.top

            if (spaceBelow > spaceAbove) {
                // Place below the target
                localTargetRect.bottom + spacingPx
            } else {
                // Place above the target
                localTargetRect.top - tooltipHeightPx - spacingPx
            }
        }
    }

    // Pulsing animation for the spotlight border
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScaleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                overlayPositionInRoot = layoutCoordinates.positionInRoot()
                overlayHeightPx = layoutCoordinates.size.height.toFloat()
                isPositioned = true
            }
    ) {
        if (isPositioned) {
            // Spotlight Canvas Overlay
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.99f) // Required for BlendMode.Clear to work
            ) {
                // Draw dark transparent background
                drawRect(color = Color.Black.copy(alpha = 0.78f))

                localTargetRect?.let { rect ->
                    // Clear the target area
                    if (currentStep.stepKey == "notification") {
                        // Circle cutout for small icons
                        val radius = (rect.width.coerceAtLeast(rect.height) / 2f) + 12.dp.toPx()
                        drawCircle(
                            color = Color.Transparent,
                            radius = radius,
                            center = rect.center,
                            blendMode = BlendMode.Clear
                        )
                    } else {
                        // Rounded rect cutout for cards
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(rect.left - 8.dp.toPx(), rect.top - 8.dp.toPx()),
                            size = rect.size.copy(
                                width = rect.width + 16.dp.toPx(),
                                height = rect.height + 16.dp.toPx()
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                            blendMode = BlendMode.Clear
                        )
                    }
                }
            }

            // Pulse border overlay
            if (localTargetRect != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    localTargetRect.let { rect ->
                        if (currentStep.stepKey == "notification") {
                            val radius = (rect.width.coerceAtLeast(rect.height) / 2f) + 12.dp.toPx()
                            drawCircle(
                                color = FinexisPrimary.copy(alpha = pulseAlpha),
                                radius = radius + pulseScaleOffset,
                                center = rect.center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        } else {
                            drawRoundRect(
                                color = FinexisPrimary.copy(alpha = pulseAlpha),
                                topLeft = Offset(rect.left - 8.dp.toPx() - pulseScaleOffset, rect.top - 8.dp.toPx() - pulseScaleOffset),
                                size = rect.size.copy(
                                    width = rect.width + 16.dp.toPx() + (pulseScaleOffset * 2),
                                    height = rect.height + 16.dp.toPx() + (pulseScaleOffset * 2)
                                ),
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset { IntOffset(0, tooltipYOffset.roundToInt()) }
                    .onGloballyPositioned {
                        tooltipHeightPx = it.size.height
                    }
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = FinexisPrimary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Header: Step index indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TUTORIAL TOUR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinexisPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${activeStepIndex + 1} of ${steps.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        Text(
                            text = currentStep.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        Text(
                            text = currentStep.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onSkip
                            ) {
                                Text(
                                    text = "Skip Tour",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = onNext,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FinexisPrimary)
                            ) {
                                Text(
                                    text = if (activeStepIndex == steps.size - 1) "Finish" else "Next Step",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
