package com.andrey.beautyplanner.billing

import kotlinx.coroutines.CompletableDeferred

object StoreKitBridgeConnector {
    var loadProducts:
            ((List<String>, CompletableDeferred<List<Map<String, String>>>) -> Unit)? = null

    var purchaseProduct:
            ((String, String, CompletableDeferred<Map<String, String>>) -> Unit)? = null

    var restorePurchases:
            ((CompletableDeferred<Map<String, String>>) -> Unit)? = null

    var currentSubscriptionInfo:
            ((CompletableDeferred<Map<String, String>>) -> Unit)? = null
}