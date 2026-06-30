package com.andrey.beautyplanner

fun debugReadAndroidLocalePaths() {
    val ctx = AndroidAppContext.context
    if (ctx == null) {
        println("LocalesDebug: Android context is null")
        return
    }

    val assetManager = ctx.assets

    fun walk(path: String, depth: Int = 0) {
        if (depth > 8) return

        try {
            val list = assetManager.list(path) ?: return

            for (name in list) {
                val fullPath = if (path.isBlank()) name else "$path/$name"

                if (
                    fullPath.contains("locales", ignoreCase = true) ||
                    fullPath.contains(".json", ignoreCase = true) ||
                    fullPath.contains("ru", ignoreCase = true) ||
                    fullPath.contains("en", ignoreCase = true)
                ) {
                    println("LocalesDebug: asset found -> $fullPath")
                }

                // пробуем зайти глубже
                walk(fullPath, depth + 1)
            }
        } catch (t: Throwable) {
            println("LocalesDebug: walk fail path=$path error=${t.message}")
        }
    }

    println("LocalesDebug: ===== ASSET TREE SCAN START =====")
    walk("")
    println("LocalesDebug: ===== ASSET TREE SCAN END =====")

    val candidates = listOf(
        "composeResources/com.andrey.beautyplanner.generated.resources/files/locales/ru.json",
        "composeResources/com.andrey.beautyplanner.generated.resources/locales/ru.json",
        "composeResources/com.andrey.beautyplanner.generated.resources/ru.json",
        "composeResources/files/locales/ru.json",
        "files/locales/ru.json",
        "locales/ru.json",
        "ru.json"
    )

    for (path in candidates) {
        try {
            println("LocalesDebug: trying asset path = $path")
            val text = assetManager.open(path).bufferedReader().use { it.readText() }
            println("LocalesDebug: SUCCESS asset path = $path")
            LocalesDebug.debugParseLocaleText(path, text)
        } catch (t: Throwable) {
            println("LocalesDebug: FAIL asset path = $path error = ${t.message}")
        }
    }
}