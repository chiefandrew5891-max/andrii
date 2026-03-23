import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
    androidTarget {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:1.6.11")
                implementation("org.jetbrains.compose.foundation:foundation:1.6.11")
                implementation("org.jetbrains.compose.material:material:1.6.11")
                implementation("org.jetbrains.compose.ui:ui:1.6.11")
                implementation("org.jetbrains.compose.components:components-resources:1.6.11")
                implementation("org.jetbrains.compose.components:components-ui-tooling-preview:1.6.11")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.6.11")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)

                // NotificationCompat lives in androidx.core
                implementation("androidx.core:core-ktx:1.13.1")

                // Preview tooling
                implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.6.11")
            }
        }
    }
}

android {
    namespace = "com.andrey.beautyplanner"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.andrey.beautyplanner"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation("org.jetbrains.compose.ui:ui-tooling:1.6.11")
}