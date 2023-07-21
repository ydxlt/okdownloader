plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    application
}

dependencies {
    implementation(project(path = ":okdownloader"))
}