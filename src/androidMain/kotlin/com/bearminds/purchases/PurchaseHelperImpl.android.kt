package com.bearminds.purchases

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.customercenter.CustomerCenterListener
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.Purchases as NativePurchases
import com.revenuecat.purchases.kmp.models.Transaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenterOptions
import org.koin.mp.KoinPlatform.getKoin

class AndroidPurchaseHelper : PurchaseHelper {

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
                configuration = PurchasesConfiguration(apiKey = apiKey)
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
                _cachedOfferings = AndroidPurchaseOfferings(offerings)
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
                onError(AndroidPurchaseError(error))
            },
            onSuccess = { offerings ->
                println("PurchaseHelper: Fetched offerings successfully")
                onSuccess(AndroidPurchaseOfferings(offerings))
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
            packageToPurchase = (packageToPurchase as AndroidPurchasePackage).delegate,
            onError = { error, userCancelled ->
                if (userCancelled) {
                    println("PurchaseHelper: Purchase cancelled by user")
                } else {
                    println("PurchaseHelper: Purchase failed: ${error.message}")
                }
                onError(AndroidPurchaseError(error), userCancelled)
            },
            onSuccess = { storeTransaction, customerInfo ->
                println("PurchaseHelper: Purchase successful; trying to convert to generic transaction")
                onSuccess(
                    AndroidPurchaseStoreTransaction(
                        delegate = Transaction(
                            transactionIdentifier = storeTransaction.transactionId ?: "",
                            productIdentifier = storeTransaction.productIds.first(),
                            purchaseDateMillis = storeTransaction.purchaseTime
                        )
                    ),
                    AndroidPurchaseCustomerInfo(customerInfo)
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
                onError(AndroidPurchaseError(error))
            },
            onSuccess = { customerInfo ->
                println("PurchaseHelper: Restored purchases successfully")
                onSuccess(AndroidPurchaseCustomerInfo(customerInfo))
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
                onError(AndroidPurchaseError(error))
            },
            onSuccess = { customerInfo ->
                println("PurchaseHelper: Got customer info successfully")
                onSuccess(AndroidPurchaseCustomerInfo(customerInfo))
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
                val wrapped = AndroidPurchaseCustomerInfo(customerInfo)
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
        NativePurchases.sharedInstance.overridePreferredUILocale(locale)
        println("PurchaseHelper: Set preferred locale to $locale")
    }

    @Composable
    override fun Paywall(offeringIdentifier: String?, source: String, dismissRequest: () -> Unit) {
        val purchaseStateManager: PurchaseStateManager = getKoin().get()
        val paywallListener: PaywallListener = getKoin().get()

        // Get offering from cache
        val offering: Offering? = remember(offeringIdentifier) {
            val cachedOffering = offeringIdentifier?.let {
                (_cachedOfferings as? AndroidPurchaseOfferings)?.delegate?.all?.get(it)
            }
            cachedOffering
        }

        val options = remember(paywallListener, offering) {
            PaywallOptions(dismissRequest = dismissRequest) {
                this.offering = offering
                shouldDisplayDismissButton = true
                listener = paywallListener
            }
        }

        // Track paywall displayed
        LaunchedEffect(Unit) {
            purchaseStateManager.emitEvent(PurchaseEvent.PaywallDisplayed(source))
            purchaseStateManager.purchaseEvents.collect { event ->
                when (event) {
                    is PurchaseEvent.PurchaseSuccess,
                    PurchaseEvent.RestoreSuccess -> dismissRequest()
                    else -> { /* Handle in UI layer */ }
                }
            }
        }

        Paywall(options)
    }

    @Composable
    override fun CustomerCenter(modifier: Modifier, dismissRequest: () -> Unit) {
        val purchaseStateManager: PurchaseStateManager = getKoin().get()
        val customerCenterListener: CustomerCenterListener = getKoin().get()

        LaunchedEffect(Unit) {
            purchaseStateManager.emitEvent(PurchaseEvent.CustomerCenter.Opened)
        }

        com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter(
            modifier = modifier,
            options = CustomerCenterOptions.Builder()
                .setListener(customerCenterListener)
                .build(),
            onDismiss = {
                purchaseStateManager.emitEvent(PurchaseEvent.CustomerCenter.Dismissed)
                dismissRequest()
            }
        )
    }
}

