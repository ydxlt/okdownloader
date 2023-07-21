plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.vanniktech.maven.publish")
}

dependencies {
    testImplementation(project(path = ":fakedata"))
    testImplementation(libs.test.junit)
    api(libs.okhttp)
    implementation(libs.commons.codec)
    implementation(libs.okio)
}