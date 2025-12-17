package com.bearminds.purchases

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_get_global_queue
import kotlin.coroutines.resume

/**
 * iOS implementation of NetworkConnectivity using NWPathMonitor.
 */
class IosNetworkConnectivity : NetworkConnectivity {

    override suspend fun isConnected(): Boolean = suspendCancellableCoroutine { continuation ->
        val monitor = nw_path_monitor_create()
        val queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)

        nw_path_monitor_set_queue(monitor, queue)

        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            val isConnected = status == nw_path_status_satisfied

            // Cancel the monitor after getting the first result
            nw_path_monitor_cancel(monitor)

            if (continuation.isActive) {
                continuation.resume(isConnected)
            }
        }

        nw_path_monitor_start(monitor)

        continuation.invokeOnCancellation {
            nw_path_monitor_cancel(monitor)
        }
    }
}
