package com.andrey.beautyplanner

actual fun loadLocaleResourceText(path: String): String? {
    val ctx = AndroidAppContext.context ?: return null

    val candidates = listOf(
        path,
        "locales/" + path.removePrefix("locales/"),
        path.removePrefix("locales/")
    ).distinct()

    for (candidate in candidates) {
        try {
            val text = ctx.assets.open(candidate).bufferedReader(Charsets.UTF_8).use { it.readText() }
            return text
        } catch (_: Throwable) {
        }
    }

    return null
}