package com.bearminds.purchases

/**
 * No-op implementation of PurchaseHelper for JVM platform
 * Purchases are not supported on desktop JVM
 */
class JVMPurchaseHelper : PurchaseHelper {

    override suspend fun initialize(apiKey: String) {
        println("PurchaseHelper: JVM platform - purchases not supported")
    }

    override suspend fun getOfferings(
        onSuccess: (PurchaseOfferings) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        println("PurchaseHelper: JVM platform - getOfferings not supported")
        onError(StubPurchaseError())
    }

    override suspend fun purchase(
        packageToPurchase: PurchasePackage,
        onSuccess: (PurchaseStoreTransaction, PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError, Boolean) -> Unit
    ) {
        println("PurchaseHelper: JVM platform - purchase not supported")
        onError(StubPurchaseError(), false)
    }

    override suspend fun restorePurchases(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        println("PurchaseHelper: JVM platform - restorePurchases not supported")
        onError(StubPurchaseError())
    }

    override suspend fun getCustomerInfo(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        println("PurchaseHelper: JVM platform - getCustomerInfo not supported")
        onError(StubPurchaseError())
    }

    override suspend fun hasActiveEntitlement(entitlementIdentifier: String): Boolean {
        println("PurchaseHelper: JVM platform - hasActiveEntitlement not supported")
        return false
    }
}

