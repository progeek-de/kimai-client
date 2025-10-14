import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    id("com.codingfeline.buildkonfig")
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()

        // Compile with Java 17 for ProGuard compatibility
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":kimai-shared"))

                implementation(compose.desktop.currentOs)
                implementation(libs.decompose.extensionsComposeJetbrains)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.dorkbox.systemTray)
                implementation(libs.dorkbox.os)
            }
        }
    }
}

tasks {
    withType<org.gradle.jvm.tasks.Jar> {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE*")
        exclude("META-INF/NOTICE*")
        exclude("**/*.kotlin_metadata")
        exclude("**/*.kotlin_builtins")

        // Minimize JAR size
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        // Compress resources
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    // Optimize compilation
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjsr305=strict",
                "-Xjvm-default=all",
                "-Xno-param-assertions",
                "-Xno-call-assertions",
                "-Xno-receiver-assertions"
            )
        }
    }
}


buildkonfig {
    packageName = "de.progeek.kimai.desktop"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
    }

    defaultConfigs("dev") {
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
    }
}


compose.desktop {
    application {
        mainClass = "de.progeek.kimai.desktop.MainKt"

        val projectVersion = when(project.hasProperty("projVersion")) {
            true -> project.properties["projVersion"]?.toString()
            false -> libs.versions.project.get()
        }

        buildTypes.release.proguard {
            configurationFiles.from("rules.pro")
            isEnabled.set(true)
            optimize.set(true)
            obfuscate.set(false)
        }

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "kimai"
            packageVersion = projectVersion
            description = "Kimai Time Tracking Desktop Client"
            copyright = "© 2025 Progeek"
            vendor = "Progeek"

            // Minimize JVM modules for smaller size
            modules(
                "java.base",
                "java.desktop",
                "java.logging",
                "java.net.http",
                "java.sql",
                "java.naming",
                "jdk.crypto.ec"
            )

            linux {
                iconFile.set(project.file("src/jvmMain/resources/kimai_logo.png"))
            }

            windows {
                iconFile.set(project.file("src/jvmMain/resources/kimai_logo.ico"))
                menuGroup = "Kimai"
                perUserInstall = true
                dirChooser = true
                upgradeUuid = "a4e8c89b-14f3-4657-9fe3-a9c6e8a38893"
            }
        }
    }
}