@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.andrey.beautyplanner

import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfFile

actual fun loadLocaleResourceText(path: String): String? {
    val normalized = path.removePrefix("/")
    val fileName = normalized.substringAfterLast("/")
    val langName = fileName.removeSuffix(".json")

    val candidates = listOfNotNull(
        NSBundle.mainBundle.pathForResource(langName, "json", "locales"),
        NSBundle.mainBundle.pathForResource(langName, "json"),
        NSBundle.mainBundle.resourcePath?.let { "$it/locales/$fileName" },
        NSBundle.mainBundle.resourcePath?.let { "$it/$fileName" }
    ).distinct()

    for (candidate in candidates) {
        val exists = NSFileManager.defaultManager.fileExistsAtPath(candidate)
        println("LocaleResourceLoader: checking $candidate exists=$exists")
        if (!exists) continue

        val text = NSString.stringWithContentsOfFile(
            path = candidate,
            encoding = 4u,
            error = null
        ) as String?

        if (!text.isNullOrBlank()) {
            println("LocaleResourceLoader: loaded locale file from $candidate")
            return text
        }
    }

    println("LocaleResourceLoader: failed to load locale file for path=$path")
    return null
}