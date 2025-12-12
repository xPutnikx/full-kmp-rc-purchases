@file:OptIn(ExperimentalTime::class)
package com.bearminds.purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.Transaction
import com.revenuecat.purchases.kmp.models.Package
import java.text.NumberFormat
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

class AndroidPurchaseOfferings(val delegate: Offerings) : PurchaseOfferings {
    override val current: PurchaseOffering? = delegate.current?.let { AndroidPurchaseOffering(it) }
    override val all: Map<String, PurchaseOffering> =
        delegate.all.mapValues { AndroidPurchaseOffering(it.value) }
}

class AndroidPurchaseOffering(val delegate: Offering) : PurchaseOffering {
    override val identifier: String = delegate.identifier
    override val availablePackages: List<PurchasePackage> =
        delegate.availablePackages.map { AndroidPurchasePackage(it) }
    override val monthly: PurchasePackage? = delegate.monthly?.let { AndroidPurchasePackage(it) }
    override val annual: PurchasePackage? = delegate.annual?.let { AndroidPurchasePackage(it) }
}

class AndroidPurchasePackage(val delegate: Package) : PurchasePackage {
    private val product = delegate.storeProduct

    override val identifier: String = delegate.identifier
    override val packageType: String = delegate.packageType.name

    // Price information from storeProduct
    override val localizedPriceString: String = product.price.formatted
    override val currencyCode: String = product.price.currencyCode
    override val priceAmountMicros: Long = product.price.amountMicros

    // Calculate price per month manually for annual packages
    // RevenueCat KMP SDK doesn't expose pricePerMonth() on Android
    // Returns just the formatted price without suffix - UI layer adds localized suffix
    override val localizedPricePerMonth: String? = run {
        val subscriptionPeriod = product.period
        if (subscriptionPeriod != null && subscriptionPeriod.unit.name == "YEAR") {
            val monthlyMicros = priceAmountMicros / 12
            val monthlyAmount = monthlyMicros / 1_000_000.0
            // Format with currency symbol only (no "/month" suffix)
            NumberFormat.getCurrencyInstance().apply {
                currency = java.util.Currency.getInstance(currencyCode)
            }.format(monthlyAmount)
        } else {
            null
        }
    }

    // Free trial detection - check subscription options for free trial
    // The API differs between Android and iOS in RevenueCat KMP SDK
    private val freePhase = run {
        val defaultOption = product.subscriptionOptions?.defaultOffer
        defaultOption?.pricingPhases?.firstOrNull { it.price.amountMicros == 0L }
    }

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

class AndroidPurchaseError(delegate: PurchasesError) : PurchaseError {
    override val message: String = delegate.message
    override val code: Int = delegate.code.code
}

class AndroidPurchaseStoreTransaction(delegate: Transaction) : PurchaseStoreTransaction {
    override val transactionIdentifier: String = delegate.transactionIdentifier
    override val productIdentifier: String = delegate.productIdentifier
    override val purchaseDate: Long = delegate.purchaseDateMillis
}
