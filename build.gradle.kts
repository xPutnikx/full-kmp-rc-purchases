@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)

    jvm()
    js().browser()
    wasmJs().browser()

    androidLibrary {
        namespace = "com.bearminds.purchases"
        compileSdk = 36
        minSdk = 24
    }

    sourceSets {
        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "purchasesKit"
    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64(),
        macosArm64(),
        macosX64(),
        watchosArm64(),
        watchosArm32(),
        tvosArm64(),
        tvosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                // Compose
                implementation(compose.runtime)

                // Koin
                implementation(libs.koin.core)

                // RevenueCat
                implementation(libs.revenuecat.core)
                implementation(libs.revenuecat.ui)
                implementation(libs.revenuecat.either)
                implementation(libs.revenuecat.result)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {}
        }

        iosMain {
            dependencies {}
        }
    }
}



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
        exclude(group = "com.revenuecat.purchases", module = "purchases-kmp-ui")
    }
}