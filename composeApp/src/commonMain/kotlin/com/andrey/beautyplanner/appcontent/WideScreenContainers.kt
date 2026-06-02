package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CenteredContentContainer(
    modifier: Modifier = Modifier,
    maxWidth: androidx.compose.ui.unit.Dp = 840.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier.widthIn(max = maxWidth)
        ) {
            content()
        }
    }
}

@Composable
fun CenteredNarrowContentContainer(
    modifier: Modifier = Modifier,
    maxWidth: androidx.compose.ui.unit.Dp = 720.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier.widthIn(max = maxWidth)
        ) {
            content()
        }
    }
}