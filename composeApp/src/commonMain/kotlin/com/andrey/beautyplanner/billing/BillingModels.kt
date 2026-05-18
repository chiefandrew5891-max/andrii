package com.andrey.beautyplanner.billing

data class BillingProduct(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long? = null,
    val priceCurrencyCode: String? = null
)

enum class BillingStatus {
    IDLE,
    CONNECTING,
    READY,
    LOADING_PRODUCTS,
    PURCHASING,
    RESTORING,
    PURCHASED,
    ERROR
}

sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

sealed class RestoreResult {
    data object Restored : RestoreResult()
    data object NothingToRestore : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

data class BillingUiState(
    val status: BillingStatus = BillingStatus.IDLE,
    val products: List<BillingProduct> = emptyList(),
    val errorMessage: String? = null,
    val ownedPremium: Boolean = false
)