package com.bearminds.purchases

import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformPurchaseModule: Module = module {

    single<PurchaseHelper> {
        AndroidPurchaseHelper()
    }

    single {
        PurchaseStateManager(
            purchaseHelper = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
            entitlementId = get(named(entitlementsKey))
        )
    }

    single<PaywallListener> {
        PaywallListenerImpl(get())
    }
}