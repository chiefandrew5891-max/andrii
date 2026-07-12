package com.andrey.beautyplanner

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val backendPlatform: String = "android"
}

actual fun getPlatform(): Platform = AndroidPlatform()