package com.andrey.beautyplanner.remote

import kotlinx.coroutines.CompletableDeferred

object BackendBridgeConnector {
    var callBackend: ((String, Map<String, String>, CompletableDeferred<Map<String, String>>) -> Unit)? = null
}