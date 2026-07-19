package com.andrey.beautyplanner.auth

import kotlinx.coroutines.CompletableDeferred

object AnonymousAuthBridgeConnector {
    var signIn:
            ((CompletableDeferred<Map<String, String>>) -> Unit)? = null
}