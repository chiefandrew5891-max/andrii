package com.andrey.beautyplanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun rememberProfileAvatarBitmap(base64: String): ImageBitmap? {
    return remember(base64) {
        runCatching {
            if (base64.isBlank()) return@runCatching null
            val bytes = Base64.decode(base64)
            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        }.getOrNull()
    }
}
