package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.Locales
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt

private enum class StatsPeriod { DAY, WEEK, MONTH, YEAR }

@Composable
fun StatsPage(
    appointments: List<Appointment>,
    today: LocalDate
) {
    val fontScale = AppSettings.getFontScale()

    val primaryText = UiColors.primaryText()
    val secondaryText = UiColors.secondaryText()
    val hintText = UiColors.hintText()

    var period by remember { mutableStateOf(StatsPeriod.MONTH) }

    val (fromDate, toDateInclusive) = remember(today, period) {
        val to = today
        val from = when (period) {
            StatsPeriod.DAY -> to
            StatsPeriod.WEEK -> to.minus(6, DateTimeUnit.DAY)
            StatsPeriod.MONTH -> to.minus(30, DateTimeUnit.DAY)
            StatsPeriod.YEAR -> to.minus(365, DateTimeUnit.DAY)
        }
        from to to
    }

    // Filter + aggregate
    val filtered = remember(appointments, fromDate, toDateInclusive) {
        appointments
            .asSequence()
            .mapNotNull { a ->
                val d = runCatching { LocalDate.parse(a.dateString) }.getOrNull() ?: return@mapNotNull null
                d to a
            }
            .filter { (d, _) -> d >= fromDate && d <= toDateInclusive }
            .map { it.second }
            .toList()
    }

    val totalCount = filtered.size
    val totalHours = filtered.sumOf { a -> if (a.durationMinutes > 0) a.durationMinutes / 60.0 else a.durationHours.toDouble() }
        .roundToInt()

    val revenue = filtered.sumOf { a ->
        a.price.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    data class ServiceStat(val service: String, val count: Int, val revenue: Double)

    val byService = remember(filtered) {
        filtered
            .groupBy { a ->
                val s = a.serviceName
                if (s.startsWith("service_")) Locales.t(s) else s
            }
            .map { (service, list) ->
                val count = list.size
                val serviceRevenue = list.sumOf { it.price.trim().replace(",", ".").toDoubleOrNull() ?: 0.0 }
                ServiceStat(service = service, count = count, revenue = serviceRevenue)
            }
            .sortedWith(compareByDescending<ServiceStat> { it.revenue }.thenByDescending { it.count })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = Locales.t("nav_stats"),
            fontSize = (22 * fontScale).sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodChip(Locales.t("stats_period_day"), period == StatsPeriod.DAY) { period = StatsPeriod.DAY }
            PeriodChip(Locales.t("stats_period_week"), period == StatsPeriod.WEEK) { period = StatsPeriod.WEEK }
            PeriodChip(Locales.t("stats_period_month"), period == StatsPeriod.MONTH) { period = StatsPeriod.MONTH }
            PeriodChip(Locales.t("stats_period_year"), period == StatsPeriod.YEAR) { period = StatsPeriod.YEAR }
        }

        Text(
            text = "${Locales.t("stats_range")}: $fromDate — $toDateInclusive",
            fontSize = (13 * fontScale).sp,
            color = hintText
        )

        Divider()

        StatRow(Locales.t("stats_revenue"), formatMoneyEur(revenue), primaryText)
        StatRow(Locales.t("stats_count"), totalCount.toString(), primaryText)
        StatRow(Locales.t("stats_hours"), Locales.hoursCount(totalHours), primaryText)

        Divider()

        Text(
            text = Locales.t("stats_top_services"),
            fontSize = (16 * fontScale).sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryText
        )

        if (byService.isEmpty()) {
            Text(
                text = Locales.t("stats_empty"),
                color = secondaryText,
                fontSize = (14 * fontScale).sp
            )
        } else {
            byService.forEach { s ->
                ServiceRow(
                    service = s.service,
                    count = s.count,
                    revenue = s.revenue,
                    fontScale = fontScale,
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PeriodChip(text: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
            Text(text)
        }
    } else {
        OutlinedButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
            Text(text)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, primaryText: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Medium, color = primaryText)
        Text(value, fontWeight = FontWeight.SemiBold, color = primaryText)
    }
}

@Composable
private fun ServiceRow(
    service: String,
    count: Int,
    revenue: Double,
    fontScale: Float,
    primaryText: androidx.compose.ui.graphics.Color,
    secondaryText: androidx.compose.ui.graphics.Color
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(service, fontWeight = FontWeight.SemiBold, color = primaryText)
            Text(formatMoneyEur(revenue), color = primaryText)
        }
        Text(
            text = "${Locales.t("stats_procedures_done")}: $count",
            color = secondaryText,
            fontSize = (13 * fontScale).sp
        )
    }
}

// Keeps existing format (EUR)
private fun formatMoneyEur(v: Double): String {
    val rounded = (v * 100).roundToInt() / 100.0
    val s = if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    return "$s €"
}