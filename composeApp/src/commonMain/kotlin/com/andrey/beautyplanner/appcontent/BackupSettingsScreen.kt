package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Locales

@Composable
fun BackupSettingsScreen(
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearDatabase: () -> Unit,
    dbOpsAllowed: Boolean
) {
    val fontScale = AppSettings.getFontScale()
    val onSurface = MaterialTheme.colors.onSurface
    val onBg = MaterialTheme.colors.onBackground

    CenteredNarrowContentContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = Locales.t("backup_settings_title"),
                fontSize = (22 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = onBg
            )

            Text(
                text = Locales.t("backup_settings_hint"),
                fontSize = (14 * fontScale).sp,
                color = onBg.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(8.dp))

            PrimaryActionButton(
                text = Locales.t("export_db"),
                onClick = onExport,
                enabled = dbOpsAllowed
            )

            SecondaryActionButton(
                text = Locales.t("import_db"),
                onClick = onImport,
                enabled = dbOpsAllowed
            )

            DangerActionButton(
                text = Locales.t("clear_db"),
                onClick = onClearDatabase,
                enabled = dbOpsAllowed
            )

            if (!dbOpsAllowed) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = Locales.t("backup_pin_required_hint"),
                    color = onSurface.copy(alpha = 0.60f),
                    fontSize = (12 * fontScale).sp
                )
            }
        }
    }
}