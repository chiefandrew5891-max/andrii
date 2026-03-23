package com.andrey.beautyplanner.appcontent

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.andrey.beautyplanner.appcontent.approot.AppRootChrome
import com.andrey.beautyplanner.appcontent.approot.AppRootContent
import com.andrey.beautyplanner.appcontent.approot.AppRootDialogs
import com.andrey.beautyplanner.appcontent.approot.rememberAppRootState

@Composable
fun AppRoot() {
    val state = rememberAppRootState()

    MaterialTheme(colors = state.colors, typography = state.customTypography) {
        AppRootChrome(state = state) { padding ->
            AppRootContent(state = state, padding = padding)
            AppRootDialogs(state = state)
        }
    }
}