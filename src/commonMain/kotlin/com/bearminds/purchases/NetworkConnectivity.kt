package com.bearminds.purchases

/**
 * Interface for checking network connectivity status.
 * Platform-specific implementations will check actual connectivity.
 */
interface NetworkConnectivity {
    /**
     * Returns true if the device has an active network connection.
     */
    suspend fun isConnected(): Boolean
}

/**
 * No-op implementation for testing and default parameter usage.
 * Always returns true (assumes connected).
 */
object NoOpNetworkConnectivity : NetworkConnectivity {
    override suspend fun isConnected(): Boolean = true
}
