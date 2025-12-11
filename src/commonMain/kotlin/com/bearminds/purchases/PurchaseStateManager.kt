package com.bearminds.purchases

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for PurchaseStateManager to enable testability.
 *
 * ViewModels should depend on this interface, not the concrete PurchaseStateManager class,
 * allowing mocking in unit tests.
 */
interface PurchaseStateManager {
    /**
     * Flow indicating whether the user has an active Pro subscription.
     */
    val isPro: StateFlow<Boolean>

    /**
     * Flow of purchase-related events (purchases, restores, errors).
     */
    val purchaseEvents: SharedFlow<PurchaseEvent>

    /**
     * Refresh the purchase state from the RevenueCat backend.
     */
    suspend fun refreshPurchaseState()

    /**
     * Update purchase state from customer info (called by platform callbacks).
     */
    fun updateFromCustomerInfo(customerInfo: PurchaseCustomerInfo)

    /**
     * Emit a purchase event to all listeners.
     */
    fun emitEvent(event: PurchaseEvent)

    /**
     * Toggle pro status for development/testing purposes.
     * Only intended for use in debug builds on desktop.
     */
    fun toggleProStatusForDevelopment()
}
