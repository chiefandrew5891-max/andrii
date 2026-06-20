package com.andrey.beautyplanner.appcontent

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.andrey.beautyplanner.generated.resources.Res
import com.andrey.beautyplanner.generated.resources.inter_bold
import com.andrey.beautyplanner.generated.resources.inter_extralight
import com.andrey.beautyplanner.generated.resources.inter_medium
import com.andrey.beautyplanner.generated.resources.inter_regular
import org.jetbrains.compose.resources.Font

@Composable
fun appFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.inter_extralight, FontWeight.ExtraLight),
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_bold, FontWeight.Bold)
    )
}