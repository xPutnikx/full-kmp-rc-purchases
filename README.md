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

### ✅ Compilation Safety

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

### ✅ Clean Abstraction

Common code never imports RevenueCat types directly. Instead, it uses platform-agnostic interfaces.

### ✅ Platform-Specific Implementations

- **Android/iOS**: Full RevenueCat integration with all features
- **JVM/JS/WASM**: No-op implementations that gracefully handle unsupported platforms
- **Other Apple platforms**: Stub implementations for compilation

### ✅ Dependency Injection Ready

The module integrates seamlessly with Koin (or any DI framework) via `expect/actual` pattern:

```kotlin
// commonMain
expect val platformPurchaseModule: Module

// androidMain
actual val platformPurchaseModule: Module = module {
    single<PurchaseHelper> { AndroidPurchaseHelper() }
}

// iosMain
actual val platformPurchaseModule: Module = module {
    single<PurchaseHelper> { IOSPurchaseHelper() }
}

// jvmMain
actual val platformPurchaseModule: Module = module {
    single<PurchaseHelper> { JVMPurchaseHelper() }
}
```

## Why You Should Use It

### 🎯 Multi-Platform Projects

If your Kotlin Multiplatform project targets more than just Android and iOS, this module is **essential**. Without it, you'll face compilation errors on unsupported platforms.

### 🎯 Clean Architecture

The abstraction layer keeps your business logic completely platform-independent. Your ViewModels, repositories, and use cases can work with purchases without knowing about RevenueCat.

### 🎯 Future-Proof

If RevenueCat adds support for new platforms in the future, you only need to update the platform-specific implementation in this module. Your common code remains unchanged.

### 🎯 Testing

No-op implementations on unsupported platforms make it easy to test your purchase logic without requiring actual platform-specific purchase infrastructure.

## How to Use It

### 1. Add the Module as a Dependency

In your main app module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":purchases"))
        }
    }
}
```

### 2. Include the Platform Module in Your DI Setup

In your Koin module (or other DI framework):

```kotlin
val purchaseModule = module {
    includes(platformPurchaseModule)  // This provides the platform-specific implementation
}
```

### 3. Use PurchaseHelper in Your Code

Inject `PurchaseHelper` wherever you need purchase functionality:

```kotlin
class SubscriptionViewModel(
    private val purchaseHelper: PurchaseHelper
) : ViewModel() {
    
    fun loadOfferings() {
        viewModelScope.launch {
            purchaseHelper.getOfferings(
                onSuccess = { offerings ->
                    // Handle offerings
                    val currentOffering = offerings.current
                    // ...
                },
                onError = { error ->
                    // Handle error
                }
            )
        }
    }
    
    fun purchasePackage(packageToPurchase: PurchasePackage) {
        viewModelScope.launch {
            purchaseHelper.purchase(
                packageToPurchase = packageToPurchase,
                onSuccess = { transaction, customerInfo ->
                    // Handle successful purchase
                },
                onError = { error, userCancelled ->
                    // Handle error or cancellation
                }
            )
        }
    }
    
    suspend fun checkPremiumAccess(): Boolean {
        return purchaseHelper.hasActiveEntitlement("premium")
    }
}
```

### 4. Initialize on App Startup

Initialize the purchase helper with your RevenueCat API key (Android/iOS only):

```kotlin
// In your app initialization code
val purchaseHelper: PurchaseHelper = get() // from DI

lifecycleScope.launch {
    // Get platform-specific API key
    val apiKey = when {
        Platform.isAndroid() -> "your_android_api_key"
        Platform.isIOS() -> "your_ios_api_key"
        else -> "" // Not needed for unsupported platforms
    }
    
    if (apiKey.isNotEmpty()) {
        purchaseHelper.initialize(apiKey)
    }
}
```

## API Reference

### PurchaseHelper Interface

The main interface for all purchase operations:

```kotlin
interface PurchaseHelper {
    suspend fun initialize(apiKey: String)
    suspend fun getOfferings(
        onSuccess: (PurchaseOfferings) -> Unit,
        onError: (PurchaseError) -> Unit
    )
    suspend fun purchase(
        packageToPurchase: PurchasePackage,
        onSuccess: (PurchaseStoreTransaction, PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError, Boolean) -> Unit
    )
    suspend fun restorePurchases(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    )
    suspend fun getCustomerInfo(
        onSuccess: (PurchaseCustomerInfo) -> Unit,
        onError: (PurchaseError) -> Unit
    )
    suspend fun hasActiveEntitlement(entitlementIdentifier: String): Boolean
}
```

### Type System

All RevenueCat types are abstracted into platform-independent interfaces:

- `PurchaseCustomerInfo` - Customer subscription and purchase information
- `PurchaseEntitlementInfo` - Entitlement status and details
- `PurchaseOfferings` - Available subscription offerings
- `PurchaseOffering` - A specific offering with packages
- `PurchasePackage` - A purchasable package (monthly, annual, etc.)
- `PurchaseError` - Error information
- `PurchaseStoreTransaction` - Transaction details

## Platform Behavior

### Android & iOS

Full RevenueCat functionality:
- ✅ Real purchases and subscriptions
- ✅ Restore purchases
- ✅ Customer info retrieval
- ✅ Entitlement checking
- ✅ Error handling

### JVM, JS, WASM, and Other Platforms

No-op implementations:
- ⚠️ All methods complete without errors
- ⚠️ `hasActiveEntitlement()` always returns `false`
- ⚠️ Purchase operations call error callbacks with stub errors
- ⚠️ No actual purchase functionality

This allows your app to compile and run, but purchase features will be disabled on these platforms. You can handle this in your UI by checking the platform or the result of purchase operations.

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
purchases/
├── src/
│   ├── commonMain/
│   │   └── kotlin/com/bearminds/purchases/
│   │       ├── PurchaseHelper.kt          # Main interface
│   │       ├── PurchaseTypes.kt           # Abstract type interfaces
│   │       └── purchaseModule.kt          # expect val for DI
│   ├── androidMain/
│   │   └── kotlin/com/bearminds/purchases/
│   │       ├── PurchaseHelperImpl.android.kt  # Android RevenueCat implementation
│   │       ├── PurchaseTypes.android.kt       # Android type wrappers
│   │       └── purchaseModule.android.kt      # Android DI module
│   ├── iosMain/
│   │   └── kotlin/com/bearminds/purchases/
│   │       ├── PurchaseHelperImpl.ios.kt      # iOS RevenueCat implementation
│   │       ├── PurchaseTypes.ios.kt            # iOS type wrappers
│   │       └── purchaseModule.ios.kt           # iOS DI module
│   └── jvmMain/
│       └── kotlin/com/bearminds/purchases/
│           ├── PurchaseHelperImpl.jvm.kt       # JVM no-op implementation
│           ├── PurchaseTypes.jvm.kt            # JVM stub types
│           └── purchaseModule.jvm.kt          # JVM DI module
└── build.gradle.kts                            # Gradle config with RevenueCat exclusions
```

## Best Practices

1. **Always check platform support** before showing purchase UI:
   ```kotlin
   if (Platform.isAndroid() || Platform.isIOS()) {
       // Show purchase options
   } else {
       // Show alternative (e.g., "Premium features not available on desktop")
   }
   ```

2. **Handle errors gracefully** - Unsupported platforms will return errors, so always handle the error callbacks.

3. **Use dependency injection** - Always inject `PurchaseHelper` rather than instantiating it directly.

4. **Initialize early** - Call `initialize()` as early as possible in your app lifecycle.

5. **Check entitlements** - Use `hasActiveEntitlement()` to gate premium features rather than assuming purchase success.

## Contributing

When adding support for new platforms:

1. Create a new source set (e.g., `jsMain`, `wasmJsMain`)
2. Implement `PurchaseHelper` with appropriate behavior (no-op or actual implementation)
3. Implement stub types in `PurchaseTypes.{platform}.kt`
4. Add the platform to the exclusion list in `build.gradle.kts` if RevenueCat doesn't support it
5. Create the platform-specific DI module

## License

This module is **free to use, copy, or fork** by anyone. It's a helper library designed to work with RevenueCat's Kotlin Multiplatform SDK and provides no additional charges or fees beyond what RevenueCat itself may charge for their service. This module is independent of RevenueCat's business model and simply provides an abstraction layer to make RevenueCat easier to use in multi-platform Kotlin projects.
