package com.andrey.beautyplanner

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

fun decodeAndroidProfileAvatar(base64: String): ImageBitmap? {
    return runCatching {
        if (base64.isBlank()) return@runCatching null
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}