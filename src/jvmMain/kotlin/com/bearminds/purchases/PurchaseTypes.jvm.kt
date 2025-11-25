package com.bearminds.purchases

/**
 * Stub implementations for JVM platform
 * These provide no-op implementations that allow compilation without RevenueCat
 */

// JVM implementation that grants pro access by default for desktop development
class PurchaseCustomerInfoWrapper(
    grantProAccess: Boolean = true
) : PurchaseCustomerInfo {
    override val entitlements: Map<String, PurchaseEntitlementInfo> = if (grantProAccess) {
        // Return a map with any key mapping to an active entitlement
        // PurchaseStateManager will look up by entitlementId, so we use a default accessor
        ProEntitlementMap()
    } else {
        emptyMap()
    }
    override val activeSubscriptions: Set<String> = if (grantProAccess) setOf("desktop_pro") else emptySet()
    override val allPurchasedProductIdentifiers: Set<String> = if (grantProAccess) setOf("desktop_pro") else emptySet()
    override val nonSubscriptionTransactions: List<PurchaseStoreTransaction> = emptyList()
    override val latestExpirationDate: Long? = null
    override val requestDate: Long = System.currentTimeMillis()
    override val firstSeen: Long = System.currentTimeMillis()
    override val originalAppUserId: String = "desktop_user"
    override val managementURL: String? = null
    override val originalApplicationVersion: String? = null
    override val originalPurchaseDate: Long? = null
}

// Custom map that returns active entitlement for any key lookup
private class ProEntitlementMap : Map<String, PurchaseEntitlementInfo> {
    private val activeEntitlement = ProPurchaseEntitlementInfo()

    override val entries: Set<Map.Entry<String, PurchaseEntitlementInfo>> = emptySet()
    override val keys: Set<String> = emptySet()
    override val size: Int = 1
    override val values: Collection<PurchaseEntitlementInfo> = listOf(activeEntitlement)

    override fun isEmpty(): Boolean = false
    override fun get(key: String): PurchaseEntitlementInfo = activeEntitlement
    override fun containsValue(value: PurchaseEntitlementInfo): Boolean = value == activeEntitlement
    override fun containsKey(key: String): Boolean = true
}

class ProPurchaseEntitlementInfo : PurchaseEntitlementInfo {
    override val identifier: String = "desktop_pro"
    override val isActive: Boolean = true
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