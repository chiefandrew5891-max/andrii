package com.andrey.beautyplanner.appcontent.approot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andrey.beautyplanner.Locales
import com.andrey.beautyplanner.Screen
import com.andrey.beautyplanner.auth.SignInProvider

@Composable
fun AppRootChrome(
    state: AppRootState,
    content: @Composable (PaddingValues) -> Unit
) {
    val onBg = MaterialTheme.colors.onBackground
    val bg = MaterialTheme.colors.background
    val onSurface = MaterialTheme.colors.onSurface

    @Composable
    fun DrawerItem(title: String, selected: Boolean, onClick: () -> Unit) {
        val itemBg =
            if (selected) MaterialTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = itemBg,
                contentColor = onSurface
            )
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = onSurface
            )
        }
    }

    @Composable
    fun DrawerActionItem(title: String, onClick: () -> Unit) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color.Transparent,
                contentColor = onSurface
            )
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = onSurface
            )
        }
    }

    @Composable
    fun DrawerInfoItem(title: String) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = onSurface.copy(alpha = 0.82f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }

    @Composable
    fun DrawerSectionTitle(title: String) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 2.dp, start = 12.dp, end = 12.dp),
            color = onSurface.copy(alpha = 0.72f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    Surface(
        color = bg,
        contentColor = onBg
    ) {
        ModalDrawer(
            drawerState = state.drawerState,
            drawerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = Locales.t("nav_menu"),
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )

                    Divider()

                    DrawerSectionTitle(Locales.t("account_current"))

                    val authUser = state.currentAuthUser
                    val isSignedInUser =
                        authUser != null && authUser.provider != SignInProvider.ANONYMOUS

                    if (!isSignedInUser) {
                        DrawerInfoItem(
                            title = Locales.t("account_anonymous")
                        )

                        DrawerActionItem(
                            title = Locales.t("account_sign_in")
                        ) {
                            state.closeDrawer()
                            state.openSignInScreen()
                        }
                    } else {
                        val accountLabel = when {
                            authUser?.email?.isNotBlank() == true -> authUser.email
                            authUser?.displayName?.isNotBlank() == true -> authUser.displayName
                            else -> when (authUser?.provider) {
                                SignInProvider.GOOGLE -> "Google"
                                SignInProvider.EMAIL -> "Email"
                                SignInProvider.APPLE -> "Apple"
                                else -> Locales.t("account_current")
                            }
                        }

                        DrawerInfoItem(
                            title = accountLabel
                        )

                        DrawerActionItem(
                            title = Locales.t("account_switch")
                        ) {
                            state.closeDrawer()
                            state.switchAccount()
                        }

                        DrawerActionItem(
                            title = Locales.t("account_sign_out")
                        ) {
                            state.closeDrawer()
                            state.signOutCompletely()
                        }
                    }

                    Divider()

                    DrawerItem(
                        title = Locales.t("nav_main"),
                        selected = state.currentScreen == Screen.MONTH
                    ) {
                        state.navigateHome()
                        state.closeDrawer()
                    }

                    DrawerItem(
                        title = Locales.t("nav_stats"),
                        selected = state.currentScreen == Screen.STATS
                    ) {
                        state.screenHistory = emptyList()
                        state.currentScreen = Screen.STATS
                        state.closeDrawer()
                    }

                    DrawerItem(
                        title = Locales.t("nav_settings"),
                        selected = state.currentScreen == Screen.SETTINGS
                    ) {
                        state.screenHistory = emptyList()
                        state.currentScreen = Screen.SETTINGS
                        state.closeDrawer()
                    }

                    DrawerItem(
                        title = Locales.t("nav_feedback"),
                        selected = state.currentScreen == Screen.FEEDBACK
                    ) {
                        state.screenHistory = emptyList()
                        state.currentScreen = Screen.FEEDBACK
                        state.closeDrawer()
                    }
                }
            }
        ) {
            val isHomeScreen = state.currentScreen == Screen.MONTH
            val isNestedScreen =
                state.currentScreen == Screen.DAY_DETAILS ||
                        state.currentScreen == Screen.SERVICE_TEMPLATES ||
                        state.currentScreen == Screen.WORK_SCHEDULE ||
                        state.currentScreen == Screen.APPEARANCE_SETTINGS ||
                        state.currentScreen == Screen.DEVELOPER_ACCESS ||
                        state.currentScreen == Screen.BACKUP_SETTINGS ||
                        state.currentScreen == Screen.PRIVACY_POLICY ||
                        state.currentScreen == Screen.NOTIFICATION_SETTINGS ||
                        state.currentScreen == Screen.PREMIUM_ACCESS ||
                        state.currentScreen == Screen.AUTH_WELCOME

            val showBackButton = !isHomeScreen

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
                                onClick = {
                                    if (showBackButton) {
                                        state.navigateBack()
                                    } else {
                                        state.openDrawer()
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = if (showBackButton) {
                                        Icons.AutoMirrored.Filled.Reply
                                    } else {
                                        Icons.Default.Menu
                                    },
                                    contentDescription = if (showBackButton) {
                                        Locales.t("cd_back")
                                    } else {
                                        Locales.t("cd_menu")
                                    },
                                    tint = MaterialTheme.colors.primary
                                )
                            }

                            Row(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isNestedScreen) {
                                    IconButton(
                                        onClick = { state.navigateHome() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = Locales.t("nav_main"),
                                            tint = MaterialTheme.colors.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(4.dp))
                                }

                                IconButton(
                                    onClick = {
                                        if (state.currentScreen == Screen.SETTINGS) {
                                            state.navigateHome()
                                        } else {
                                            state.screenHistory = emptyList()
                                            state.currentScreen = Screen.SETTINGS
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = Locales.t("cd_settings"),
                                        tint = if (state.currentScreen == Screen.SETTINGS) {
                                            MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colors.primary
                                        }
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
}