package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.JapPayViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: JapPayViewModel) {
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        // Parallel entry animations
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(1f, animationSpec = tween(400))
        textAlpha.animateTo(1f, animationSpec = tween(500))

        // Wait 1.8 seconds then transition to next screen
        delay(1800)
        if (viewModel.currentUser.value != null) {
            viewModel.currentScreen.value = Screen.Main
        } else {
            viewModel.currentScreen.value = Screen.Login
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(BrandPurpleTint.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(BrandPurpleTint.copy(alpha = 0.8f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Jap App Icon badge
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(12.dp, CircleShape, spotColor = BrandPurple)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(BrandPurple, BrandPurpleDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = "Jap Pay",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Jap Pay",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = BrandPurple,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Fast • Secure • Virtual UPI",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        // Bottom tagline & security guarantee
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(textAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "100% In-App Currency Ecosystem",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Zero Real Bank Dependencies",
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}
