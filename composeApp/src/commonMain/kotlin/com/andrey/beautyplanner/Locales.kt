package com.andrey.beautyplanner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object Locales {
    var currentLanguage by mutableStateOf(
        AppSettings.languageCodes[AppSettings.selectedLanguage] ?: "en"
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val strings = mutableMapOf<String, Map<String, String>>()
    private var initialized = false

    suspend fun init() {
        if (initialized) return
        ensureLoaded("en")
        ensureLoaded(normalizeLang(currentLanguage))
        initialized = true
    }

    suspend fun onLanguageChanged(langCode: String) {
        currentLanguage = normalizeLang(langCode)
        ensureLoaded("en")
        ensureLoaded(currentLanguage)
    }

    fun t(key: String): String {
        val lang = normalizeLang(currentLanguage)
        return strings[lang]?.get(key)
            ?: strings["en"]?.get(key)
            ?: key
    }

    fun daysCount(n: Int): String = tPlural("duration_days", n)
    fun hoursCount(n: Int): String = tPlural("duration_hours", n)
    fun minutesCount(n: Int): String = tPlural("duration_minutes", n)

    fun tPlural(key: String, count: Int): String {
        val template = t(key)
        if (!template.contains("plural")) {
            return template.replace("{count}", count.toString())
        }
        return formatPluralTemplate(template, count)
    }

    private suspend fun ensureLoaded(lang: String) {
        val normalized = normalizeLang(lang)
        if (strings.containsKey(normalized)) return
        strings[normalized] = loadLang(normalized)
    }

    private fun loadLang(lang: String): Map<String, String> {
        val text = loadLocaleResourceText("locales/$lang.json") ?: return emptyMap()
        return try {
            val root = json.parseToJsonElement(text).jsonObject
            buildMap(root.size) {
                for ((k, v) in root) {
                    put(k, v.jsonPrimitive.content)
                }
            }
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    private fun normalizeLang(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return "en"

        val supported = setOf(
            "ar", "bg", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr",
            "hi", "hu", "id", "it", "ja", "ko", "lt", "lv", "nl", "no", "pl",
            "pt-BR", "ro", "ru", "sk", "sl", "sr", "sv", "tr", "uk", "zh"
        )

        val candidates = buildList {
            add(value)
            add(value.replace('_', '-'))
            if (value.contains("-")) add(value.substringBefore("-"))
            if (value.contains("_")) add(value.substringBefore("_"))
        }.distinct()

        for (c in candidates) {
            if (supported.contains(c)) return c
        }

        return "en"
    }

    private fun formatPluralTemplate(template: String, count: Int): String {
        val marker = "plural,"
        val p = template.indexOf(marker)
        if (p == -1) return template.replace("{count}", count.toString())

        val bodyRaw = template.substring(p + marker.length).trim()
        val body = if (bodyRaw.endsWith("}")) bodyRaw.dropLast(1).trim() else bodyRaw
        val forms = parsePluralForms(body)
        val category = pluralCategoryFor(normalizeLang(currentLanguage), count)
        val chosen = forms[category] ?: forms["other"] ?: template
        return chosen.replace("#", count.toString())
    }

    private fun parsePluralForms(body: String): Map<String, String> {
        val keys = listOf("zero", "one", "two", "few", "many", "other")
        val out = mutableMapOf<String, String>()
        var i = 0

        while (i < body.length) {
            while (i < body.length && body[i].isWhitespace()) i++

            var found: String? = null
            for (k in keys) {
                if (body.startsWith(k, i)) {
                    found = k
                    break
                }
            }

            if (found == null) {
                i++
                continue
            }

            i += found.length
            while (i < body.length && body[i].isWhitespace()) i++
            if (i >= body.length || body[i] != '{') continue

            val (txt, next) = readBracedText(body, i)
            out[found] = txt
            i = next
        }

        return out
    }

    private fun readBracedText(s: String, openIdx: Int): Pair<String, Int> {
        var depth = 0
        var i = openIdx
        val sb = StringBuilder()

        while (i < s.length) {
            val c = s[i]
            if (c == '{') {
                depth++
                if (depth > 1) sb.append(c)
            } else if (c == '}') {
                depth--
                if (depth == 0) return Pair(sb.toString(), i + 1)
                sb.append(c)
            } else {
                sb.append(c)
            }
            i++
        }

        return Pair(sb.toString(), s.length)
    }

    private fun pluralCategoryFor(lang: String, n: Int): String {
        val x = kotlin.math.abs(n)
        return when (lang) {
            "ru", "uk" -> {
                val m10 = x % 10
                val m100 = x % 100
                when {
                    m10 == 1 && m100 != 11 -> "one"
                    m10 in 2..4 && m100 !in 12..14 -> "few"
                    m10 == 0 || m10 in 5..9 || m100 in 11..14 -> "many"
                    else -> "other"
                }
            }
            "pl" -> {
                val m10 = x % 10
                val m100 = x % 100
                when {
                    x == 1 -> "one"
                    m10 in 2..4 && m100 !in 12..14 -> "few"
                    m10 == 0 || m10 == 1 || m10 in 5..9 || m100 in 12..14 -> "many"
                    else -> "other"
                }
            }
            "cs", "sk" -> when {
                x == 1 -> "one"
                x in 2..4 -> "few"
                else -> "other"
            }
            "sl" -> when (x % 100) {
                1 -> "one"
                2 -> "two"
                3, 4 -> "few"
                else -> "other"
            }
            "ar" -> when {
                x == 0 -> "zero"
                x == 1 -> "one"
                x == 2 -> "two"
                x % 100 in 3..10 -> "few"
                x % 100 in 11..99 -> "many"
                else -> "other"
            }
            "ja", "zh", "ko", "tr", "id", "hu" -> "other"
            else -> if (x == 1) "one" else "other"
        }
    }
}

expect fun loadLocaleResourceText(path: String): String?