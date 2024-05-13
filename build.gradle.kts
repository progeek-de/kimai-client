@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.multiplatform.resources).apply(false)
    alias(libs.plugins.jlleitschuh.gradle.ktlint).apply(false)
}

buildscript {
    dependencies {
        classpath(libs.moko.resources.generator)
        classpath(libs.buildkonfig.gradle.plugin)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
