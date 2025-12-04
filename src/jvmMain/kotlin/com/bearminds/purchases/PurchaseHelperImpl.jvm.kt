package com.bearminds.purchases

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * JVM implementation of PurchaseHelper that grants pro access by default.
 * Purchases are not supported on desktop JVM, but we auto-grant pro for development.
 */
class JVMPurchaseHelper : PurchaseHelper {

    private val proCustomerInfo = PurchaseCustomerInfoWrapper()

    override val cachedOfferings: PurchaseOfferings? = null

    override suspend fun initialize(apiKey: String) {
        // JVM platform - no initialization needed, pro access is auto-granted
    }

    override suspend fun getOfferings(
        onSuccess: (PurchaseOfferings) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        println("PurchaseHelper: JVM platform - getOfferings not supported (pro already granted)")
        onError(StubPurchaseError())
    }

    override suspend fun purchase(
        packageToPurchase: PurchasePackage,
        onSuccess: (PurchaseStoreTransaction, PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError, Boolean) -> Unit
    ) {
        println("PurchaseHelper: JVM platform - purchase not needed (pro already granted)")
        // Auto-complete purchase since pro is granted
        onSuccess(StubPurchaseStoreTransaction(), proCustomerInfo)
    }

    override suspend fun restorePurchases(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        println("PurchaseHelper: JVM platform - restore returning pro access")
        onSuccess(proCustomerInfo)
    }

    override suspend fun getCustomerInfo(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        println("PurchaseHelper: JVM platform - returning pro customer info")
        onSuccess(proCustomerInfo)
    }

    override suspend fun hasActiveEntitlement(entitlementIdentifier: String): Boolean {
        println("PurchaseHelper: JVM platform - hasActiveEntitlement returning true (pro granted)")
        return true
    }

    override fun setPreferredLocale(locale: String) {
        println("PurchaseHelper: JVM platform - setPreferredLocale not supported")
    }

    @Composable
    override fun Paywall(offeringIdentifier: String?, source: String, dismissRequest: () -> Unit) {
        println("PurchaseHelper: JVM platform - Paywall not supported (source: $source, offering: $offeringIdentifier)")
    }

    @Composable
    override fun CustomerCenter(modifier: Modifier, dismissRequest: () -> Unit) {
        println("PurchaseHelper: JVM platform - CustomerCenter not supported")
    }
}

