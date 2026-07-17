package com.andrey.beautyplanner

import kotlinx.coroutines.CompletableDeferred

object BackupCryptoBridgeConnector {
    var encrypt: ((
        plaintext: String,
        password: String,
        saltBase64: String,
        iterations: Int,
        deferred: CompletableDeferred<Map<String, String>>
    ) -> Unit)? = null

    var decrypt: ((
        ciphertextBase64: String,
        password: String,
        saltBase64: String,
        ivBase64: String,
        iterations: Int,
        deferred: CompletableDeferred<String>
    ) -> Unit)? = null
}