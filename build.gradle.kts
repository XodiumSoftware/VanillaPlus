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
    id("idea")
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1"
}

group = pluginGroup
version = if (project.hasProperty("buildVersion")) project.property("buildVersion")!! else pluginVersion
description = pluginDescription

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.15") // TODO("Move away from WorldEdit")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.9.6")

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
    }
}

idea { module { excludeDirs.add(file("run")) } }

tasks {
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        destinationDirectory.set(layout.projectDirectory.dir("build/libs"))
        relocate("com.fasterxml.jackson", "$pluginGroup.jackson")
        minimize { exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*")) }
    }
    jar { enabled = false }
    runServer { minecraftVersion(apiVersion) }
    withType<JavaCompile> { options.encoding = "UTF-8" }
    withType(xyz.jpenilla.runtask.task.AbstractRun::class) { jvmArgs("-XX:+AllowEnhancedClassRedefinition") }
}
