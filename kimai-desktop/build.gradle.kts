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
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Pkg)
            packageName = "kimai"
            packageVersion = projectVersion
            modules("java.sql")
        }
    }
}
