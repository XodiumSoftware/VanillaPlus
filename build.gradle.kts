@file:Suppress("ktlint:standard:no-wildcard-imports")

import java.util.*

val pluginYmlFile = file("src/main/resources/paper-plugin.yml")
val pluginProperties = Properties().apply { pluginYmlFile.inputStream().use { load(it) } }

fun getPluginProperty(key: String): String =
    pluginProperties.getProperty(key)
        ?: error("'$key' not found in paper-plugin.yml")

val pluginVersion = getPluginProperty("version")
val pluginDescription = getPluginProperty("description")
val mainClass = getPluginProperty("main")
val pluginGroup = mainClass.substringBeforeLast('.')
val apiVersion = Regex("""^(\d+\.\d+\.\d+)""").find(pluginVersion)?.groupValues?.get(1) ?: pluginVersion

plugins {
    id("java")
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0"
}

group = pluginGroup
version = pluginVersion
description = pluginDescription

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.15") // TODO("Move away from WorldEdit")

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2")
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

tasks {
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        destinationDirectory.set(layout.projectDirectory.dir("build/libs"))
        relocate("com.fasterxml.jackson", "$pluginGroup.jackson")
        minimize { exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*")) }
    }
    jar { enabled = false }
    withType<JavaCompile> { options.encoding = "UTF-8" }
}
