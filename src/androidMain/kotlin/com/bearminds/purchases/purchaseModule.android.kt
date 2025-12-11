package com.bearminds.purchases

import com.revenuecat.purchases.customercenter.CustomerCenterListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformPurchaseModule: Module = module {

    single {
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    single<PurchaseHelper> {
        AndroidPurchaseHelper()
    }

    single<PurchaseStateManager> {
        PurchaseStateManagerImpl(
            purchaseHelper = get(),
            scope = get(),
            entitlementId = get(named(entitlementsKey))
        )
    }

    single<PaywallListener> {
        PaywallListenerImpl(get())
    }

    single<CustomerCenterListener> {
        CustomerCenterListenerImpl(
            scope = get(),
            purchaseStateManager = get()
        )
    }
}