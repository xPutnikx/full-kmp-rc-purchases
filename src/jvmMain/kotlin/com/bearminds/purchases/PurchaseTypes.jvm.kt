package com.bearminds.purchases

/**
 * Stub implementations for JVM platform
 * These provide no-op implementations that allow compilation without RevenueCat
 */

// Stub classes that implement the expect interfaces
class PurchaseCustomerInfoWrapper : PurchaseCustomerInfo {
    override val entitlements: Map<String, PurchaseEntitlementInfo> = emptyMap()
    override val activeSubscriptions: Set<String> = emptySet()
    override val allPurchasedProductIdentifiers: Set<String> = emptySet()
    override val nonSubscriptionTransactions: List<PurchaseStoreTransaction> = emptyList()
    override val latestExpirationDate: Long? = null
    override val requestDate: Long = 0L
    override val firstSeen: Long = 0L
    override val originalAppUserId: String = ""
    override val managementURL: String? = null
    override val originalApplicationVersion: String? = null
    override val originalPurchaseDate: Long? = null
}

class StubPurchaseEntitlementInfo : PurchaseEntitlementInfo {
    override val identifier: String = ""
    override val isActive: Boolean = false
}

class StubPurchaseOfferings : PurchaseOfferings {
    override val current: PurchaseOffering? = null
    override val all: Map<String, PurchaseOffering> = emptyMap()
}

class StubPurchaseOffering : PurchaseOffering {
    override val identifier: String = ""
    override val availablePackages: List<PurchasePackage> = emptyList()
    override val monthly: PurchasePackage? = null
    override val annual: PurchasePackage? = null
}

class StubPurchasePackage : PurchasePackage {
    override val identifier: String = ""
    override val packageType: String = ""
}

class StubPurchaseError : PurchaseError {
    override val message: String = "Purchases not supported on JVM"
    override val code: Int = -1
}

class StubPurchaseStoreTransaction : PurchaseStoreTransaction {
    override val transactionIdentifier: String = ""
    override val productIdentifier: String = ""
    override val purchaseDate: Long = 0L
}