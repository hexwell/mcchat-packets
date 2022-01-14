plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

dependencies {
    val kspVersion: String by project
    val kotlinPoetVersion: String by project
    val autoserviceVersion: String by project
    val autoserviceKspVersion: String by project

    implementation(kotlin("stdlib"))
    implementation("com.google.auto.service:auto-service-annotations:$autoserviceVersion")
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")

    ksp("dev.zacsweers.autoservice:auto-service-ksp:$autoserviceKspVersion")
}

kotlin {
    sourceSets.main {
        kotlin.srcDirs("src/main/kotlin")
    }
}
