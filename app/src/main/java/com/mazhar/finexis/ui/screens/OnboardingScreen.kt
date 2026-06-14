package com.mazhar.finexis.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.finexis.R
import com.mazhar.finexis.viewmodel.PreferenceViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class OnboardingPage(
    val title: String,
    val description: String,
    val iconResId: Int,
    val themeColor: Color
)

@SuppressLint("FrequentlyChangingValue")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    preferenceViewModel: PreferenceViewModel
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            title = "Track Every Penny",
            description = "Seamlessly log and categorize your expenses and income.",
            iconResId = R.drawable.icon_app,
            themeColor = Color(0xFF3B82F6) // Bright blue
        ),
        OnboardingPage(
            title = "Insightful Analytics",
            description = "Deep-dive into your spending habits with interactive charts.",
            iconResId = R.drawable.icon_analytics,
            themeColor = Color(0xFF7C3AED) // Royal violet
        ),
        OnboardingPage(
            title = "Secure Cloud Sync",
            description = "Your financial data is safely stored and synced across your devices.",
            iconResId = R.drawable.icon_shield_check,
            themeColor = Color(0xFF10B981) // Emerald green
        )
    )

    // Dynamic background glow transition color based on current page
    val currentThemeColor by animateColorAsState(
        targetValue = pages[pagerState.currentPage].themeColor,
        animationSpec = tween(durationMillis = 600),
        label = "bg_glow_color"
    )

    // Infinite floating animation for the main icon card
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative blurred glowing background blobs for premium visual depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Parallax scroll effect on the background blobs as you swipe
                    translationX = -pagerState.currentPageOffsetFraction * 120f
                }
        ) {
            // Top Right glow blob
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 80.dp, y = (-80).dp)
                    .alpha(0.12f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(currentThemeColor, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            // Bottom Left glow blob
            Box(
                modifier = Modifier
                    .size(360.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-100).dp, y = 100.dp)
                    .alpha(0.12f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(currentThemeColor, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Skip Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (pagerState.currentPage < 2) {
                    Text(
                        text = "Skip",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                preferenceViewModel.completeOnboarding()
                                onNavigateToLogin()
                            }
                            .padding(8.dp)
                    )
                }
            }

            // Horizontal Pager with transition parallax scale and alpha animations
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                val page = pages[pageIndex]

                // Calculate pager offset for smooth transition animations
                val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                val scale = 1f - (pageOffset.absoluteValue * 0.18f).coerceIn(0f, 1f)
                val alpha = 1f - (pageOffset.absoluteValue * 0.75f).coerceIn(0f, 1f)
                val translationX = 200.dp * pageOffset

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            this.alpha = alpha
                            this.scaleX = scale
                            this.scaleY = scale
                        }
                        .offset {
                            IntOffset(translationX.roundToPx(), 0)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Card Box with float micro-animation and dynamic color glow
                    Box(
                        modifier = Modifier
                            .offset(y = floatOffset.dp)
                            .size(165.dp)
                            .graphicsLayer {
                                shadowElevation = 12.dp.toPx()
                                shape = RoundedCornerShape(38.dp)
                                clip = true
                            }
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        page.themeColor,
                                        page.themeColor.copy(alpha = 0.8f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = page.iconResId),
                            contentDescription = page.title,
                            modifier = Modifier.size(72.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Slide Title
                    Text(
                        text = page.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Slide Description
                    Text(
                        text = page.description,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Bottom Section: Page Indicators and Action Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Capsule Page Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width = animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width.value)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) currentThemeColor
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Premium Action Button with Dynamic Color Transitions
                val isLastPage = pagerState.currentPage == 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(currentThemeColor)
                        .clickable {
                            if (isLastPage) {
                                preferenceViewModel.completeOnboarding()
                                onNavigateToLogin()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLastPage) "Get Started" else "Next",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (!isLastPage) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Arrow Forward",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
