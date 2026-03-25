package com.andrey.beautyplanner.appcontent

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSplashScreen(
    ownerName: String = "",
    onAnimationFinished: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.8f) }

    // Создаем scope для запуска параллельных анимаций внутри эффекта
    val scope = rememberCoroutineScope()

    var displayedText by remember { mutableStateOf("") }
    val fullText = if (ownerName.isBlank()) "Beauty Planner" else ownerName

    LaunchedEffect(Unit) {
        // Запускаем появление фона
        scope.launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        // Запускаем масштаб поп-апа
        scope.launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )
        }

        delay(300)
        for (i in fullText.indices) {
            displayedText = fullText.substring(0, i + 1)
            delay(50)
        }

        delay(2000)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f * alphaAnim.value)),
        contentAlignment = Alignment.Center
    ) {
        // Используем Card или Surface для "поп-апа"
        Surface(
            modifier = Modifier
                .padding(32.dp)
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFE91E63)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = displayedText,
                    fontSize = 22.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}