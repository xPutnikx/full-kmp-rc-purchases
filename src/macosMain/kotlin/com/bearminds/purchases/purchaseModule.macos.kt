package com.bearminds.purchases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * macOS implementation of platformPurchaseModule.
 * RevenueCat doesn't support macOS, so we provide stub implementations
 * that auto-grant pro access for development purposes.
 */
actual val platformPurchaseModule: Module = module {

    single<NetworkConnectivity> {
        MacosNetworkConnectivity()
    }

    single<PurchaseHelper> {
        MacosPurchaseHelper()
    }

    single<PurchaseStateManager> {
        PurchaseStateManagerImpl(
            purchaseHelper = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
            entitlementId = "ignore"
        )
    }
}
