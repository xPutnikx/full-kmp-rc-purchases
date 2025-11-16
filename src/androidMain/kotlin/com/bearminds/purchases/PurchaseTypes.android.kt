@file:OptIn(ExperimentalTime::class)
package com.bearminds.purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.Transaction
import com.revenuecat.purchases.kmp.models.Package
import kotlin.time.ExperimentalTime

/**
 * Wrapper classes that implement expect interfaces and delegate to RevenueCat types
 * This allows us to hide RevenueCat imports from common code
 */

class AndroidPurchaseCustomerInfo(delegate: CustomerInfo) : PurchaseCustomerInfo {
    override val entitlements: Map<String, PurchaseEntitlementInfo> =
        delegate.entitlements.all.mapValues { AndroidPurchaseEntitlementInfo(it.value) }
    override val activeSubscriptions: Set<String> = delegate.activeSubscriptions
    override val allPurchasedProductIdentifiers: Set<String> =
        delegate.allPurchasedProductIdentifiers
    override val nonSubscriptionTransactions: List<PurchaseStoreTransaction> =
        delegate.nonSubscriptionTransactions.map { AndroidPurchaseStoreTransaction(it) }
    override val latestExpirationDate: Long? = delegate.latestExpirationDate?.toEpochMilliseconds()
    override val requestDate: Long = delegate.requestDate.toEpochMilliseconds()
    override val firstSeen: Long = delegate.firstSeen.toEpochMilliseconds()
    override val originalAppUserId: String = delegate.originalAppUserId
    override val managementURL: String? = delegate.managementUrlString
    override val originalApplicationVersion: String? = delegate.originalApplicationVersion
    override val originalPurchaseDate: Long? = delegate.originalPurchaseDate?.toEpochMilliseconds()
}

class AndroidPurchaseEntitlementInfo(delegate: EntitlementInfo) :
    PurchaseEntitlementInfo {
    override val identifier: String = delegate.identifier
    override val isActive: Boolean = delegate.isActive
}

class AndroidPurchaseOfferings(delegate: Offerings) : PurchaseOfferings {
    override val current: PurchaseOffering? = delegate.current?.let { AndroidPurchaseOffering(it) }
    override val all: Map<String, PurchaseOffering> =
        delegate.all.mapValues { AndroidPurchaseOffering(it.value) }
}

class AndroidPurchaseOffering(delegate: Offering) : PurchaseOffering {
    override val identifier: String = delegate.identifier
    override val availablePackages: List<PurchasePackage> =
        delegate.availablePackages.map { AndroidPurchasePackage(it) }
    override val monthly: PurchasePackage? = delegate.monthly?.let { AndroidPurchasePackage(it) }
    override val annual: PurchasePackage? = delegate.annual?.let { AndroidPurchasePackage(it) }
}

class AndroidPurchasePackage(val delegate: Package) : PurchasePackage {
    override val identifier: String = delegate.identifier
    override val packageType: String = delegate.packageType.name
}

class AndroidPurchaseError(delegate: PurchasesError) : PurchaseError {
    override val message: String = delegate.message
    override val code: Int = delegate.code.code
}

class AndroidPurchaseStoreTransaction(delegate: Transaction) : PurchaseStoreTransaction {
    override val transactionIdentifier: String = delegate.transactionIdentifier
    override val productIdentifier: String = delegate.productIdentifier
    override val purchaseDate: Long = delegate.purchaseDateMillis
}
