@file:OptIn(ExperimentalTime::class)

package com.bearminds.purchases

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.Transaction
import kotlin.time.ExperimentalTime

/**
 * Wrapper classes that implement expect interfaces and delegate to RevenueCat types
 * This allows us to hide RevenueCat imports from common code
 */

internal class PurchaseCustomerInfoWrapper(delegate: CustomerInfo) :
    PurchaseCustomerInfo {
    override val entitlements: Map<String, PurchaseEntitlementInfo> =
        delegate.entitlements.all.mapValues { PurchaseEntitlementInfoWrapper(it.value) }
    override val activeSubscriptions: Set<String> = delegate.activeSubscriptions
    override val allPurchasedProductIdentifiers: Set<String> =
        delegate.allPurchasedProductIdentifiers
    override val nonSubscriptionTransactions: List<PurchaseStoreTransaction> =
        delegate.nonSubscriptionTransactions.map { PurchaseStoreTransactionWrapper(it) }
    override val latestExpirationDate: Long? = delegate.latestExpirationDate?.toEpochMilliseconds()
    override val requestDate: Long = delegate.requestDate.toEpochMilliseconds()
    override val firstSeen: Long = delegate.firstSeen.toEpochMilliseconds()
    override val originalAppUserId: String = delegate.originalAppUserId
    override val managementURL: String? = delegate.managementUrlString
    override val originalApplicationVersion: String? = delegate.originalApplicationVersion
    override val originalPurchaseDate: Long? = delegate.originalPurchaseDate?.toEpochMilliseconds()
}

internal class PurchaseEntitlementInfoWrapper(delegate: EntitlementInfo) :
    PurchaseEntitlementInfo {
    override val identifier: String = delegate.identifier
    override val isActive: Boolean = delegate.isActive
}

internal class PurchaseOfferingsWrapper(val delegate: Offerings) : PurchaseOfferings {
    override val current: PurchaseOffering? = delegate.current?.let { PurchaseOfferingWrapper(it) }
    override val all: Map<String, PurchaseOffering> =
        delegate.all.mapValues { PurchaseOfferingWrapper(it.value) }
}

internal class PurchaseOfferingWrapper(val delegate: Offering) : PurchaseOffering {
    override val identifier: String = delegate.identifier
    override val availablePackages: List<PurchasePackage> =
        delegate.availablePackages.map { PurchasePackageWrapper(it) }
    override val monthly: PurchasePackage? = delegate.monthly?.let { PurchasePackageWrapper(it) }
    override val annual: PurchasePackage? = delegate.annual?.let { PurchasePackageWrapper(it) }
}

internal class PurchasePackageWrapper(val delegate: Package) : PurchasePackage {
    override val identifier: String = delegate.identifier
    override val packageType: String = delegate.packageType.name
}

internal class PurchaseErrorWrapper(delegate: PurchasesError) : PurchaseError {
    override val message: String = delegate.message
    override val code: Int = delegate.code.code
}

internal class PurchaseStoreTransactionWrapper(delegate: Transaction) :
    PurchaseStoreTransaction {
    override val transactionIdentifier: String = delegate.transactionIdentifier
    override val productIdentifier: String = delegate.productIdentifier
    override val purchaseDate: Long = delegate.purchaseDate.toEpochMilliseconds()
}
