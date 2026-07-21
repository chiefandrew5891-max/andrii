package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.ProfileImagePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale


@Composable
fun PersonalInfoSettingsScreen() {
    val fontScale = AppSettings.getFontScale()
    val onBg = MaterialTheme.colors.onBackground
    val onSurface = MaterialTheme.colors.onSurface

    var userNameDraft by remember { mutableStateOf(AppSettings.ownerName) }
    var phoneDraft by remember { mutableStateOf(AppSettings.profilePhone) }
    var avatarUrlDraft by remember { mutableStateOf(AppSettings.profileAvatarUrl) }
    var avatarBase64Draft by remember { mutableStateOf(AppSettings.profileAvatarBase64) }
    var phoneVisibleDraft by remember { mutableStateOf(AppSettings.profilePhoneVisible) }

    val hasChanges =
        userNameDraft.trim() != AppSettings.ownerName.trim() ||
                phoneDraft.trim() != AppSettings.profilePhone.trim() ||
                avatarUrlDraft.trim() != AppSettings.profileAvatarUrl.trim() ||
                avatarBase64Draft != AppSettings.profileAvatarBase64 ||
                phoneVisibleDraft != AppSettings.profilePhoneVisible

    val hasPreviewData = userNameDraft.trim().isNotBlank() ||
            phoneDraft.trim().isNotBlank() ||
            avatarUrlDraft.trim().isNotBlank() ||
            avatarBase64Draft.isNotBlank()

    val avatarBitmap = rememberProfileAvatarBitmap(avatarBase64Draft)

    CenteredNarrowContentContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Личная информация",
                fontSize = (22 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = onBg
            )

            Text(
                text = "Здесь можно настроить информацию о себе для профиля мастера.",
                fontSize = (14 * fontScale).sp,
                color = onBg.copy(alpha = 0.7f)
            )

            Divider()
            if (hasPreviewData) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap,
                                contentDescription = "Аватар профиля",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userNameDraft.trim().take(1).ifBlank { "?" }.uppercase(),
                                    fontSize = (72 * fontScale).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }

                    if (userNameDraft.trim().isNotBlank()) {
                        Text(
                            text = userNameDraft.trim(),
                            fontSize = (24 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = onBg,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (phoneDraft.trim().isNotBlank() && phoneVisibleDraft) {
                        Text(
                            text = phoneDraft.trim(),
                            fontSize = (14 * fontScale).sp,
                            color = onSurface.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Divider()
            }

            ProfileTextField(
                title = "Имя пользователя",
                value = userNameDraft,
                onValueChange = { userNameDraft = it },
                placeholder = "Введите имя пользователя"
            )

            ProfileTextField(
                title = "Номер телефона",
                value = phoneDraft,
                onValueChange = { phoneDraft = it },
                placeholder = "Введите номер телефона"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Показывать номер телефона",
                    fontSize = (16 * fontScale).sp,
                    color = onSurface
                )
                AppSwitch(
                    checked = phoneVisibleDraft,
                    onCheckedChange = { phoneVisibleDraft = it }
                )
            }

            ProfileTextField(
                title = "Аватар (ссылка)",
                value = avatarUrlDraft,
                onValueChange = { avatarUrlDraft = it },
                placeholder = "Вставьте ссылку на изображение"
            )

            SecondaryActionButton(
                text = "Выбрать фото с устройства",
                onClick = {
                    ProfileImagePicker.pickImage { base64 ->
                        if (!base64.isNullOrBlank()) {
                            avatarBase64Draft = base64
                        }
                    }
                }
            )

            SecondaryActionButton(
                text = "Удалить выбранное фото",
                onClick = {
                    avatarBase64Draft = ""
                },
                enabled = avatarBase64Draft.isNotBlank()
            )

            Text(
                text = "Пока здесь используется только ссылка на изображение. Позже можно заменить на загрузку файла.",
                fontSize = (12 * fontScale).sp,
                color = onSurface.copy(alpha = 0.65f),
                lineHeight = (18 * fontScale).sp
            )

            Spacer(Modifier.height(6.dp))

            PrimaryActionButton(
                text = "Сохранить",
                onClick = {
                    AppSettings.ownerName = userNameDraft.trim()
                    AppSettings.profilePhone = phoneDraft.trim()
                    AppSettings.profilePhoneVisible = phoneVisibleDraft
                    AppSettings.profileAvatarUrl = avatarUrlDraft.trim()
                    AppSettings.profileAvatarBase64 = avatarBase64Draft
                    AppSettings.persist()
                },
                enabled = hasChanges
            )
        }
    }
}

@Composable
private fun ProfileTextField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val fontScale = AppSettings.getFontScale()
    val onSurface = MaterialTheme.colors.onSurface

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            fontSize = (16 * fontScale).sp,
            fontWeight = FontWeight.SemiBold,
            color = onSurface.copy(alpha = 0.85f)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = onSurface.copy(alpha = 0.50f)
                )
            },
            textStyle = TextStyle(
                fontFamily = appFontFamily(),
                fontSize = (16 * fontScale).sp,
                color = onSurface
            ),
            colors = androidx.compose.material.TextFieldDefaults.outlinedTextFieldColors(
                textColor = onSurface,
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = onSurface.copy(alpha = 0.28f),
                focusedLabelColor = MaterialTheme.colors.primary,
                unfocusedLabelColor = onSurface.copy(alpha = 0.68f),
                cursorColor = MaterialTheme.colors.primary,
                backgroundColor = MaterialTheme.colors.surface,
                placeholderColor = onSurface.copy(alpha = 0.50f)
            )
        )
    }
}