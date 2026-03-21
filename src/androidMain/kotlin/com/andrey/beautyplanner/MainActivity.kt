package com.andrey.beautyplanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    private val requestNotificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // MVP: ничего не делаем. Если не дал — уведомления просто не появятся.
            // Позже можно показывать подсказку в SettingsPage.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Важно для DataManager на Android (filesDir)
        AndroidAppContext.context = applicationContext

        // Растягиваем интерфейс
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Android 13+ требует runtime permission на уведомления
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