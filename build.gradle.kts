plugins {
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.detekt)
    alias(libs.plugins.benManes.versions)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        allRules = true
        config.setFrom(files(rootProject.file("detekt.yml")))
        reports {
            xml.required.set(true)
            txt.required.set(false)
            html.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
    dependencies {
        detektPlugins(libs.detekt.libraries)
        detektPlugins(libs.detekt.formatting)
        detektPlugins(libs.detekt.kotlinCompilerWrapper)
        detektPlugins(libs.detekt.hbmartin)
    }
}
