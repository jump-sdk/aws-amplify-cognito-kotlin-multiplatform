import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    id("maven-publish")
}

group = "com.jump.sdk.amplifyframework"
version = System.getenv()["GITHUB_RUN_NUMBER"] ?: "1"

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        publishLibraryVariants("release")
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "amplifyframework"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.jetbrainsAnnotations)
                implementation(libs.bignum)
                implementation(kotlincrypto.random.secure)
                implementation(platform(kotlincrypto.macs.bom))
                implementation(kotlincrypto.macs.hmac.sha2)
                implementation(platform(kotlincrypto.hash.bom))
                implementation(kotlincrypto.hash.sha2)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.core)
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
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
            url = uri("https://maven.pkg.github.com/jump-sdk/aws-amplify-cognito-kotlin-multiplatform")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xexpect-actual-classes"
    }
}

tasks.withType<DependencyUpdatesTask> {
    checkForGradleUpdate = true
    rejectVersionIf {
        listOf("-dev-", "-alpha").any { word ->
            candidate.version.contains(word)
        }
    }
}
