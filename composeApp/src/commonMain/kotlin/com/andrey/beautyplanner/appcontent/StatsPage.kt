package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Appointment
import com.andrey.beautyplanner.ClientSuggestion
import com.andrey.beautyplanner.ClientSuggestions
import com.andrey.beautyplanner.Locales
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.math.roundToInt

private enum class StatsPeriod {
    DAY, WEEK, MONTH, YEAR, CUSTOM
}

private data class ServiceStat(
    val service: String,
    val count: Int,
    val revenueByCurrency: Map<String, Double>
)

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

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showClientPickerDialog by remember { mutableStateOf(false) }

    var customFromDate by remember { mutableStateOf(today.minus(30, DateTimeUnit.DAY)) }
    var customToDate by remember { mutableStateOf(today) }

    var clientQuery by remember { mutableStateOf("") }
    var isMultiCurrencyMode by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf<ClientSuggestion?>(null) }

    val allClients = remember(appointments) {
        ClientSuggestions.fromAppointments(appointments, limit = 1000)
    }

    val (fromDate, toDateInclusive) = remember(today, period, customFromDate, customToDate) {
        when (period) {
            StatsPeriod.DAY -> today to today
            StatsPeriod.WEEK -> today.minus(6, DateTimeUnit.DAY) to today
            StatsPeriod.MONTH -> today.minus(30, DateTimeUnit.DAY) to today
            StatsPeriod.YEAR -> today.minus(365, DateTimeUnit.DAY) to today
            StatsPeriod.CUSTOM -> {
                val safeFrom = if (customFromDate <= customToDate) customFromDate else customToDate
                val safeTo = if (customToDate >= customFromDate) customToDate else customFromDate
                safeFrom to safeTo
            }
        }
    }

    val filtered = remember(
        appointments,
        fromDate,
        toDateInclusive,
        clientQuery,
        selectedClient,
        isMultiCurrencyMode,
        AppSettings.selectedCurrency
    ) {
        val clientFilter = selectedClient?.displayName?.trim()?.lowercase()
            ?: clientQuery.trim().lowercase().takeIf { it.isNotBlank() }

        appointments
            .asSequence()
            .mapNotNull { a ->
                val d = runCatching { LocalDate.parse(a.dateString) }.getOrNull() ?: return@mapNotNull null
                d to a
            }
            .filter { (d, _) -> d >= fromDate && d <= toDateInclusive }
            .map { it.second }
            .filter { appt ->
                if (!isMultiCurrencyMode) {
                    appt.currency == AppSettings.selectedCurrency
                } else {
                    true
                }
            }
            .filter { appt ->
                if (clientFilter.isNullOrBlank()) {
                    true
                } else {
                    appt.clientName.trim().lowercase().contains(clientFilter)
                }
            }
            .toList()
    }

    val totalCount = filtered.size

    val totalHours = filtered.sumOf { a ->
        if (a.durationMinutes > 0) a.durationMinutes / 60.0
        else a.durationHours.toDouble()
    }.roundToInt()

    val revenue = filtered.sumOf { a ->
        a.price.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    val revenueByCurrency = remember(filtered, AppSettings.selectedCurrency) {
        filtered
            .groupBy { it.currency.ifBlank { AppSettings.selectedCurrency } }
            .mapValues { (_, items) ->
                items.sumOf { appt ->
                    appt.price.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                }
            }
    }

    val byService = remember(filtered, AppSettings.selectedCurrency) {
        filtered
            .groupBy { a ->
                val s = a.serviceName
                if (s.startsWith("service_")) Locales.t(s) else s
            }
            .map { (service, list) ->
                val count = list.size
                val serviceRevenueByCurrency = list
                    .groupBy { it.currency.ifBlank { AppSettings.selectedCurrency } }
                    .mapValues { (_, currencyList) ->
                        currencyList.sumOf {
                            it.price.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                        }
                    }

                ServiceStat(
                    service = service,
                    count = count,
                    revenueByCurrency = serviceRevenueByCurrency
                )
            }
            .sortedWith(
                compareByDescending<ServiceStat> { it.revenueByCurrency.values.sum() }
                    .thenByDescending { it.count }
            )
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

        Text(
            text = Locales.t("stats_filters"),
            fontSize = (16 * fontScale).sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryText
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = Locales.t("multi_currency_mode_all"),
                fontSize = (15 * fontScale).sp,
                color = primaryText,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = isMultiCurrencyMode,
                onCheckedChange = { isMultiCurrencyMode = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colors.primary,
                    checkedTrackColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodChip(Locales.t("stats_period_day"), period == StatsPeriod.DAY) {
                period = StatsPeriod.DAY
            }
            PeriodChip(Locales.t("stats_period_week"), period == StatsPeriod.WEEK) {
                period = StatsPeriod.WEEK
            }
            PeriodChip(Locales.t("stats_period_month"), period == StatsPeriod.MONTH) {
                period = StatsPeriod.MONTH
            }
            PeriodChip(Locales.t("stats_period_year"), period == StatsPeriod.YEAR) {
                period = StatsPeriod.YEAR
            }
        }

        OutlinedButton(
            onClick = { period = StatsPeriod.CUSTOM },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = if (period == StatsPeriod.CUSTOM) {
                    MaterialTheme.colors.primary.copy(alpha = 0.08f)
                } else {
                    Color.Transparent
                }
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (period == StatsPeriod.CUSTOM) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                }
            )
        ) {
            Text(
                text = Locales.t("stats_period_custom"),
                color = MaterialTheme.colors.primary
            )
        }

        if (period == StatsPeriod.CUSTOM) {
            Text(
                text = Locales.t("stats_custom_range"),
                fontSize = (14 * fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showFromDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${Locales.t("stats_date_from")}: $customFromDate",
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { showToDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${Locales.t("stats_date_to")}: $customToDate",
                        maxLines = 1
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { showClientPickerDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text(Locales.t("stats_client_filter_button"))
        }

        if (clientQuery.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = Locales.t("stats_selected_client"),
                        fontSize = (12 * fontScale).sp,
                        color = secondaryText
                    )

                    Text(
                        text = clientQuery,
                        fontSize = (15 * fontScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText
                    )
                }
            }

            TextButton(
                onClick = {
                    clientQuery = ""
                    selectedClient = null
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(Locales.t("stats_clear_client_filter"))
            }
        }

        Text(
            text = "${Locales.t("stats_range")}: $fromDate — $toDateInclusive",
            fontSize = (13 * fontScale).sp,
            color = hintText
        )

        Divider()

        if (!isMultiCurrencyMode) {
            StatRow(
                label = Locales.t("stats_revenue"),
                value = formatMoney(revenue, AppSettings.selectedCurrency),
                primaryText = primaryText
            )
        } else {
            RevenueByCurrencyBlock(
                label = Locales.t("stats_revenue"),
                revenueByCurrency = revenueByCurrency,
                primaryText = primaryText
            )
        }

        StatRow(
            label = Locales.t("stats_count"),
            value = totalCount.toString(),
            primaryText = primaryText
        )

        StatRow(
            label = Locales.t("stats_hours"),
            value = Locales.hoursCount(totalHours),
            primaryText = primaryText
        )

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
                    revenueByCurrency = s.revenueByCurrency,
                    isMultiCurrencyMode = isMultiCurrencyMode,
                    selectedCurrency = AppSettings.selectedCurrency,
                    fontScale = fontScale,
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showClientPickerDialog) {
        ClientPickerDialog(
            clients = allClients,
            selectedClientName = selectedClient?.displayName ?: clientQuery.takeIf { it.isNotBlank() },
            onDismiss = { showClientPickerDialog = false },
            onClear = {
                clientQuery = ""
                selectedClient = null
                showClientPickerDialog = false
            },
            onSelect = { client ->
                selectedClient = client
                clientQuery = client.displayName
                showClientPickerDialog = false
            }
        )
    }

    if (showFromDatePicker) {
        StatsDatePickerDialog(
            title = Locales.t("stats_pick_start_date"),
            initialSelectedDate = customFromDate,
            initialMonthDate = customFromDate,
            onDismiss = { showFromDatePicker = false },
            onConfirm = { picked ->
                customFromDate = picked
                showFromDatePicker = false
            }
        )
    }

    if (showToDatePicker) {
        StatsDatePickerDialog(
            title = Locales.t("stats_pick_end_date"),
            initialSelectedDate = customToDate,
            initialMonthDate = customToDate,
            onDismiss = { showToDatePicker = false },
            onConfirm = { picked ->
                customToDate = picked
                showToDatePicker = false
            }
        )
    }
}

@Composable
private fun PeriodChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val modifier = Modifier.height(38.dp)

    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Text(text, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text(text, maxLines = 1)
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    primaryText: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = primaryText
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            color = primaryText
        )
    }
}

@Composable
private fun RevenueByCurrencyBlock(
    label: String,
    revenueByCurrency: Map<String, Double>,
    primaryText: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = primaryText
        )

        if (revenueByCurrency.isEmpty()) {
            Text(
                text = "0",
                fontWeight = FontWeight.SemiBold,
                color = primaryText
            )
        } else {
            revenueByCurrency
                .toList()
                .sortedBy { it.first }
                .forEach { (currencyCode, amount) ->
                    Text(
                        text = formatMoney(amount, currencyCode),
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText
                    )
                }
        }
    }
}

@Composable
private fun ServiceRow(
    service: String,
    count: Int,
    revenueByCurrency: Map<String, Double>,
    isMultiCurrencyMode: Boolean,
    selectedCurrency: String,
    fontScale: Float,
    primaryText: Color,
    secondaryText: Color
) {
    val revenueText = if (!isMultiCurrencyMode) {
        formatMoney(
            revenueByCurrency[selectedCurrency] ?: 0.0,
            selectedCurrency
        )
    } else {
        formatMoneyCompactList(revenueByCurrency)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = service,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = revenueText,
                color = primaryText,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "${Locales.t("stats_procedures_done")}: $count",
            color = secondaryText,
            fontSize = (13 * fontScale).sp
        )

        if (isMultiCurrencyMode && revenueByCurrency.size > 1) {
            Spacer(modifier = Modifier.height(4.dp))
            revenueByCurrency
                .toList()
                .sortedBy { it.first }
                .forEach { (currencyCode, amount) ->
                    Text(
                        text = "• ${formatMoney(amount, currencyCode)}",
                        color = secondaryText,
                        fontSize = (12 * fontScale).sp
                    )
                }
        }
    }
}

private fun formatMoney(
    v: Double,
    currencyCode: String
): String {
    val rounded = (v * 100).roundToInt() / 100.0
    val s = if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
    return AppSettings.formatMoneyAmount(
        amount = s,
        currencyCode = currencyCode
    )
}

private fun formatMoneyCompactList(
    revenueByCurrency: Map<String, Double>
): String {
    if (revenueByCurrency.isEmpty()) return "0"
    return revenueByCurrency
        .toList()
        .sortedBy { it.first }
        .joinToString(" • ") { (currencyCode, amount) ->
            formatMoney(amount, currencyCode)
        }
}