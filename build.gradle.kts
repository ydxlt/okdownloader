// Top-level build file where you can add configuration options common to all sub-projects/modules.
import downloader.by
import downloader.groupId
import downloader.versionName
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import groovy.util.Node
import groovy.util.NodeList

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.gradlePlugin.android)
        classpath(libs.gradlePlugin.kotlin)
        classpath(libs.gradlePlugin.mavenPublish)
    }
}

plugins {
    alias(libs.plugins.hilt) apply false
}

allprojects {
    // Necessary to publish to Maven.
    group = groupId
    version = versionName

    // Target JVM 8.
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
        options.compilerArgs = options.compilerArgs + "-Xlint:-options"
    }
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget by JvmTarget.JVM_1_8
    }

    dependencies {
        modules {
            module("org.jetbrains.kotlin:kotlin-stdlib-jdk7") {
                replacedBy("org.jetbrains.kotlin:kotlin-stdlib")
            }
            module("org.jetbrains.kotlin:kotlin-stdlib-jdk8") {
                replacedBy("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }
    }

    // Uninstall test APKs after running instrumentation tests.
    tasks.whenTaskAdded {
        if (name == "connectedDebugAndroidTest") {
            finalizedBy("uninstallDebugAndroidTest")
        }
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
            )
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    plugins.withId("com.vanniktech.maven.publish.base") {
        val publishingExtension = extensions.getByType(PublishingExtension::class.java)
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
            signAllPublications()
            pom {
                name.set(project.name)
                description.set("Square’s meticulous HTTP client for Java and Kotlin.")
                url.set("https://ydxlt.github.io/okdownloader/")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/ydxlt/okdownloader.git")
                    developerConnection.set("scm:git:ssh://git@github.com/ydxlt/okdownloader.git")
                    url.set("https://github.com/ydxlt/okdownloader")
                }
                developers {
                    developer {
                        name.set("Billbook, Inc.")
                    }
                }
            }

            // Configure the kotlinMultiplatform artifact to depend on the JVM artifact in pom.xml only.
            // This hack allows Maven users to continue using our original OkDownloader artifact names (like
            // com.billbook.okdownloader:okdownloader:1.x.y) even though we changed that artifact from JVM-only
            // to Kotlin Multiplatform. Note that module.json doesn't need this hack.
            val mavenPublications = publishingExtension.publications.withType<MavenPublication>()
            mavenPublications.configureEach {
                if (name != "jvm") return@configureEach
                val jvmPublication = this
                val kmpPublication = mavenPublications.getByName("kotlinMultiplatform")
                kmpPublication.pom.withXml {
                    val root = asNode()
                    val dependencies = (root["dependencies"] as NodeList).firstOrNull() as Node?
                        ?: root.appendNode("dependencies")
                    for (child in dependencies.children().toList()) {
                        dependencies.remove(child as Node)
                    }
                    dependencies.appendNode("dependency").apply {
                        appendNode("groupId", jvmPublication.groupId)
                        appendNode("artifactId", jvmPublication.artifactId)
                        appendNode("version", jvmPublication.version)
                        appendNode("scope", "compile")
                    }
                }
            }
        }
    }
}