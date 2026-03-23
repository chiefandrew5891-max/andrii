package com.andrey.beautyplanner.appcontent.approot

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.Screen

@Composable
fun AppRootChrome(
    state: AppRootState,
    content: @Composable (PaddingValues) -> Unit
) {
    @Composable
    fun DrawerItem(title: String, selected: Boolean, onClick: () -> Unit) {
        val bg = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.12f)
        else androidx.compose.ui.graphics.Color.Transparent

        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(backgroundColor = bg)
        ) {
            Text(text = title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        }
    }

    ModalDrawer(
        drawerState = state.drawerState,
        drawerContent = {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = Locales.t("nav_menu"), fontWeight = FontWeight.Bold)
                Divider()

                DrawerItem(Locales.t("nav_main"), state.currentScreen == Screen.MONTH) {
                    state.currentScreen = Screen.MONTH
                    state.closeDrawer()
                }
                DrawerItem(Locales.t("nav_stats"), state.currentScreen == Screen.STATS) {
                    state.currentScreen = Screen.STATS
                    state.closeDrawer()
                }
                DrawerItem(Locales.t("nav_settings"), state.currentScreen == Screen.SETTINGS) {
                    state.currentScreen = Screen.SETTINGS
                    state.closeDrawer()
                }
                DrawerItem(Locales.t("nav_feedback"), state.currentScreen == Screen.FEEDBACK) {
                    state.currentScreen = Screen.FEEDBACK
                    state.closeDrawer()
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 2.dp,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        IconButton(
                            onClick = { state.openDrawer() },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colors.primary)
                        }

                        val titleText = when (state.currentScreen) {
                            Screen.MONTH -> Locales.t("nav_main")
                            Screen.SETTINGS -> Locales.t("nav_settings")
                            Screen.DAY_DETAILS -> Locales.t("nav_day")
                            Screen.STATS -> Locales.t("nav_stats")
                            Screen.FEEDBACK -> Locales.t("nav_feedback")
                        }

                        Text(
                            text = titleText,
                            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            fontSize = (18 * state.fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.currentScreen != Screen.MONTH) {
                                IconButton(onClick = { state.currentScreen = Screen.MONTH }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Reply,
                                        "Back",
                                        tint = MaterialTheme.colors.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(4.dp))

                            IconButton(onClick = {
                                state.currentScreen =
                                    if (state.currentScreen == Screen.SETTINGS) Screen.MONTH else Screen.SETTINGS
                            }) {
                                Icon(
                                    Icons.Default.Settings,
                                    "Settings",
                                    tint = if (state.currentScreen == Screen.SETTINGS)
                                        MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                    else MaterialTheme.colors.primary
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            content(padding)
        }
    }
}