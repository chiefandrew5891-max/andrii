package com.andrey.beautyplanner

import androidx.compose.runtime.*
import com.andrey.beautyplanner.appcontent.AppRoot

@Composable
fun App() {
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val startCode = AppSettings.languageCodes[AppSettings.selectedLanguage] ?: "ru"
        Locales.currentLanguage = startCode
        Locales.init()
        ready = true
    }

    if (ready) {
        AppRoot()
    }
}