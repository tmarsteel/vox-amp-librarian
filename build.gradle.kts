plugins {
    kotlin("js") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    id("io.kotest.multiplatform") version "5.5.4"
}

group = "com.github.tmarsteel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
            commonWebpackConfig {

            }
        }
        binaries.executable()
    }
}

dependencies {
    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.354"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")

    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    implementation(npm("bootstrap", "5.2.3"))

    testImplementation("io.kotest:kotest-framework-engine:5.5.4")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
}

rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
    versions.webpackCli.version = "4.10.0"
}