package com.andrey.beautyplanner.appcontent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.andrey.beautyplanner.AppSettings
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.ProfileImageCropper
import com.andrey.beautyplanner.rememberProfileAvatarBitmap

/**
 * Full-screen dialog that lets the user drag a selected photo to choose the crop region.
 * On confirm, the cropped 512×512 JPEG base64 is passed to [onConfirm].
 */
@Composable
fun AvatarCropEditorDialog(
    rawBase64: String,
    onConfirm: (croppedBase64: String) -> Unit,
    onDismiss: () -> Unit
) {
    val fontScale = AppSettings.getFontScale()
    val onSurface = MaterialTheme.colors.onSurface
    val primary = MaterialTheme.colors.primary

    val avatarBitmap = rememberProfileAvatarBitmap(rawBase64)

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Physical pixel size of the crop container, tracked after layout
    var containerSizePx by remember { mutableStateOf(0f) }

    // Max drag bounds (updated when avatarBitmap or containerSizePx changes)
    val maxOffsetX: Float
    val maxOffsetY: Float
    if (avatarBitmap != null && containerSizePx > 0f) {
        val bitmapW = avatarBitmap.width.toFloat()
        val bitmapH = avatarBitmap.height.toFloat()
        val scale = containerSizePx / minOf(bitmapW, bitmapH)
        val displayedW = bitmapW * scale
        val displayedH = bitmapH * scale
        maxOffsetX = maxOf(0f, (displayedW - containerSizePx) / 2f)
        maxOffsetY = maxOf(0f, (displayedH - containerSizePx) / 2f)
    } else {
        maxOffsetX = 0f
        maxOffsetY = 0f
    }

    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Locales.t("avatar_crop_title"),
                    fontSize = (18 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = Locales.t("avatar_crop_hint"),
                    fontSize = (13 * fontScale).sp,
                    color = onSurface.copy(alpha = 0.68f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Crop container: square, fills available width
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
                        .clipToBounds()
                        .onGloballyPositioned { coords ->
                            containerSizePx = coords.size.width.toFloat()
                        }
                        .pointerInput(maxOffsetX, maxOffsetY) {
                            detectDragGestures { _, delta ->
                                offsetX = (offsetX + delta.x).coerceIn(-maxOffsetX, maxOffsetX)
                                offsetY = (offsetY + delta.y).coerceIn(-maxOffsetY, maxOffsetY)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .graphicsLayer(
                                    translationX = offsetX,
                                    translationY = offsetY
                                ),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Loading / decode placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "...",
                                color = onSurface.copy(alpha = 0.45f),
                                fontSize = 28.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { if (!isProcessing) onDismiss() }
                    ) {
                        Text(
                            text = Locales.t("avatar_crop_cancel"),
                            color = onSurface.copy(alpha = 0.72f),
                            fontSize = (15 * fontScale).sp
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    TextButton(
                        modifier = Modifier.weight(1f),
                        enabled = avatarBitmap != null && !isProcessing,
                        onClick = {
                            if (isProcessing) return@TextButton
                            isProcessing = true
                            ProfileImageCropper.cropImage(
                                base64 = rawBase64,
                                offsetXPx = offsetX,
                                offsetYPx = offsetY,
                                displaySizePx = containerSizePx,
                                targetSize = 512
                            ) { cropped ->
                                isProcessing = false
                                if (!cropped.isNullOrBlank()) {
                                    onConfirm(cropped)
                                } else {
                                    // Fallback: pass the raw base64 if cropping failed
                                    onConfirm(rawBase64)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = if (isProcessing) "..." else Locales.t("avatar_crop_confirm"),
                            color = if (avatarBitmap != null && !isProcessing) primary else onSurface.copy(alpha = 0.38f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (15 * fontScale).sp
                        )
                    }
                }
            }
        }
    }
}
