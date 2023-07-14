plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

apply(from = "../maven_publish.gradle")

dependencies {
    testImplementation(project(path = ":fakedata"))
    testImplementation(libs.test.junit)
    api(libs.okhttp)
    implementation(libs.commons.codec)
    implementation(libs.okio)
}