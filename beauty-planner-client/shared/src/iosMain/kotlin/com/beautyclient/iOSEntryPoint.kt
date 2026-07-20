package com.beautyclient

import androidx.compose.ui.window.ComposeUIViewController
import com.beautyclient.ui.BeautyClientApp

/**
 * iOS entry point — called from ContentView.swift via the generated KMP framework.
 */
fun BeautyClientAppViewController() = ComposeUIViewController { BeautyClientApp() }
