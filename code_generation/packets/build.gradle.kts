plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":processor"))

    ksp(project(":processor"))
}

kotlin {
    sourceSets.main {
        kotlin.srcDirs(
            "src/main/kotlin",
            "build/generated/ksp/main/kotlin"
        )
    }
}
