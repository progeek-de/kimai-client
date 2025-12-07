@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.ktor.client)
                implementation(libs.kotlin.serialization)
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

// Suppress warnings in auto-generated OpenAPI client code
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
        // Suppress all warnings for auto-generated code
        suppressWarnings.set(true)
    }
}