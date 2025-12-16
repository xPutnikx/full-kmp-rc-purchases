@file:OptIn(ExperimentalTime::class)

package com.bearminds.purchases

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.Transaction
import com.revenuecat.purchases.kmp.models.freePhase
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.currentLocale
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
    private val product = delegate.storeProduct

    override val identifier: String = delegate.identifier
    override val packageType: String = delegate.packageType.name

    // Price information from storeProduct
    override val localizedPriceString: String = product.price.formatted
    override val currencyCode: String = product.price.currencyCode
    override val priceAmountMicros: Long = product.price.amountMicros

    // Calculate price per month manually for annual packages
    // Returns just the formatted price without suffix - UI layer adds localized suffix
    override val localizedPricePerMonth: String? = run {
        val period = product.period
        if (period != null && period.unit.name == "YEAR") {
            val monthlyMicros = priceAmountMicros / 12
            val monthlyAmount = monthlyMicros / 1_000_000.0
            // Format with currency symbol using NSNumberFormatter
            formatCurrency(monthlyAmount, currencyCode)
        } else {
            null
        }
    }

    // Free trial detection - use freeTrial from subscriptionOptions
    // This returns the free trial SubscriptionOption if available
    private val freeTrialOption = product.subscriptionOptions?.freeTrial
    private val freePhase = freeTrialOption?.freePhase

    override val hasFreeTrial: Boolean = freePhase != null

    override val freeTrialPeriod: String? = freePhase?.billingPeriod?.let {
        "${it.value} ${it.unit.name.lowercase()}"
    }

    override val freeTrialDays: Int? = freePhase?.billingPeriod?.let { period ->
        when (period.unit.name.uppercase()) {
            "DAY" -> period.value
            "WEEK" -> period.value * 7
            "MONTH" -> period.value * 30
            "YEAR" -> period.value * 365
            else -> null
        }
    }
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

/**
 * Format a currency amount using NSNumberFormatter
 */
private fun formatCurrency(amount: Double, currencyCode: String): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        this.currencyCode = currencyCode
        locale = NSLocale.currentLocale
    }
    return formatter.stringFromNumber(NSNumber(amount)) ?: "$amount"
}
