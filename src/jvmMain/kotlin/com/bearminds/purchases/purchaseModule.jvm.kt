package com.bearminds.purchases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPurchaseModule: Module = module {
    single<PurchaseHelper> {
        JVMPurchaseHelper()
    }

    single {
        PurchaseStateManager(
            purchaseHelper = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
            entitlementId = "ignore"
        )
    }
}