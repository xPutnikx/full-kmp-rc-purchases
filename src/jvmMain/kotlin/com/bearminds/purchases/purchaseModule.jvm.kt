package com.bearminds.purchases

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPurchaseModule: Module = module {
    single<PurchaseHelper> {
        JVMPurchaseHelper()
    }
}