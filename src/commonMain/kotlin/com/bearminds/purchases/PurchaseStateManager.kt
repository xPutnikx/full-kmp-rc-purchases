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
    data object PurchaseSuccess : PurchaseEvent
    data object RestoreSuccess : PurchaseEvent
    data object PurchaseCancelled : PurchaseEvent
    data class Error(val error: PurchaseError) : PurchaseEvent
    data class RestoreFailed(val error: PurchaseError) : PurchaseEvent
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
}
