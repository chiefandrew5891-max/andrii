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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

// ВАЖНО: Мы не пишем здесь import beautyplanner...
// Мы заставим студию саму предложить импорт.

@Composable
fun AnimatedSplashScreen(
    ownerName: String = "",
    onAnimationFinished: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    var displayedText by remember { mutableStateOf("") }
    val fullText = ownerName.ifBlank { "Beauty Planner" }

    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
        fullText.forEachIndexed { index, _ ->
            displayedText = fullText.substring(0, index + 1)
            delay(60)
        }
        delay(1000)
        onAnimationFinished()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Если Res горит красным — это нормально на 5 секунд.
            // Нажми Build -> Rebuild Project.
            Text(text = "Loading...")

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = displayedText,
                fontSize = 22.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}