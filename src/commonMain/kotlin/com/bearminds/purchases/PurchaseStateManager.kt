package com.bearminds.purchases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PurchaseEvent {
    data class PurchaseStarted(val packageIdentifier: String) : PurchaseEvent
    data class PurchaseSuccess(val productIdentifier: String) : PurchaseEvent
    data object RestoreStarted : PurchaseEvent
    data object RestoreSuccess : PurchaseEvent
    data object PurchaseCancelled : PurchaseEvent
    data class Error(val error: PurchaseError) : PurchaseEvent
    data class RestoreFailed(val error: PurchaseError) : PurchaseEvent
    data class PaywallDisplayed(val source: String) : PurchaseEvent

    // Customer Center events
    sealed interface CustomerCenter : PurchaseEvent {
        data object Opened : CustomerCenter
        data object Dismissed : CustomerCenter
        data object RestoreStarted : CustomerCenter
        data object RestoreCompleted : CustomerCenter
        data class RestoreFailed(val errorMessage: String) : CustomerCenter
        data object ManageSubscriptionsShown : CustomerCenter
        data class FeedbackSurveyCompleted(val optionId: String) : CustomerCenter
        data class ManagementOptionSelected(val option: ManagementOption) : CustomerCenter
    }
}

sealed interface ManagementOption {
    object Unknown: ManagementOption
    object MissingPurchase: ManagementOption
    object Cancel: ManagementOption
    data class CustomUrl(val url: String): ManagementOption
    data class CustomAction(
        val actionIdentifier: String,
        val purchaseIdentifier: String?,
    ): ManagementOption
}

class PurchaseStateManager(
    private val purchaseHelper: PurchaseHelper,
    private val scope: CoroutineScope,
    private val entitlementId: String
) {
    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>()
    val purchaseEvents: SharedFlow<PurchaseEvent> = _purchaseEvents.asSharedFlow()

    suspend fun refreshPurchaseState() {
        purchaseHelper.getCustomerInfo(
            onSuccess = { customerInfo ->
                updateFromCustomerInfo(customerInfo)
            },
            onError = { error ->
                println("PurchaseStateManager: Failed to refresh state: ${error.message}")
            }
        )
    }

    fun updateFromCustomerInfo(customerInfo: PurchaseCustomerInfo) {
        val isActive = customerInfo.entitlements[entitlementId]?.isActive == true
        _isPro.value = isActive
        println("PurchaseStateManager: Updated isPro to $isActive")
    }

    fun emitEvent(event: PurchaseEvent) {
        scope.launch {
            _purchaseEvents.emit(event)
        }
    }

    /**
     * Toggle pro status for development/testing purposes.
     * This is only intended for use in debug builds on desktop.
     */
    fun toggleProStatusForDevelopment() {
        _isPro.value = !_isPro.value
        println("PurchaseStateManager: DEV - Toggled isPro to ${_isPro.value}")
    }
}
