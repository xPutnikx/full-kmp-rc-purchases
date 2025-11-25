package com.bearminds.purchases

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

val purchasesApiKey = "purchasesApiKey"
val entitlementsKey = "entitlementsKey"

/**
 * Helper interface for managing in-app purchases and subscriptions
 * Uses abstract purchase types that are resolved to platform-specific implementations
 */
interface PurchaseHelper {
    /**
     * Initialize the purchase SDK with the appropriate API key
     * @param apiKey The API key for the current platform
     */
    suspend fun initialize(apiKey: String)

    /**
     * Fetch available offerings
     * @param onSuccess Callback with the available offerings
     * @param onError Callback with the error if fetching fails
     */
    suspend fun getOfferings(
        onSuccess: (PurchaseOfferings) -> Unit,
        onError: (PurchaseError) -> Unit
    )

    /**
     * Purchase a package
     * @param packageToPurchase The package to purchase
     * @param onSuccess Callback with the transaction and updated customer info
     * @param onError Callback with the error and whether the user cancelled
     */
    suspend fun purchase(
        packageToPurchase: PurchasePackage,
        onSuccess: (PurchaseStoreTransaction, PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError, Boolean) -> Unit
    )

    /**
     * Restore previous purchases
     * @param onSuccess Callback with the restored customer info
     * @param onError Callback with the error
     */
    suspend fun restorePurchases(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    )

    /**
     * Get current customer info
     * @param onSuccess Callback with the current customer info
     * @param onError Callback with the error
     */
    suspend fun getCustomerInfo(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    )

    /**
     * Check if user has active entitlement for a specific identifier
     * @param entitlementIdentifier The entitlement identifier to check
     * @return True if the user has an active entitlement
     */
    suspend fun hasActiveEntitlement(entitlementIdentifier: String): Boolean

    /**
     * Set the preferred locale for paywalls and customer center UI
     * @param locale The locale code (e.g., "es-ES", "de-DE")
     *
     * This doesn't work on iOS platform just yet, because KMP doesn't support it.
     * On Android I integrate android lib directly, but on iOS I don't want to bring in SPM package
     */
    fun setPreferredLocale(locale: String)

    @Composable
    fun Paywall(dismissRequest: () -> Unit)

    @Composable
    fun CustomerCenter(modifier: Modifier, dismissRequest: () -> Unit)
}
