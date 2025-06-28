pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
        create("kotlincrypto") {
            from("org.kotlincrypto:version-catalog:0.7.1")
        }
    }
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AWS_Amplify_Cognito_Multiplatform"
include(":amplifyframework")