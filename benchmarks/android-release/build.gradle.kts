plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.jump.sdk.srpreleasebenchmark"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.jump.sdk.srpreleasebenchmark"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1"
    }
    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
dependencies {
    implementation(project(":"))
    implementation("com.ionspin.kotlin:bignum:${providers.gradleProperty("bignumVersion").getOrElse("0.3.8")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
}
