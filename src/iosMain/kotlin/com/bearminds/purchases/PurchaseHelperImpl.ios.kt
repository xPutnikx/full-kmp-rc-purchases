package com.bearminds.purchases

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.models.Transaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import org.koin.mp.KoinPlatform.getKoin

class IOSPurchaseHelper : PurchaseHelper {

    private var isInitialized = false

    override suspend fun initialize(apiKey: String) {
        if (isInitialized) {
            println("PurchaseHelper: Already initialized")
            return
        }

        try {
            Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(
                configuration = PurchasesConfiguration(
                    apiKey = apiKey
                )
            )
            isInitialized = true
            println("PurchaseHelper: Initialized successfully with API key")
        } catch (e: Exception) {
            println("PurchaseHelper: Initialization failed: ${e.message}")
            throw e
        }
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

    @Composable
    override fun Paywall(dismissRequest: () -> Unit) {
        val purchaseStateManager: PurchaseStateManager = getKoin().get()
        val paywallListener: PaywallListener = getKoin().get()

        val options = remember(paywallListener) {
            PaywallOptions(dismissRequest = dismissRequest) {
                shouldDisplayDismissButton = true
                listener = paywallListener
            }
        }

        // Observe events to handle dismiss on success/restore
        LaunchedEffect(Unit) {
            purchaseStateManager.purchaseEvents.collect { event ->
                when (event) {
                    PurchaseEvent.PurchaseSuccess,
                    PurchaseEvent.RestoreSuccess -> dismissRequest()
                    else -> { /* Handle in UI layer */ }
                }
            }
        }

        com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall(options)
    }
}

