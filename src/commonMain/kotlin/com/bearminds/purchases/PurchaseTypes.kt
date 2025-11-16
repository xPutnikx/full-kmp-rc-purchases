package com.bearminds.purchases

/**
 * Abstract types for purchase functionality
 * These are expect interfaces that will be implemented by platform-specific code
 * 
 * Common code should use these types instead of importing RevenueCat directly
 */

interface PurchaseCustomerInfo {
    val entitlements: Map<String, PurchaseEntitlementInfo>
    val activeSubscriptions: Set<String>
    val allPurchasedProductIdentifiers: Set<String>
    val nonSubscriptionTransactions: List<PurchaseStoreTransaction>
    val latestExpirationDate: Long?
    val requestDate: Long
    val firstSeen: Long
    val originalAppUserId: String
    val managementURL: String?
    val originalApplicationVersion: String?
    val originalPurchaseDate: Long?
}

interface PurchaseEntitlementInfo {
    val identifier: String
    val isActive: Boolean
}

interface PurchaseOfferings {
    val current: PurchaseOffering?
    val all: Map<String, PurchaseOffering>
}

interface PurchaseOffering {
    val identifier: String
    val availablePackages: List<PurchasePackage>
    val monthly: PurchasePackage?
    val annual: PurchasePackage?
}

interface PurchasePackage {
    val identifier: String
    val packageType: String
}

interface PurchaseError {
    val message: String
    val code: Int
}

interface PurchaseStoreTransaction {
    val transactionIdentifier: String
    val productIdentifier: String
    val purchaseDate: Long
}
