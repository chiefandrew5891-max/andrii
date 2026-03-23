package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.Locales
import kotlinx.datetime.*

private enum class StatsPeriod { DAY, WEEK, MONTH, YEAR }

@Composable
fun StatsPage(
    appointments: List<Appointment>,
    today: LocalDate
) {
    val fontScale = AppSettings.getFontScale()
    var period by remember { mutableStateOf(StatsPeriod.MONTH) }

    val tz = TimeZone.currentSystemDefault()
    val (fromDate, toDateInclusive) = remember(period, today) {
        when (period) {
            StatsPeriod.DAY -> today to today
            StatsPeriod.WEEK -> today.minus(6, DateTimeUnit.DAY) to today
            StatsPeriod.MONTH -> LocalDate(today.year, today.month, 1) to today
            StatsPeriod.YEAR -> LocalDate(today.year, 1, 1) to today
        }
    }

    val filtered = remember(appointments, fromDate, toDateInclusive) {
        appointments.filter { appt ->
            val d = runCatching { LocalDate.parse(appt.dateString) }.getOrNull() ?: return@filter false
            d >= fromDate && d <= toDateInclusive
        }
    }

    val revenue = remember(filtered) { filtered.sumOf { parsePriceToDouble(it.price) } }
    val totalHours = remember(filtered) { filtered.sumOf { it.durationHours.coerceAtLeast(0) } }
    val totalCount = filtered.size

    val byService = remember(filtered) {
        filtered
            .groupBy { it.serviceName.trim() }
            .map { (service, list) ->
                val count = list.size
                val serviceRevenue = list.sumOf { parsePriceToDouble(it.price) }
                ServiceStat(
                    service = if (service.isBlank()) Locales.t("stats_unknown_service") else service,
                    count = count,
                    revenue = serviceRevenue
                )
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
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PeriodChip(Locales.t("stats_period_day"), period == StatsPeriod.DAY) { period = StatsPeriod.DAY }
            PeriodChip(Locales.t("stats_period_week"), period == StatsPeriod.WEEK) { period = StatsPeriod.WEEK }
            PeriodChip(Locales.t("stats_period_month"), period == StatsPeriod.MONTH) { period = StatsPeriod.MONTH }
            PeriodChip(Locales.t("stats_period_year"), period == StatsPeriod.YEAR) { period = StatsPeriod.YEAR }
        }

        Text(
            text = "${Locales.t("stats_range")}: ${fromDate} — ${toDateInclusive}",
            fontSize = (13 * fontScale).sp,
            color = Color.Gray
        )

        Divider()

        StatRow(Locales.t("stats_revenue"), formatMoneyEur(revenue))
        StatRow(Locales.t("stats_count"), totalCount.toString())
        StatRow(Locales.t("stats_hours"), Locales.hoursCount(totalHours))

        Divider()

        Text(
            text = Locales.t("stats_top_services"),
            fontSize = (16 * fontScale).sp,
            fontWeight = FontWeight.SemiBold
        )

        if (byService.isEmpty()) {
            Text(
                text = Locales.t("stats_empty"),
                color = Color.Gray,
                fontSize = (14 * fontScale).sp
            )
        } else {
            byService.forEach { s ->
                ServiceRow(
                    service = s.service,
                    count = s.count,
                    revenue = s.revenue
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
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ServiceRow(service: String, count: Int, revenue: Double) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(service, fontWeight = FontWeight.SemiBold)
            Text(formatMoneyEur(revenue))
        }
        Text(
            text = "${Locales.t("stats_procedures_done")}: $count",
            color = Color.Gray,
            fontSize = 13.sp
        )
        Divider(color = Color.LightGray.copy(alpha = 0.35f))
    }
}

private data class ServiceStat(val service: String, val count: Int, val revenue: Double)

private fun parsePriceToDouble(price: String): Double {
    val cleaned = price
        .trim()
        .replace(" ", "")
        .replace("\u00A0", "")
        .replace(",", ".")
        .filter { it.isDigit() || it == '.' || it == '-' }

    return cleaned.toDoubleOrNull() ?: 0.0
}

private fun formatMoneyEur(value: Double): String {
    val rounded = kotlin.math.round(value * 100) / 100
    val eur = Locales.t("currency_eur")
    return "$rounded $eur"
}