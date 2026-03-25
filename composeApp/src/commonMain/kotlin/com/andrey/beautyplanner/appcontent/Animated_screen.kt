package com.andrey.beautyplanner.appcontent

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    ownerName: String = "",
    onAnimationFinished: () -> Unit
) {
    // 1. Анимация масштаба
    val scale = remember { Animatable(0f) }
    var displayedText by remember { mutableStateOf("") }

    // Если имя пустое — просто название, иначе "Beauty Planner от ..."
    val fullText = if (ownerName.isBlank())
        "Beauty Planner"
    else
        ownerName

    // 2. Анимация логотипа и текст-появление
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        fullText.forEachIndexed { index, _ ->
            displayedText = fullText.substring(0, index + 1)
            delay(60)
        }
        delay(1000)
        onAnimationFinished()
    }

    // 3. Макет
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_splash),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(220.dp)
                    .scale(scale.value)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = displayedText,
                fontSize = 22.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Cursive
            )
        }
    }
}