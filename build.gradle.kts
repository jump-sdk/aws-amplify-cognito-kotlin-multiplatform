plugins {
    id("com.android.library").version("8.2.0-rc03").apply(false)
    kotlin("multiplatform").version("1.9.20").apply(false)
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
    id("com.github.ben-manes.versions") version "0.50.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.hbmartin")
            }
        }
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
        val detektVersion = "1.23.3"
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:$detektVersion")
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
        detektPlugins("com.braisgabin.detekt:kotlin-compiler-wrapper:0.0.4")
        detektPlugins("com.github.hbmartin:hbmartin-detekt-rules:0.1.3")
    }
}
