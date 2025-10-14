import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.multiplatform.resources)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.jlleitschuh.gradle.ktlint)
    id("kotlin-parcelize")
    id("com.codingfeline.buildkonfig")
}

dependencies {
    ktlintRuleset(libs.twitter.compose.rules)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = libs.versions.jvmTarget.get()
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"

            export(libs.decompose.decompose)
            export(libs.essenty.lifecycle)
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
                implementation(libs.decompose.extensionsComposeJetbrains)

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

                //napier logging
                implementation(libs.napier)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.moko.resources.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                dependsOn(commonMain)

                implementation(libs.cryptography.jdk)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }

        val androidMain by getting {
            dependencies {
                dependsOn(commonMain)

                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.cryptography.jdk)

                implementation(libs.sqldelight.android.driver)

                // Koin
                implementation(libs.koin.android)
            }
        }

        val iosMain by getting {
            dependsOn(commonMain)

            dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

buildkonfig {
    packageName = "de.progeek.kimai.shared"

    val projectVersion = when(project.hasProperty("projVersion")) {
        true -> project.properties["projVersion"]?.toString()
        false -> libs.versions.project.orNull
    }

    val server = when(project.hasProperty("projServer")) {
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

android {
    namespace = "de.progeek.kimai.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "de.progeek.kimai.shared" // required
    multiplatformResourcesClassName = "SharedRes"
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
    ignoreFailures.set(true)
    outputToConsole.set(true)
    filter {
        exclude { entry ->
            entry.file.toString().contains("build/generated")
        }
    }
    reporters {
        reporter(ReporterType.HTML)
        reporter(ReporterType.PLAIN)
    }
}