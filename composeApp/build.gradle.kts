import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}



kotlin {
    jvmToolchain(21)
    // Android target configured via androidLibrary block (replaces androidTarget + android{})
    androidLibrary {
        namespace = "com.sanket.tools.passwordmanager.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        // Required for Compose Multiplatform resources to be bundled into the AAR
        androidResources {
            enable = true
        }
    }
    // ── iOS disabled — targets: Android + Desktop JVM only ──────────────
    // listOf(
    //     iosArm64(),
    //     iosSimulatorArm64()
    // ).forEach { iosTarget ->
    //     iosTarget.binaries.framework {
    //         baseName = "ComposeApp"
    //         isStatic = true
    //     }
    // }
    
    jvm()
    
    // js {
    //     browser()
    //     binaries.executable()
    // }
    
    // @OptIn(ExperimentalWasmDsl::class)
    // wasmJs {
    //     browser()
    //     binaries.executable()
    // }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.materialKolor)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(projects.shared)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            //will removie on release
            implementation("co.touchlab:kermit:2.1.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.sanket.tools.passwordmanager.MainKt"

        // Use the toolchain downloaded by Gradle instead of Android Studio's bundled JBR which lacks jpackage
        val javaToolchains = project.extensions.getByType<JavaToolchainService>()
        javaHome = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
            vendor.set(JvmVendorSpec.ADOPTIUM) // 👈 ADD THIS
        }.get().metadata.installationPath.asFile.absolutePath

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.sanket.tools.passwordmanager"
            packageVersion = "1.0.0"
        }
    }
}
