/*
 * Copyright (c) 2025. Xodium.
 * All rights reserved.
 */

plugins {
    id("java")
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta6"
}

group = "org.xodium.vanillaplus"
version = "1.0.0"
description = "Minecraft plugin that enhances the base gameplay."

var pluginName: String = "VanillaPlus"
var apiVersion: String = "1.21.4"
var authors: List<String> = listOf("XodiumSoftware")

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    // TODO: use stable build when available.
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.0-20250109.111726-21")

    implementation("net.kyori:adventure-api:4.18.0")
    implementation(kotlin("stdlib-jdk8"))
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
}
