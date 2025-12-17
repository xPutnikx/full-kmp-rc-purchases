package com.bearminds.purchases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * JVM implementation of NetworkConnectivity.
 * Uses a simple socket connection test to check internet connectivity.
 */
class JvmNetworkConnectivity : NetworkConnectivity {

    override suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                // Try to connect to a known reliable server (Google DNS)
                socket.connect(InetSocketAddress("8.8.8.8", 53), TIMEOUT_MS)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val TIMEOUT_MS = 1500
    }
}
