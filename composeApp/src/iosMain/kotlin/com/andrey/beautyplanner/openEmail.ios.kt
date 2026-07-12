package com.andrey.beautyplanner

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openEmail(email: String) {
    val cleaned = email.trim()
    if (cleaned.isBlank()) return

    val url = NSURL.URLWithString("mailto:$cleaned") ?: return
    val app = UIApplication.sharedApplication

    if (app.canOpenURL(url)) {
        app.openURL(url)
    }
}