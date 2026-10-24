package com.example.fertilizerapp.ui.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fertilizerapp.BuildConfig
import com.example.fertilizerapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_DURATION_MS = 4000
private const val TEXT_DELAY_MS = 400
private const val FADE_OUT_MS = 400

// Fixed dark brand colors (independent of light/dark mode) so white text always has strong contrast
private val SplashTop = Color(0xFF2E7D32)
private val SplashMiddle = Color(0xFF1B5E20)
private val SplashBottom = Color(0xFF0B2A12)

private val TextShadow = Shadow(color = Color.Black.copy(alpha = 0.45f), offset = Offset(0f, 3f), blurRadius = 10f)

@Composable
fun TimerSplashScreen(onFinished: () -> Unit) {
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    val screenAlpha = remember { Animatable(1f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(1f, tween(SPLASH_DURATION_MS - FADE_OUT_MS, easing = LinearEasing))
        }
        logoVisible = true
        delay(TEXT_DELAY_MS.toLong())
        textVisible = true
        delay((SPLASH_DURATION_MS - TEXT_DELAY_MS - FADE_OUT_MS).toLong())
        screenAlpha.animateTo(0f, tween(FADE_OUT_MS))
        onFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.26f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(Brush.verticalGradient(listOf(SplashTop, SplashMiddle, SplashBottom)))
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.5f)
            ) {
                // Soft glow behind the logo
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(
                            Brush.radialGradient(listOf(Color.White.copy(alpha = glowAlpha), Color.Transparent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.gc_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulseScale)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Grow Helper",
                        style = MaterialTheme.typography.displaySmall.copy(shadow = TextShadow),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        // VERSION_NAME carries the flavor suffix ("1.5.0-timer")
                        "Version ${BuildConfig.VERSION_NAME.substringBefore('-')}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .width(160.dp)
                    .height(4.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {}
            )
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(visible = textVisible, enter = fadeIn(tween(500))) {
                Text(
                    "entwickelt von Sven Kersten",
                    style = MaterialTheme.typography.bodyMedium.merge(TextStyle(shadow = TextShadow)),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
