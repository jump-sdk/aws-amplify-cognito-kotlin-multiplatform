plugins {
    kotlin("multiplatform") version "2.2.10"
    id("com.android.library") version "8.13.2"
    id("com.android.application") version "8.13.2" apply false
}
val srpBackend = providers.gradleProperty("srpBackend").getOrElse("native")
require(srpBackend in setOf("native", "kotlin")) { "srpBackend must be native or kotlin" }
val androidBackend = providers.gradleProperty("androidBackend").getOrElse("java")
require(androidBackend in setOf("kotlin", "java")) { "androidBackend must be kotlin or java" }
val bignumVersion = providers.gradleProperty("bignumVersion").getOrElse("0.3.8")
kotlin {
    androidTarget { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
    iosSimulatorArm64 {
        providers.gradleProperty("simulatorId").orNull?.let { testRuns["test"].deviceId = it }
        binaries.test(listOf(org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE))
        testRuns.create("release") {
            setExecutionSourceFrom(binaries.getTest(org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE))
            providers.gradleProperty("simulatorId").orNull?.let { deviceId = it }
        }
    }
    sourceSets {
        commonMain {
            kotlin.srcDir("../amplifyframework/src/commonMain/kotlin")
            dependencies {
                implementation("com.ionspin.kotlin:bignum:$bignumVersion")
                implementation("org.kotlincrypto:secure-random:0.1.0")
                implementation("org.kotlincrypto.macs:hmac-sha2:0.3.0")
                implementation("org.kotlincrypto.hash:sha2:0.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("io.ktor:ktor-client-core:2.3.6")
            }
        }
        androidMain {
            kotlin.srcDir(if (androidBackend == "java") "../amplifyframework/src/androidMain/kotlin" else "src/kotlinArithmetic/kotlin")
        }
        androidInstrumentedTest {
            kotlin.srcDir("../amplifyframework/src/commonTest/kotlin")
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("androidx.test:runner:1.6.2")
            }
        }
        iosMain {
            kotlin.srcDir(if (srpBackend == "native") "../amplifyframework/src/iosMain/kotlin" else "src/kotlinArithmetic/kotlin")
        }
        commonTest {
            kotlin.srcDir("../amplifyframework/src/commonTest/kotlin")
            dependencies { implementation(kotlin("test")) }
        }
    }
}

tasks.withType<org.gradle.api.tasks.testing.AbstractTestTask>().configureEach { testLogging.showStandardStreams = true }

if (srpBackend == "native") apply(from = file("../gradle/ios-srp-native.gradle"))

android {
    namespace = "com.jump.sdk.srpbenchmark"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
