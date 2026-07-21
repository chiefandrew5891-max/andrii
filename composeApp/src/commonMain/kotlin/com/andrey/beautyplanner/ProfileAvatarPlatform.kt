package com.andrey.beautyplanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

@Composable
fun rememberProfileAvatarPreview(base64: String): ImageBitmap? {
    return remember(base64) { null }
}