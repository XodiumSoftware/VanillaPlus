/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper
import java.net.URI

plugins {
    id("java")
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0-beta17"
    id("de.undercouch.download") version "5.6.0"
}

group = "org.xodium.vanillaplus"
version = "1.14.0"
description = "Minecraft plugin that enhances the base gameplay."

var author: String = "Xodium"
var apiVersion: String = "1.21.7"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://nexus.frengor.com/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.15-SNAPSHOT") //TODO("Move away from WorldEdit")
    compileOnly("com.frengor:ultimateadvancementapi-shadeable:2.5.1")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.1")
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

tasks {
    processResources {
        filesMatching("paper-plugin.yml") {
            expand(
                mapOf(
                    "version" to version,
                    "description" to description,
                    "author" to author,
                )
            )
        }
    }
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        destinationDirectory.set(file(".server/plugins/update"))
        relocate("com.fasterxml.jackson", "org.xodium.vanillaplus.jackson")
        relocate("com.frengor.ultimateadvancementapi-shadeable", "org.xodium.vanillaplus.advancements")
        minimize { exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*")) }
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
            fun findLatestBuild(builds: List<Map<*, *>>): Map<*, *>? {
                return builds.findLast { it["channel"] == "default" }
                    ?: builds.findLast { it["channel"] == "experimental" }
            }

            val buildsUrl = URI("https://api.papermc.io/v2/projects/paper/versions/$apiVersion/builds").toURL()
            val response = JsonSlurper().parse(buildsUrl) as? Map<*, *>
                ?: throw GradleException("Failed to parse PaperMC builds API response.")
            val builds = response["builds"] as? List<*>
                ?: throw GradleException("No 'builds' key in PaperMC API response.")
            val buildMapList = builds.mapNotNull { it as? Map<*, *> }
            val latestBuild = findLatestBuild(buildMapList)
                ?: throw GradleException("No build with channel='default' or 'experimental' found.")
            val buildNumber = latestBuild["build"] ?: throw GradleException("Build number missing in build info.")

            src("https://api.papermc.io/v2/projects/paper/versions/$apiVersion/builds/$buildNumber/downloads/paper-$apiVersion-$buildNumber.jar")
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
        workingDir = file(".server/")
        standardInput = System.`in`
        val javaExec = project.extensions.getByType(JavaToolchainService::class.java)
            .launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) }
            .get().executablePath.asFile.absolutePath
        commandLine = listOf(
            javaExec,
            "-XX:+AllowEnhancedClassRedefinition",
            "-jar", "server.jar", "nogui"
        )
    }
}

