package com.andrey.beautyplanner

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object LocalesDebug {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun debugParseLocaleText(tag: String, text: String?) {
        println("LocalesDebug: [$tag] text is null = ${text == null}")
        if (text == null) return

        println("LocalesDebug: [$tag] length = ${text.length}")
        println("LocalesDebug: [$tag] preview =")
        println(text.take(500))

        try {
            val root = json.parseToJsonElement(text).jsonObject
            println("LocalesDebug: [$tag] parsed keys count = ${root.size}")
            println("LocalesDebug: [$tag] first keys = ${root.keys.take(10)}")
            if ("auth_title" in root) {
                println("LocalesDebug: [$tag] auth_title = ${root["auth_title"]?.jsonPrimitive?.content}")
            }
        } catch (t: Throwable) {
            println("LocalesDebug: [$tag] parse error = ${t::class.simpleName}: ${t.message}")
        }
    }
}