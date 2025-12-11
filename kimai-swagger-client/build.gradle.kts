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
    jvmToolchain(21)
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

// Configure test task to not fail when no tests are discovered (auto-generated module)
tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}
