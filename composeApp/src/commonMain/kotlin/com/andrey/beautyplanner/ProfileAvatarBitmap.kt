package com.andrey.beautyplanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeBase64

@Composable
fun rememberProfileAvatarBitmap(base64: String): ImageBitmap? {
    return remember(base64) {
        runCatching {
            if (base64.isBlank()) return@runCatching null
            val byteString: ByteString = base64.decodeBase64() ?: return@runCatching null
            byteString.toByteArray().decodeToImageBitmap()
        }.getOrNull()
    }
}