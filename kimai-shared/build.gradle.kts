import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.multiplatform.resources)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.jlleitschuh.gradle.ktlint)
    id("com.codingfeline.buildkonfig")
    jacoco
}

dependencies {
    ktlintRuleset(libs.compose.rules.ktlint)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
            // Configure headless mode for Compose Desktop UI tests
            // Use software rendering and set AWT to headless mode
            jvmArgs(
                "-Djava.awt.headless=true",
                "-Dskiko.renderApi=SOFTWARE_FAST",
                "-Dskiko.fps.enabled=false"
            )
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kimai-swagger-client"))

                // compose
                api(compose.ui)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(compose.preview)

                // kotlinx
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                api(libs.jetbrains.compose.preview)

                implementation(libs.bundles.ktor.client)
                implementation(libs.kotlin.serialization)

                // decompose
                api(libs.decompose.decompose)
                api(libs.essenty.lifecycle)
                implementation(libs.decompose.decompose)
                implementation(libs.decompose.extensionsCompose)

                // koin
                api(libs.bundles.koin)

                // MVI Kotlin
                api(libs.mvikotlin.core)
                api(libs.mvikotlin.main)
                api(libs.mvikotlin.coroutines)
                api(libs.mvikotlin.logging)
                api(libs.mvikotlin.timetravel)

                // Multiplatform settings
                api(libs.settings.core)
                api(libs.settings.coroutines)
                api(libs.settings.noarg)

                // moko
                api(libs.moko.resources.core)
                api(libs.moko.resources.compose)

                // crypto
                implementation(libs.cryptography.core)

                // sqldelight
                api(libs.sqldelight.coroutines.extensions)
                api(libs.sqldelight.primitive.adapters)

                // arrow
                api(libs.arrow.core)

                // store5
                implementation(libs.store.core)
                implementation(libs.jetbrains.kotlinx.atomicfu)

                // napier logging
                implementation(libs.napier)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.moko.resources.test)

                // testing framework
                implementation(libs.mockk)
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)

                // sqldelight in-memory driver for testing
                implementation(libs.sqldelight.sqlite.driver)

                // settings for test mocks
                implementation(libs.settings.core)
                implementation(libs.settings.noarg)

                // koin for testing
                implementation(libs.koin.core)
                implementation("io.insert-koin:koin-test:4.0.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.cryptography.jdk)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                implementation(compose.desktop.currentOs) // Include native Skiko binaries
                implementation(libs.junit)
                implementation(kotlin("test"))
                implementation(libs.mockk)
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
                // Provides Dispatchers.Main for JVM tests (using Swing event loop)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.koin.core)
                implementation("io.insert-koin:koin-test:4.0.0")
                implementation(libs.sqldelight.sqlite.driver)
                implementation(libs.settings.core)
                implementation(libs.settings.noarg)
            }
        }

        // Android and iOS sourceSets removed - focus on desktop only
    }
}

buildkonfig {
    packageName = "de.progeek.kimai.shared"

    val projectVersion: String = when (project.hasProperty("projVersion")) {
        true -> project.properties["projVersion"].toString()
        false -> libs.versions.project.orElse("1.0.0").get()
    }

    val server = when (project.hasProperty("projServer")) {
        true -> project.properties["projServer"]?.toString()
        false -> "https://kimai.cloud"
    }

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "KIMAI_SERVER", server)
        buildConfigField(FieldSpec.Type.STRING, "KIMAI_VER", projectVersion)
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
    }

    defaultConfigs("dev") {
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
    }

    targetConfigs("dev") {
        create("jvm") {
        }
    }
}

// Android configuration removed - focus on desktop only

multiplatformResources {
    resourcesPackage.set("de.progeek.kimai.shared") // required
    resourcesClassName.set("SharedRes")
}

sqldelight {
    databases {
        create("KimaiDatabase") {
            packageName.set("de.progeek.kimai.shared.core.database")
        }
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    debug.set(true)
    verbose.set(true)
    ignoreFailures.set(false)
    outputToConsole.set(true)
    filter {
        exclude { element ->
            element.file.path.contains("build")
        }
    }
    reporters {
        reporter(ReporterType.HTML)
        reporter(ReporterType.PLAIN)
    }
}
// JaCoCo test coverage configuration
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("jvmTest"))
    group = "verification"
    description = "Generate JaCoCo coverage reports for JVM tests"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(
            fileTree("${layout.buildDirectory.get()}/classes/kotlin/jvm/main") {
                exclude(
                    "**/database/**", // Exclude SQLDelight generated code
                    "**/buildkonfig/**", // Exclude BuildKonfig generated code
                    "**/*\$*" // Exclude inner classes
                )
            }
        )
    )

    sourceDirectories.setFrom(files("src/commonMain/kotlin"))
    executionData.setFrom(files("${layout.buildDirectory.get()}/jacoco/jvmTest.exec"))

    doLast {
        println("JaCoCo report generated: ${reports.html.outputLocation.get()}/index.html")
    }
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("jacocoTestReport"))
    group = "verification"
    description = "Verify JaCoCo coverage meets minimum threshold"

    violationRules {
        rule {
            limit {
                minimum = "0.60".toBigDecimal() // 60% minimum coverage
            }
        }
    }

    classDirectories.setFrom(
        files(
            fileTree("${layout.buildDirectory.get()}/classes/kotlin/jvm/main") {
                exclude(
                    "**/database/**",
                    "**/buildkonfig/**",
                    "**/*\$*"
                )
            }
        )
    )

    executionData.setFrom(files("${layout.buildDirectory.get()}/jacoco/jvmTest.exec"))
}
