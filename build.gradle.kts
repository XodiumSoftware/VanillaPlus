/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper
import java.net.URI

plugins {
    id("java")
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("de.undercouch.download") version "5.6.0"
}

group = "org.xodium.vanillaplus"
version = "1.8.0"
description = "Minecraft plugin that enhances the base gameplay."

var apiVersion: String = "1.21.5"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.jeff-media.com/public/")
    maven("https://repo.triumphteam.dev/snapshots")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.11") //TODO("Move away from WorldEdit")
    implementation(kotlin("stdlib-jdk8"))
    implementation("de.jeff_media:ChestSortAPI:12.0.0")
    implementation("dev.triumphteam:triumph-gui-paper-kotlin:4.0.0-SNAPSHOT") {
        exclude(group = "com.google.guava", module = "guava")
    }
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

tasks {
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
                    ?.findLast { it["channel"] == "default" }
                    ?: (builds as? List<*>)?.mapNotNull { it as? Map<*, *> }
                        ?.findLast { it["channel"] == "experimental" }
            }?.get("build")
                ?: throw GradleException("No build with channel='default' or 'experimental' found.")
            src("https://api.papermc.io/v2/projects/paper/versions/$apiVersion/builds/$latestBuild/downloads/paper-$apiVersion-$latestBuild.jar")
            dest(file(".server/server.jar"))
            onlyIfModified(true)
        }
    }
    register("acceptEula") {
        group = "application"
        description = "Accept EULA before running the server"
        doLast { file(".server/eula.txt").writeText("eula=true\n") }
    }
    register<Exec>("runDevServer") {
        group = "application"
        description = "Run Development Server"
        dependsOn("shadowJar", "downloadServerJar", "acceptEula")
        workingDir = file(".server")
        val javaLauncher: JavaLauncher = project.extensions
            .getByType(JavaToolchainService::class.java)
            .launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) }
            .get()
        val javaHome: String = javaLauncher.metadata.installationPath.asFile.absolutePath
        commandLine = listOf(
            "$javaHome/bin/java",
            "-jar",
            "server.jar",
            "nogui"
        )
    }
}
