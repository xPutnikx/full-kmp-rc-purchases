# Purchases Module

A Kotlin Multiplatform abstraction layer for RevenueCat that enables seamless in-app purchase functionality across all supported platforms, including those where RevenueCat doesn't natively compile.

## Why This Module Exists

### The Problem

[RevenueCat's Kotlin Multiplatform SDK](https://www.revenuecat.com/docs/getting-started/installation/kotlin-multiplatform) only supports **Android** and **iOS** platforms. If you're building a Kotlin Multiplatform project that targets additional platforms like:

- **JVM** (Desktop applications)
- **JS** (Web applications)
- **WASM** (WebAssembly)
- **macOS**, **tvOS**, **watchOS** (Apple platforms beyond iOS)

...your project will **fail to compile** when you try to use RevenueCat directly in common code. The RevenueCat SDK simply doesn't provide implementations for these platforms, causing build errors.

### The Solution

This module provides a **platform-agnostic abstraction layer** that:

1. **Wraps RevenueCat** on supported platforms (Android/iOS) with full functionality
2. **Excludes RevenueCat dependencies** from unsupported platforms at build time
3. **Provides no-op implementations** for unsupported platforms, allowing your code to compile and run everywhere
4. **Hides RevenueCat types** from common code, keeping your business logic clean and platform-independent

## Why It's Good

### Compilation Safety

Your project compiles successfully for **all** target platforms, not just Android and iOS. The module automatically excludes RevenueCat from unsupported platforms using Gradle configuration:

```kotlin
// Exclude RevenueCat from Unsupported platforms configurations
afterEvaluate {
    configurations.matching {
        it.name.contains("jvm", ignoreCase = true) ||
        it.name.contains("macos", ignoreCase = true) ||
        it.name.contains("tvos", ignoreCase = true) ||
        it.name.contains("watchos", ignoreCase = true) ||
        it.name.contains("js", ignoreCase = true)
    }.configureEach {
        exclude(group = "com.revenuecat.purchases", module = "purchases-kmp-core")
        exclude(group = "com.revenuecat.purchases", module = "purchases-kmp-either")
        exclude(group = "com.revenuecat.purchases", module = "purchases-kmp-result")
    }
}
```

### Clean Abstraction

Common code never imports RevenueCat types directly. Instead, it uses platform-agnostic interfaces.

### Platform-Specific Implementations

- **Android/iOS**: Full RevenueCat integration with all features
- **JVM/JS/WASM**: No-op implementations that gracefully handle unsupported platforms
- **Other Apple platforms**: Stub implementations for compilation

### Dependency Injection Ready

The module integrates seamlessly with Koin via `expect/actual` pattern:

```kotlin
// commonMain
expect val platformPurchaseModule: Module

// androidMain/iosMain/jvmMain
actual val platformPurchaseModule: Module = module {
    single<PurchaseHelper> { PlatformPurchaseHelper() }
    single { PurchaseStateManager(get(), CoroutineScope(...)) }
    single<PaywallListener> { PaywallListenerImpl(get()) }
}
```

## Why You Should Use It

### Multi-Platform Projects

If your Kotlin Multiplatform project targets more than just Android and iOS, this module is **essential**. Without it, you'll face compilation errors on unsupported platforms.

### Clean Architecture

The abstraction layer keeps your business logic completely platform-independent. Your ViewModels, repositories, and use cases can work with purchases without knowing about RevenueCat.

### Future-Proof

If RevenueCat adds support for new platforms in the future, you only need to update the platform-specific implementation in this module. Your common code remains unchanged.

### Testing

No-op implementations on unsupported platforms make it easy to test your purchase logic without requiring actual platform-specific purchase infrastructure.

## Quick Start

### 1. Add Dependency

```kotlin
commonMain.dependencies {
    implementation(project(":purchases"))
}
```

### 2. Include in DI

```kotlin
val appModule = module {
    includes(platformPurchaseModule)
}
```

### 3. Use in Code

```kotlin
class YourViewModel(
    private val purchaseStateManager: PurchaseStateManager,
    private val purchaseHelper: PurchaseHelper
) {
    val isPro = purchaseStateManager.isPro

    init {
        viewModelScope.launch {
            purchaseStateManager.purchaseEvents.collect { event ->
                when (event) {
                    PurchaseEvent.PurchaseSuccess -> { /* Handle success */ }
                    is PurchaseEvent.Error -> { /* Handle error */ }
                    // ...
                }
            }
        }
    }
}
```

## API Overview

### Core Components

| Component | Description |
|-----------|-------------|
| `PurchaseHelper` | Main interface for purchase operations (initialize, getOfferings, purchase, restore) |
| `PurchaseStateManager` | Manages `isPro` state and emits `PurchaseEvent` flow |
| `PaywallListener` | Handles RevenueCat paywall callbacks |

### Purchase Events

```kotlin
sealed interface PurchaseEvent {
    data object PurchaseSuccess
    data object RestoreSuccess
    data object PurchaseCancelled
    data class Error(val error: PurchaseError)
    data class RestoreFailed(val error: PurchaseError)
}
```

### Abstract Types

`PurchaseCustomerInfo`, `PurchaseEntitlementInfo`, `PurchaseOfferings`, `PurchaseOffering`, `PurchasePackage`, `PurchaseError`, `PurchaseStoreTransaction`

## Platform Behavior

| Platform | Behavior |
|----------|----------|
| **Android/iOS** | Full RevenueCat functionality |
| **JVM/JS/WASM** | No-op implementations (compiles but returns errors/false) |

## RevenueCat Setup

This module requires RevenueCat to be properly configured for Android and iOS.
Refer to the [official RevenueCat KMP documentation](https://www.revenuecat.com/docs/getting-started/installation/kotlin-multiplatform) for:

1. **Account Setup**: Create a RevenueCat account and project
2. **Product Configuration**: Set up products and offerings in the RevenueCat dashboard
3. **Native SDK Integration**: Link the native iOS SDK (PurchasesHybridCommon) via Swift Package Manager or CocoaPods
4. **Android Configuration**: Ensure proper `launchMode` settings in AndroidManifest.xml

The module handles the Kotlin Multiplatform integration, but you still need to complete the platform-specific RevenueCat setup.

## Architecture

```
purchases/src/
├── commonMain/     # PurchaseHelper interface, PurchaseTypes, PurchaseStateManager
├── androidMain/    # Android RevenueCat implementation
├── iosMain/        # iOS RevenueCat implementation
└── jvmMain/        # JVM no-op implementation
```

## License

This module is **free to use, copy, or fork** by anyone. It's a helper library designed to work with RevenueCat's Kotlin Multiplatform SDK and provides no additional charges or fees beyond what RevenueCat itself may charge for their service.
