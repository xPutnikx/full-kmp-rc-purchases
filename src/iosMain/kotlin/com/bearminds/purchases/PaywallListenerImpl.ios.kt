package com.bearminds.purchases

import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener

class PaywallListenerImpl(
    private val purchaseStateManager: PurchaseStateManager
): PaywallListener {

    override fun onPurchaseCancelled() {
        purchaseStateManager.emitEvent(PurchaseEvent.PurchaseCancelled)
    }

    override fun onPurchaseCompleted(
        customerInfo: CustomerInfo,
        storeTransaction: StoreTransaction
    ) {
        val wrappedInfo = PurchaseCustomerInfoWrapper(customerInfo)
        purchaseStateManager.updateFromCustomerInfo(wrappedInfo)
        purchaseStateManager.emitEvent(PurchaseEvent.PurchaseSuccess)
    }

    override fun onPurchaseError(error: PurchasesError) {
        purchaseStateManager.emitEvent(
            PurchaseEvent.Error(PurchaseErrorWrapper(error))
        )
    }

    override fun onPurchaseStarted(rcPackage: Package) {
        // No action needed
    }

    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
        val wrappedInfo = PurchaseCustomerInfoWrapper(customerInfo)
        purchaseStateManager.updateFromCustomerInfo(wrappedInfo)
        purchaseStateManager.emitEvent(PurchaseEvent.RestoreSuccess)
    }

    override fun onRestoreError(error: PurchasesError) {
        purchaseStateManager.emitEvent(
            PurchaseEvent.RestoreFailed(PurchaseErrorWrapper(error))
        )
    }

    override fun onRestoreStarted() {
        // No action needed
    }
}
