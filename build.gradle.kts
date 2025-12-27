plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.multiplatform.resources).apply(false)
    alias(libs.plugins.jlleitschuh.gradle.ktlint).apply(false)
    alias(libs.plugins.detekt).apply(false)
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