package com.andrey.beautyplanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun rememberProfileAvatarBitmap(base64: String): ImageBitmap?