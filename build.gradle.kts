import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "maver.talkingonstations"

val starsectorModFolder: String = providers.gradleProperty("starsectorModFolder").get()

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    id("com.gradleup.shadow") version "9.4.1"
}

repositories {
    mavenCentral()
}

// Target Java 17
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:okhttp-coroutines:5.3.2") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
    }



    // LazyLib will provide kotlin runtime and coroutines
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

// Prefer fat jar
tasks.jar {
    enabled = false
}

/**
 * Compile and create a fat jar containing additional third-party packages
 */
tasks.withType<ShadowJar> {
    archiveBaseName.set("TalkingOnStations")
    archiveClassifier.set("")
}

/**
 * Compile, create fat jar and assemble mod folder
 */
tasks.register<Copy>("packageMod") {
    dependsOn(tasks.shadowJar)

    val tosFolder = "$starsectorModFolder/TalkingOnStations"

    doFirst {
        val destinationDir = file(tosFolder)
        if (destinationDir.exists()) {
            destinationDir.deleteRecursively()
        }
    }

    destinationDir = file(starsectorModFolder)
    duplicatesStrategy = DuplicatesStrategy.FAIL

    from(tasks.shadowJar.get().archiveFile) {
        into("TalkingOnStations/jars")
    }

    from("src/main/modfiles/") {
        into("TalkingOnStations/")
    }
}
