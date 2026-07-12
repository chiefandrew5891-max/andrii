package com.andrey.beautyplanner

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val backendPlatform: String = "ios"
}

actual fun getPlatform(): Platform = IOSPlatform()