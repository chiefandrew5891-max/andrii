package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Locales

@Composable
fun AuthEmailScreen(
    isRegisterMode: Boolean,
    errorMessage: String?,
    onSubmit: (email: String, password: String, confirmPassword: String) -> Unit,
    onForgotPassword: (email: String) -> Unit
) {
    val fontScale = AppSettings.getFontScale()
    val onBg = MaterialTheme.colors.onBackground

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }

    CenteredNarrowContentContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Text(
                text = Locales.t("auth_email_title"),
                fontSize = (24 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = onBg
            )

            Text(
                text = if (isRegisterMode) {
                    Locales.t("auth_email_mode_register")
                } else {
                    Locales.t("auth_email_mode_sign_in")
                },
                fontSize = (14 * fontScale).sp,
                color = onBg.copy(alpha = 0.72f)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                modifier = Modifier.fillMaxSize(),
                singleLine = true,
                label = { Text(Locales.t("auth_email_field")) }
            )

            OutlinedTextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                modifier = Modifier.fillMaxSize(),
                singleLine = true,
                label = { Text(Locales.t("auth_password_field")) },
                visualTransformation = PasswordVisualTransformation()
            )

            if (isRegisterMode) {
                OutlinedTextField(
                    value = confirmPasswordState.value,
                    onValueChange = { confirmPasswordState.value = it },
                    modifier = Modifier.fillMaxSize(),
                    singleLine = true,
                    label = { Text(Locales.t("auth_password_confirm_field")) },
                    visualTransformation = PasswordVisualTransformation()
                )
            } else {
                TextButton(
                    onClick = {
                        onForgotPassword(emailState.value)
                    }
                ) {
                    Text(Locales.t("auth_password_reset"))
                }
            }

            PrimaryActionButton(
                text = if (isRegisterMode) {
                    Locales.t("auth_email_submit_register")
                } else {
                    Locales.t("auth_email_submit_sign_in")
                },
                onClick = {
                    onSubmit(
                        emailState.value,
                        passwordState.value,
                        confirmPasswordState.value
                    )
                }
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    fontSize = (13 * fontScale).sp,
                    color = MaterialTheme.colors.error
                )
            }
        }
    }
}