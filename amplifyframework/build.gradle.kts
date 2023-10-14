plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
}

group = "com.jump.sdk.amplifyframework"
version = System.getenv()["GITHUB_RUN_NUMBER"] ?: "1"

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()
    androidTarget {
        publishLibraryVariants("release")
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "amplifyframework"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains:annotations:24.0.1")
                implementation("com.ionspin.kotlin:bignum:0.3.8")
                implementation("org.kotlincrypto:secure-random:0.1.0")
                implementation(platform("org.kotlincrypto.macs:bom:0.3.0"))
                implementation("org.kotlincrypto.macs:hmac-sha2")
                implementation(platform("org.kotlincrypto.hash:bom:0.3.0"))
                implementation("org.kotlincrypto.hash:sha2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("io.ktor:ktor-client-core:2.3.5")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.jump.sdk.amplifyframework"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        checkDependencies = true
        abortOnError = true
        checkDependencies = true
        ignoreWarnings = false
        checkAllWarnings = true
        warningsAsErrors = true
        explainIssues = true
        showAll = true
        xmlReport = true
        htmlReport = false
        baseline = file("lint-baseline.xml")
        disable.add("InvalidPackage")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/jump-sdk/mobile-sdk-android")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
