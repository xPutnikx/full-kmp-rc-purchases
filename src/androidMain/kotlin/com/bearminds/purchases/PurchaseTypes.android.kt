@file:OptIn(ExperimentalTime::class)
package com.bearminds.purchases
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.Transaction
import com.revenuecat.purchases.kmp.models.freePhase
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

    // Introductory offer detection - check defaultOption for discounted intro phase
    // A discounted intro has 2+ pricing phases where the first is FINITE_RECURRING with non-zero price
    private val defaultOption = product.subscriptionOptions?.defaultOffer
    private val pricingPhases = defaultOption?.pricingPhases ?: emptyList()

    // Intro phase: first phase that is FINITE_RECURRING with a non-zero price (not free trial)
    private val introPhase = pricingPhases.firstOrNull { phase ->
        phase.recurrenceMode.name == "FINITE_RECURRING" && phase.price.amountMicros > 0
    }

    // Regular phase: the INFINITE_RECURRING phase (the ongoing price after intro)
    private val regularPhase = pricingPhases.firstOrNull { phase ->
        phase.recurrenceMode.name == "INFINITE_RECURRING"
    }

    override val hasIntroductoryOffer: Boolean = introPhase != null && regularPhase != null

    override val introductoryPrice: String? = introPhase?.price?.formatted

    override val introductoryPriceAmountMicros: Long? = introPhase?.price?.amountMicros

    override val regularPrice: String? = regularPhase?.price?.formatted

    override val regularPriceAmountMicros: Long? = regularPhase?.price?.amountMicros

    override val introductoryPeriod: String? = introPhase?.billingPeriod?.let {
        "${it.value} ${it.unit.name.lowercase()}"
    }

    override val discountPercentage: Int? = run {
        val introMicros = introPhase?.price?.amountMicros ?: return@run null
        val regularMicros = regularPhase?.price?.amountMicros ?: return@run null
        if (regularMicros <= 0) return@run null
        ((regularMicros - introMicros) * 100 / regularMicros).toInt()
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