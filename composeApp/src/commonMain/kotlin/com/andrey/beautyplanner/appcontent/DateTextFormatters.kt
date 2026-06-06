package com.andrey.beautyplanner.appcontent

import com.andrey.beautyplanner.Locales
import kotlinx.datetime.LocalDate

fun localizedMonthTitle(date: LocalDate): String {
    val monthKey = when (date.monthNumber) {
        1 -> "month_jan"
        2 -> "month_feb"
        3 -> "month_mar"
        4 -> "month_apr"
        5 -> "month_may"
        6 -> "month_jun"
        7 -> "month_jul"
        8 -> "month_aug"
        9 -> "month_sep"
        10 -> "month_oct"
        11 -> "month_nov"
        12 -> "month_dec"
        else -> "month_jan"
    }
    return "${Locales.t(monthKey)} ${date.year}"
}