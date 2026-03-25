package com.andrey.beautyplanner.appcontent

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.Locales
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    ownerName: String = "",
    onAnimationFinished: () -> Unit
) {
    val contentAlpha = remember { Animatable(0f) }
    val mainTitle = "Beauty Planner"

    // Используем ключ из Locales для мультиязычности
    val forPrefix = Locales.t("splash_for")
    val subTitle = if (ownerName.isNotEmpty()) "$forPrefix $ownerName" else ""

    var displayedTitle by remember { mutableStateOf("") }
    var displayedForOwner by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        // Эффект печати заголовка
        mainTitle.forEachIndexed { index, _ ->
            displayedTitle = mainTitle.substring(0, index + 1)
            delay(60)
        }

        if (subTitle.isNotEmpty()) {
            delay(300)
            subTitle.forEachIndexed { index, _ ->
                displayedForOwner = subTitle.substring(0, index + 1)
                delay(50)
            }
        }

        delay(1500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(contentAlpha.value)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = displayedTitle,
                fontSize = 34.sp,
                // Используем Serif + Italic для эффекта "курсива от руки"
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal, // Единый вес шрифта
                color = Color(0xFF212121),
                modifier = Modifier.height(45.dp)
            )

            if (displayedForOwner.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = displayedForOwner,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal, // Такой же вес, как у заголовка
                    color = Color(0xFF212121), // Такой же цвет
                    modifier = Modifier.height(30.dp)
                )
            }
        }
    }
}