package com.bearminds.purchases

import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.customercenter.CustomerCenterListener
import com.revenuecat.purchases.customercenter.CustomerCenterManagementOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CustomerCenterListenerImpl(
    private val scope: CoroutineScope,
    private val purchaseStateManager: PurchaseStateManager
) : CustomerCenterListener {
    override fun onManagementOptionSelected(action: CustomerCenterManagementOption) {
        val option = when (action) {
            CustomerCenterManagementOption.Cancel -> ManagementOption.Cancel
            is CustomerCenterManagementOption.CustomUrl -> ManagementOption.CustomUrl(action.uri.toString())
            CustomerCenterManagementOption.MissingPurchase -> ManagementOption.MissingPurchase
            is CustomerCenterManagementOption.CustomAction -> ManagementOption.CustomAction(
                action.actionIdentifier,
                action.purchaseIdentifier
            )

            else -> ManagementOption.Unknown
        }
        purchaseStateManager.emitEvent(
            PurchaseEvent.CustomerCenter.ManagementOptionSelected(option)
        )
    }

    override fun onRestoreStarted() {
        println("CustomerCenter: Restore started")
        purchaseStateManager.emitEvent(PurchaseEvent.CustomerCenter.RestoreStarted)
    }

    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
        scope.launch {
            purchaseStateManager.refreshPurchaseState()
        }
        purchaseStateManager.emitEvent(PurchaseEvent.CustomerCenter.RestoreCompleted)
    }

    override fun onRestoreFailed(error: PurchasesError) {
        println("CustomerCenter: Restore failed - ${error.message}")
        purchaseStateManager.emitEvent(
            PurchaseEvent.CustomerCenter.RestoreFailed(error.message)
        )
    }

    override fun onShowingManageSubscriptions() {
        println("CustomerCenter: Showing manage subscriptions")
        purchaseStateManager.emitEvent(PurchaseEvent.CustomerCenter.ManageSubscriptionsShown)
    }

    override fun onFeedbackSurveyCompleted(feedbackSurveyOptionId: String) {
        println("CustomerCenter: Feedback survey completed")
        purchaseStateManager.emitEvent(
            PurchaseEvent.CustomerCenter.FeedbackSurveyCompleted(feedbackSurveyOptionId)
        )
    }

    override fun onCustomActionSelected(actionIdentifier: String, purchaseIdentifier: String?) {
        purchaseStateManager.emitEvent(
            PurchaseEvent.CustomerCenter.ManagementOptionSelected(
                ManagementOption.CustomAction(actionIdentifier, purchaseIdentifier)
            )
        )
    }
}
