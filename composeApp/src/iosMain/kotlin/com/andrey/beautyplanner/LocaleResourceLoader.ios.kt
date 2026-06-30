package com.andrey.beautyplanner

import platform.Foundation.NSBundle
import platform.Foundation.NSString

actual fun loadLocaleResourceText(path: String): String? {
    val clean = path.removePrefix("locales/").removeSuffix(".json")
    val filePath = NSBundle.mainBundle.pathForResource(
        name = clean,
        ofType = "json",
        inDirectory = "locales"
    ) ?: return null

    return NSString.stringWithContentsOfFile(
        path = filePath,
        encoding = 4u, // UTF-8
        error = null
    ) as String?
}