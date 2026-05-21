package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AccessState
import com.andrey.beautyplanner.AccessTier
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Locales

@Composable
fun DeveloperAccessScreen(
    accessState: AccessState,
    onEnablePremium: () -> Unit,
    onDisablePremium: () -> Unit,
    onResetTrial: () -> Unit,
    onExpireTrial: () -> Unit,
    onLogoutDeveloperMode: () -> Unit
) {
    val fontScale = AppSettings.getFontScale()
    val onSurface = MaterialTheme.colors.onSurface
    val onBg = MaterialTheme.colors.onBackground

    val tierText = when (accessState.tier) {
        AccessTier.TRIAL -> Locales.t("premium_status_trial")
        AccessTier.FREE_LIMITED -> Locales.t("premium_status_free")
        AccessTier.PREMIUM -> Locales.t("premium_status_premium")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = Locales.t("developer_mode_title"),
            fontSize = (22 * fontScale).sp,
            fontWeight = FontWeight.Bold,
            color = onBg
        )

        Text(
            text = Locales.t("developer_mode_hint"),
            fontSize = (14 * fontScale).sp,
            color = onBg.copy(alpha = 0.7f)
        )

        Divider()

        Text(
            text = "${Locales.t("developer_current_tier")}: $tierText",
            color = onSurface,
            fontSize = (16 * fontScale).sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "${Locales.t("developer_trial_active")}: ${accessState.isTrialActive}",
            color = onSurface.copy(alpha = 0.8f),
            fontSize = (14 * fontScale).sp
        )

        Text(
            text = "${Locales.t("developer_trial_days_left")}: ${accessState.trialDaysLeft}",
            color = onSurface.copy(alpha = 0.8f),
            fontSize = (14 * fontScale).sp
        )

        Text(
            text = "${Locales.t("developer_trial_started_at")}: ${AppSettings.trialStartedAtMillis}",
            color = onSurface.copy(alpha = 0.7f),
            fontSize = (13 * fontScale).sp
        )

        Text(
            text = "${Locales.t("developer_premium_unlocked")}: ${AppSettings.premiumUnlocked}",
            color = onSurface.copy(alpha = 0.7f),
            fontSize = (13 * fontScale).sp
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onEnablePremium,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text(Locales.t("developer_enable_premium"))
        }

        OutlinedButton(
            onClick = onDisablePremium,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text(Locales.t("developer_disable_premium"))
        }

        OutlinedButton(
            onClick = onResetTrial,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text(Locales.t("developer_reset_trial"))
        }

        OutlinedButton(
            onClick = onExpireTrial,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text(Locales.t("developer_expire_trial"))
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onLogoutDeveloperMode,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text(Locales.t("developer_logout"))
        }
    }
}