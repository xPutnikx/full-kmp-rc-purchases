@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bearminds.purchases

import kotlin.time.Clock

/**
 * macOS stub implementations for purchase types.
 * These provide no-op implementations that allow compilation without RevenueCat.
 * macOS users get pro access by default.
 */

// macOS implementation that grants pro access by default for desktop development
class MacosPurchaseCustomerInfo(
    grantProAccess: Boolean = true
) : PurchaseCustomerInfo {
    override val entitlements: Map<String, PurchaseEntitlementInfo> = if (grantProAccess) {
        // Return a map with any key mapping to an active entitlement
        MacosProEntitlementMap()
    } else {
        emptyMap()
    }
    override val activeSubscriptions: Set<String> = if (grantProAccess) setOf("macos_pro") else emptySet()
    override val allPurchasedProductIdentifiers: Set<String> = if (grantProAccess) setOf("macos_pro") else emptySet()
    override val nonSubscriptionTransactions: List<PurchaseStoreTransaction> = emptyList()
    override val latestExpirationDate: Long? = null
    override val requestDate: Long = Clock.System.now().toEpochMilliseconds()
    override val firstSeen: Long = Clock.System.now().toEpochMilliseconds()
    override val originalAppUserId: String = "macos_user"
    override val managementURL: String? = null
    override val originalApplicationVersion: String? = null
    override val originalPurchaseDate: Long? = null
}

// Custom map that returns active entitlement for any key lookup
private class MacosProEntitlementMap : Map<String, PurchaseEntitlementInfo> {
    private val activeEntitlement = MacosProPurchaseEntitlementInfo()

    override val entries: Set<Map.Entry<String, PurchaseEntitlementInfo>> = emptySet()
    override val keys: Set<String> = emptySet()
    override val size: Int = 1
    override val values: Collection<PurchaseEntitlementInfo> = listOf(activeEntitlement)

    override fun isEmpty(): Boolean = false
    override fun get(key: String): PurchaseEntitlementInfo = activeEntitlement
    override fun containsValue(value: PurchaseEntitlementInfo): Boolean = value == activeEntitlement
    override fun containsKey(key: String): Boolean = true
}

class MacosProPurchaseEntitlementInfo : PurchaseEntitlementInfo {
    override val identifier: String = "macos_pro"
    override val isActive: Boolean = true
}

class MacosPurchaseError : PurchaseError {
    override val message: String = "Purchases not supported on macOS"
    override val code: Int = -1
}

class MacosPurchaseStoreTransaction : PurchaseStoreTransaction {
    override val transactionIdentifier: String = "macos_transaction"
    override val productIdentifier: String = "macos_pro"
    override val purchaseDate: Long = Clock.System.now().toEpochMilliseconds()
}
