val kspVersion: String by project
val kotlinPoetVersion: String by project

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}

kotlin {
    sourceSets.main {
        kotlin.srcDirs("src/main/kotlin")
    }
}
