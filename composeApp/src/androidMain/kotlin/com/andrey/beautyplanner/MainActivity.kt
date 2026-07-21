package com.andrey.beautyplanner

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import android.content.Intent
import com.andrey.beautyplanner.auth.GoogleSignInFallbackBridge
import com.andrey.beautyplanner.auth.GoogleSignInFallbackResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CompletableDeferred
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {

    private val requestNotificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // MVP: nothing
        }

    private val requestContactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            ContactsAutocompleteAndroid.permissionGranted = granted
            ContactsAutocompleteAndroid.permissionRequestedOnce = true
        }

    // --- Backup: Android system pickers ---
    private var pendingExportJson: String? = null
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingExportJson
        pendingExportJson = null
        if (uri == null || json == null) return@registerForActivityResult

        runCatching {
            contentResolver.openOutputStream(uri)?.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
            }
        }
    }

    private var pendingProfileImagePicked: ((String?) -> Unit)? = null

    private val profileImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val callback = pendingProfileImagePicked
        pendingProfileImagePicked = null

        if (uri == null) {
            callback?.invoke(null)
            return@registerForActivityResult
        }

        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        runCatching {
            val mime = contentResolver.getType(uri).orEmpty().lowercase()
            val allowed = mime == "image/jpeg" || mime == "image/jpg" || mime == "image/png"
            if (!allowed) {
                callback?.invoke(null)
                return@registerForActivityResult
            }

            val base64 = contentResolver.openInputStream(uri)?.use { input ->
                val original = BitmapFactory.decodeStream(input) ?: return@use null
                val cropped = cropCenterSquare(original)
                val resized = resizeBitmap(cropped, 512)
                bitmapToBase64Jpeg(resized, 82)
            }

            callback?.invoke(base64)
        }.onFailure {
            callback?.invoke(null)
        }
    }

    private var pendingImportOnPicked: ((String) -> Unit)? = null
    private var pendingImportOnError: ((String) -> Unit)? = null

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val onPicked = pendingImportOnPicked
        val onError = pendingImportOnError
        pendingImportOnPicked = null
        pendingImportOnError = null

        if (uri == null) return@registerForActivityResult

        runCatching {
            val text = contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) }
                .orEmpty()

            if (text.isBlank()) onError?.invoke(Locales.t("backup_import_error_empty"))
            else onPicked?.invoke(text)
        }.onFailure {
            onError?.invoke(Locales.t("backup_import_error_read"))
        }
    }

    private var pendingGoogleFallbackResult: CompletableDeferred<GoogleSignInFallbackResult>? = null

    private val googleSignInFallbackLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val deferred = pendingGoogleFallbackResult
        pendingGoogleFallbackResult = null

        if (deferred == null) return@registerForActivityResult

        if (result.resultCode != RESULT_OK) {
            deferred.complete(GoogleSignInFallbackResult.Cancelled)
            return@registerForActivityResult
        }

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            val idToken = account?.idToken
            if (idToken.isNullOrBlank()) {
                deferred.complete(
                    GoogleSignInFallbackResult.Error("Google ID token is missing")
                )
            } else {
                deferred.complete(
                    GoogleSignInFallbackResult.Success(idToken)
                )
            }
        } catch (e: Exception) {
            deferred.complete(
                GoogleSignInFallbackResult.Error(e.message ?: "Google sign-in failed")
            )
        }
    }

    private fun cropCenterSquare(source: Bitmap): Bitmap {
        val size = minOf(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        return Bitmap.createBitmap(source, x, y, size, size)
    }

    private fun resizeBitmap(source: Bitmap, targetSize: Int = 512): Bitmap {
        return Bitmap.createScaledBitmap(source, targetSize, targetSize, true)
    }

    private fun bitmapToBase64Jpeg(source: Bitmap, quality: Int = 82): String {
        val output = ByteArrayOutputStream()
        source.compress(Bitmap.CompressFormat.JPEG, quality, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.andrey.beautyplanner.notifications.NotificationsPlatform.init(applicationContext)
        AndroidAppContext.context = applicationContext
        AndroidAppContext.activity = this
        GoogleSignInFallbackBridge.launchSignInIntent = { intent: Intent, deferred ->
            pendingGoogleFallbackResult = deferred
            googleSignInFallbackLauncher.launch(intent)
        }

        ContactsAutocompleteAndroid.init(
            context = applicationContext,
            permissionChecker = {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            },
            requestPermission = {
                requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        )

        BackupFilePicker.exportImpl = { suggestedFileName, json ->
            val name = suggestedFileName.trim().ifBlank { "beautyplanner-backup" }
            val finalName = if (name.endsWith(".json", ignoreCase = true)) name else "$name.json"
            pendingExportJson = json
            exportLauncher.launch(finalName)
        }

        BackupFilePicker.importImpl = { onPicked, onError ->
            pendingImportOnPicked = onPicked
            pendingImportOnError = onError
            importLauncher.launch(arrayOf("application/json", "text/plain"))
        }

        ProfileImagePicker.pickImageImpl = { onImagePicked ->
            pendingProfileImagePicked = onImagePicked
            profileImageLauncher.launch(arrayOf("image/jpeg", "image/png"))
        }

        AppSettings.load()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        maybeRequestPostNotificationsPermission()

        setContent {
            App()
        }
    }

    private fun maybeRequestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val alreadyGranted = ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            requestNotificationsPermissionLauncher.launch(permission)
        }
    }
}