/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

import de.undercouch.gradle.tasks.download.Download
import groovy.json.JsonSlurper
import java.net.URI

plugins {
    id("java")
    kotlin("jvm") version "2.1.21"
    id("com.gradleup.shadow") version "9.0.0-beta15"
    id("de.undercouch.download") version "5.6.0"
}

group = "org.xodium.vanillaplus"
version = "1.9.1"
description = "Minecraft plugin that enhances the base gameplay."

var apiVersion: String = "1.21.5"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.13") //TODO("Move away from WorldEdit")
    implementation(kotlin("stdlib-jdk8"))
    implementation("dev.kord:kord-core:0.15.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

tasks {
    processResources {
        filesMatching("paper-plugin.yml") {
            expand(
                mapOf(
                    "version" to version,
                    "description" to description,
                )
            )
        }
    }
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        destinationDirectory.set(file(".server/plugins/update"))
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
        val hotswapAgentPath = file(".hotswap/hotswap-agent-core.jar").absolutePath
        commandLine = listOf(
            javaExec,
            "-javaagent:$hotswapAgentPath",
            "-XX:+AllowEnhancedClassRedefinition",
            "-jar", "server.jar", "nogui"
        )
    }
}

