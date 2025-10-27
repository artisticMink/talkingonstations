import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.util.Path.path
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "maver.talkingonstations"

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.gradleup.shadow") version "9.2.2"
    id("application")
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("maver.ChatTestKt")
}

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // LazyLib will provide kotlin runtime
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

sourceSets {
    main {
        java {
            // Prevents decompiling of starfarer.api.jar
            srcDirs("/starfarer-api/")
        }
    }
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

    // Workaround for https://github.com/GradleUp/shadow/issues/713
    dependsOn("distTar", "distZip")
}

/**
 * Compile, create fat jar and assemble mod folder
 */
tasks.register<Copy>("packageMod") {
    dependsOn(tasks.shadowJar)

    val starsectorModFolder = "D:/StarsectorDev/mods"
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
