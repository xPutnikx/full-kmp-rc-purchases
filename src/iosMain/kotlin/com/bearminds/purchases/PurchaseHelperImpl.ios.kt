package com.bearminds.purchases

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.models.Transaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import org.koin.mp.KoinPlatform.getKoin

class IOSPurchaseHelper : PurchaseHelper {

    private var isInitialized = false
    private var _cachedOfferings: PurchaseOfferings? = null

    override val cachedOfferings: PurchaseOfferings?
        get() = _cachedOfferings

    override suspend fun initialize(apiKey: String) {
        if (isInitialized) {
            return
        }

        try {
            Purchases.logLevel = LogLevel.ERROR
            Purchases.configure(
                configuration = PurchasesConfiguration(
                    apiKey = apiKey
                )
            )
            isInitialized = true

            // Prefetch offerings for faster paywall display
            prefetchOfferings()
        } catch (e: Exception) {
            throw e
        }
    }

    private fun prefetchOfferings() {
        Purchases.sharedInstance.getOfferings(
            onError = { /* Silently fail - will retry when needed */ },
            onSuccess = { offerings ->
                _cachedOfferings = PurchaseOfferingsWrapper(offerings)
            }
        )
    }

    override suspend fun getOfferings(
        onSuccess: (PurchaseOfferings) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        if (!isInitialized) {
            println("PurchaseHelper: Not initialized, cannot fetch offerings")
            return
        }

        Purchases.sharedInstance.getOfferings(
            onError = { error ->
                println("PurchaseHelper: Failed to fetch offerings: ${error.message}")
                onError(PurchaseErrorWrapper(error))
            },
            onSuccess = { offerings ->
                println("PurchaseHelper: Fetched offerings successfully")
                onSuccess(PurchaseOfferingsWrapper(offerings))
            }
        )
    }

    override suspend fun purchase(
        packageToPurchase: PurchasePackage,
        onSuccess: (PurchaseStoreTransaction, PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError, Boolean) -> Unit
    ) {
        if (!isInitialized) {
            println("PurchaseHelper: Not initialized, cannot make purchase")
            return
        }

        Purchases.sharedInstance.purchase(
            packageToPurchase = (packageToPurchase as PurchasePackageWrapper).delegate,
            onError = { error, userCancelled ->
                if (userCancelled) {
                    println("PurchaseHelper: Purchase cancelled by user")
                } else {
                    println("PurchaseHelper: Purchase failed: ${error.message}")
                }
                onError(PurchaseErrorWrapper(error), userCancelled)
            },
            onSuccess = { storeTransaction, customerInfo ->
                println("PurchaseHelper: Purchase successful")
                val transaction = Transaction(
                    transactionIdentifier = storeTransaction.transactionId ?: "",
                    productIdentifier = storeTransaction.productIds.first(),
                    purchaseDateMillis = storeTransaction.purchaseTime,
                )
                onSuccess(
                    PurchaseStoreTransactionWrapper(transaction),
                    PurchaseCustomerInfoWrapper(customerInfo)
                )
            }
        )
    }

    override suspend fun restorePurchases(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        if (!isInitialized) {
            println("PurchaseHelper: Not initialized, cannot restore purchases")
            return
        }

        Purchases.sharedInstance.restorePurchases(
            onError = { error ->
                println("PurchaseHelper: Failed to restore purchases: ${error.message}")
                onError(PurchaseErrorWrapper(error))
            },
            onSuccess = { customerInfo ->
                println("PurchaseHelper: Restored purchases successfully")
                onSuccess(PurchaseCustomerInfoWrapper(customerInfo))
            }
        )
    }

    override suspend fun getCustomerInfo(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    ) {
        if (!isInitialized) {
            println("PurchaseHelper: Not initialized, cannot get customer info")
            return
        }

        Purchases.sharedInstance.getCustomerInfo(
            onError = { error ->
                println("PurchaseHelper: Failed to get customer info: ${error.message}")
                onError(PurchaseErrorWrapper(error))
            },
            onSuccess = { customerInfo ->
                println("PurchaseHelper: Got customer info successfully")
                onSuccess(PurchaseCustomerInfoWrapper(customerInfo))
            }
        )
    }

    override suspend fun hasActiveEntitlement(entitlementIdentifier: String): Boolean {
        if (!isInitialized) {
            println("PurchaseHelper: Not initialized, cannot check entitlement")
            return false
        }

        var hasEntitlement = false

        Purchases.sharedInstance.getCustomerInfo(
            onError = { error ->
                println("PurchaseHelper: Failed to check entitlement: ${error.message}")
            },
            onSuccess = { customerInfo ->
                val wrapped = PurchaseCustomerInfoWrapper(customerInfo)
                hasEntitlement = wrapped.entitlements[entitlementIdentifier]?.isActive == true
                println("PurchaseHelper: Entitlement '$entitlementIdentifier' active: $hasEntitlement")
            }
        )

        return hasEntitlement
    }

    override fun setPreferredLocale(locale: String) {
        if (!isInitialized) {
            println("PurchaseHelper: Not initialized, cannot set preferred locale")
            return
        }
        // Note: The RevenueCat KMP SDK doesn't expose overridePreferredUILocale for iOS yet.
        // The native iOS SDK supports this feature, but it's not wrapped in the KMP SDK.
        // When the KMP SDK adds support, this can be updated.
        // For now, the paywall will use the device's system locale on iOS.
        println("PurchaseHelper: setPreferredLocale($locale) - Not yet supported in KMP SDK for iOS")
    }

    @Composable
    override fun Paywall(offeringIdentifier: String?, source: String, dismissRequest: () -> Unit) {
        val purchaseStateManager: PurchaseStateManager = getKoin().get()
        val paywallListener: PaywallListener = getKoin().get()

        // Try to get offering from cache first, then fetch if not available
        var offering: com.revenuecat.purchases.kmp.models.Offering? by remember {
            val cachedOffering = offeringIdentifier?.let {
                (_cachedOfferings as? PurchaseOfferingsWrapper)?.delegate?.all?.get(it)
            }
            mutableStateOf(cachedOffering)
        }

        // Fetch from network if not in cache
        LaunchedEffect(offeringIdentifier) {
            if (offeringIdentifier != null && offering == null) {
                Purchases.sharedInstance.getOfferings(
                    onError = { /* Use default offering on error */ },
                    onSuccess = { offerings ->
                        offering = offerings.all[offeringIdentifier]
                        // Update cache
                        _cachedOfferings = PurchaseOfferingsWrapper(offerings)
                    }
                )
            }
        }

        val options = remember(paywallListener, offering) {
            PaywallOptions(dismissRequest = dismissRequest) {
                this.offering = offering
                shouldDisplayDismissButton = true
                listener = paywallListener
            }
        }

        // Track paywall displayed and observe events to handle dismiss on success/restore
        LaunchedEffect(Unit) {
            purchaseStateManager.emitEvent(PurchaseEvent.PaywallDisplayed(source))
            purchaseStateManager.purchaseEvents.collect { event ->
                when (event) {
                    is PurchaseEvent.PurchaseSuccess,
                    PurchaseEvent.RestoreSuccess -> dismissRequest()

                    else -> { /* Handle in UI layer */
                    }
                }
            }
        }

        com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall(options)
    }

    @Composable
    override fun CustomerCenter(modifier: Modifier, dismissRequest: () -> Unit) {
        val purchaseStateManager: PurchaseStateManager = getKoin().get()

        // Log CustomerCenter open event
        LaunchedEffect(Unit) {
            purchaseStateManager.emitEvent(PurchaseEvent.CustomerCenter.Opened)
        }

        com.revenuecat.purchases.kmp.ui.revenuecatui.CustomerCenter(
            modifier = modifier,
            onDismiss = {
                purchaseStateManager.emitEvent(PurchaseEvent.CustomerCenter.Dismissed)
                dismissRequest()
            }
        )
    }
}

