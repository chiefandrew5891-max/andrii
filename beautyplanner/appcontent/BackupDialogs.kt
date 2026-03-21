package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andrey.beautyplanner.Locales

@Composable
fun ExportBackupDialog(
    json: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Locales.t("export_db")) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(Locales.t("export_hint"), style = MaterialTheme.typography.body2)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = json,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                    readOnly = true,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(Locales.t("close")) }
        }
    )
}

@Composable
fun ImportBackupDialog(
    json: String,
    errorText: String?,
    onJsonChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Locales.t("import_db")) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(Locales.t("import_hint"), style = MaterialTheme.typography.body2)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = json,
                    onValueChange = onJsonChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                    singleLine = false
                )

                if (errorText != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorText,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onImport) { Text(Locales.t("import_btn")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Locales.t("cancel")) }
        }
    )
}