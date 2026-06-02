package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DefaultActionButtonMaxWidth = 420.dp
private val DefaultActionButtonHeight = 44.dp
private val DefaultActionButtonShape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .widthIn(max = DefaultActionButtonMaxWidth)
                .fillMaxWidth()
                .height(DefaultActionButtonHeight),
            shape = DefaultActionButtonShape
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .widthIn(max = DefaultActionButtonMaxWidth)
                .fillMaxWidth()
                .height(DefaultActionButtonHeight),
            shape = DefaultActionButtonShape
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MaterialTheme.colors.onSurface
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.45f)
                }
            )
        }
    }
}

@Composable
fun DangerActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .widthIn(max = DefaultActionButtonMaxWidth)
                .fillMaxWidth()
                .height(DefaultActionButtonHeight),
            shape = DefaultActionButtonShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Red,
                disabledContentColor = Color.Red.copy(alpha = 0.45f)
            )
        ) {
            Text(
                text = text,
                color = if (enabled) Color.Red else Color.Red.copy(alpha = 0.45f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}