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

    // Price information
    val localizedPriceString: String      // e.g., "$4.99" - full localized price
    val localizedPricePerMonth: String?   // e.g., "$2.49" - price per month for annual packages (no suffix)
    val currencyCode: String
    val priceAmountMicros: Long           // Price in micros for calculations

    // Free trial information
    val hasFreeTrial: Boolean
    val freeTrialPeriod: String?          // e.g., "7 days", "1 week"
    val freeTrialDays: Int?               // Number of days for free trial (null if no trial)

    // Introductory offer/discount information (paid intro phase, not free trial)
    val hasIntroductoryOffer: Boolean     // True if there's a discounted intro period
    val introductoryPrice: String?        // Formatted intro price, e.g., "174.50 kr"
    val introductoryPriceAmountMicros: Long?  // Intro price in micros for calculations
    val regularPrice: String?             // Formatted regular price after intro, e.g., "349.00 kr"
    val regularPriceAmountMicros: Long?   // Regular price in micros
    val introductoryPeriod: String?       // Intro period description, e.g., "1 year"
    val discountPercentage: Int?          // Calculated discount percentage, e.g., 50
}

interface PurchaseError {
    val message: String
    val code: Int
}

/**
 * RevenueCat error codes for purchase operations.
 * These codes help identify specific error conditions.
 */
object PurchaseErrorCode {
    const val UNKNOWN = 0
    const val PURCHASE_CANCELLED = 1
    const val STORE_PROBLEM = 2
    const val PURCHASE_NOT_ALLOWED = 3
    const val PRODUCT_NOT_AVAILABLE = 5
    const val NETWORK_ERROR = 10
    const val PAYMENT_PENDING = 20
    const val INVALID_RECEIPT = 21
    const val MISSING_RECEIPT = 22
    const val NOT_INITIALIZED = 99  // SDK not initialized (e.g., no Google Play Services)

    /**
     * Check if the error code indicates a user cancellation (not a real error)
     */
    fun isCancellation(code: Int): Boolean = code == PURCHASE_CANCELLED

    /**
     * Check if the error code indicates a pending payment (not a real error)
     */
    fun isPendingPayment(code: Int): Boolean = code == PAYMENT_PENDING

    /**
     * Check if the error code indicates a retriable network/store error
     */
    fun isRetriable(code: Int): Boolean =
        code == NETWORK_ERROR || code == STORE_PROBLEM
}

interface PurchaseStoreTransaction {
    val transactionIdentifier: String
    val productIdentifier: String
    val purchaseDate: Long
}

/**
 * Error returned when purchase operations are attempted before SDK is initialized.
 * This typically happens on devices without Google Play Services (Android) or
 * when initialization failed.
 */
class NotInitializedError : PurchaseError {
    override val message: String = "Purchase SDK not initialized. Store may not be available on this device."
    override val code: Int = PurchaseErrorCode.NOT_INITIALIZED
}
