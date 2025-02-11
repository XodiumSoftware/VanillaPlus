/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper
import java.net.URI

/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

plugins {
    id("java")
    kotlin("jvm") version "2.1.10"
    id("com.gradleup.shadow") version "9.0.0-beta8"
    id("de.undercouch.download") version "5.6.0"
}

group = "org.xodium.vanillaplus"
version = "1.1.1"
description = "Minecraft plugin that enhances the base gameplay."

var pluginName: String = "VanillaPlus"
var apiVersion: String = "1.21.4"
var authors: List<String> = listOf("XodiumSoftware")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.triumphteam.dev/snapshots")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
//    TODO("Use stable build when available")
//    TODO("Move away from WorldEdit")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.0-20250126.070433-22")
    implementation("net.kyori:adventure-api:4.18.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
    implementation("dev.triumphteam:triumph-gui-paper-kotlin:4.0.0-SNAPSHOT") {
        exclude(group = "com.google.guava", module = "guava")
    }
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

tasks {
    processResources {
        filesMatching("paper-plugin.yml") {
            expand(
                mapOf(
                    "version" to version,
                    "description" to description,
                    "name" to pluginName,
                    "apiVersion" to apiVersion,
                    "authors" to authors.joinToString(", ")
                )
            )
        }
    }
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        relocate("kotlin", "org.xodium.vanillaplus.kotlin")
        relocate("dev.triumphteam.gui", "org.xodium.vanillaplus.gui")
        destinationDirectory.set(file(".server/plugins"))
        minimize()
        doLast {
            copy {
                from(archiveFile)
                into(layout.buildDirectory.dir("libs"))
            }
        }
    }
    jar { enabled = false }
    withType<JavaCompile> { options.encoding = "UTF-8" }
    register("printVersion") { doLast { println(version) } }
    register<Download>("downloadServerJar") {
        group = "application"
        description = "Download the PaperMC server jar"
        doFirst {
            val latestBuild = (JsonSlurper().parse(
                URI("https://api.papermc.io/v2/projects/paper/versions/$apiVersion/builds").toURL()
            ) as? Map<*, *>)?.get("builds")?.let { builds ->
                (builds as? List<*>)?.mapNotNull { it as? Map<*, *> }
                    ?.findLast { it["channel"] == "default" }?.get("build")
            } ?: throw GradleException("No build with channel='default' found.")
            src("https://api.papermc.io/v2/projects/paper/versions/$apiVersion/builds/$latestBuild/downloads/paper-$apiVersion-$latestBuild.jar")
            dest(file(".server/server.jar"))
            onlyIfModified(true)
        }
    }
    register("acceptEula") {
        group = "application"
        description = "Accept EULA before running the server"
        doLast {
            file(".server/eula.txt").apply {
                if (!exists()) {
                    createNewFile()
                    println("Created eula.txt file.")
                }
                writeText("eula=true\n")
            }.also { println("EULA has been accepted.") }
        }
    }
    register<Exec>("runDevServer") {
        group = "application"
        description = "RUN: DEV SERVER"
        workingDir = file(".server")
        dependsOn("downloadServerJar", "acceptEula", "shadowJar")
        commandLine = listOf("java", "-jar", "server.jar", "nogui")
    }
}
