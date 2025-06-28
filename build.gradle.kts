plugins {
    id("com.android.library").version("8.10.0").apply(false)
    kotlin("multiplatform").version("2.1.21").apply(false)
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.github.ben-manes.versions") version "0.52.0"
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
        val detektVersion = "1.23.8"
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:$detektVersion")
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
        detektPlugins("com.braisgabin.detekt:kotlin-compiler-wrapper:0.0.4")
        detektPlugins("com.github.hbmartin:hbmartin-detekt-rules:0.1.7")
    }
}
