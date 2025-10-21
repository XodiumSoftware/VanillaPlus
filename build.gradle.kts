@file:Suppress("ktlint:standard:no-wildcard-imports")

import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml
import xyz.jpenilla.runtask.task.AbstractRun

plugins {
    id("java")
    id("idea")
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "9.2.2"
    id("xyz.jpenilla.run-paper") version "3.0.1"
}

val mcVersion = "1.21.10"

group = "org.xodium.vanillaplus.VanillaPlus"
version = mcVersion
description = "Minecraft plugin that enhances the base gameplay"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.jpenilla.xyz/snapshots/") {
        mavenContent {
            snapshotsOnly()
            includeModule("org.incendo.interfaces", "interfaces-core")
            includeModule("org.incendo.interfaces", "interfaces-paper")
            includeModule("org.incendo.interfaces", "interfaces-kotlin")
        }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$version-R0.1-SNAPSHOT")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.0")
    implementation("org.incendo.interfaces:interfaces-paper:1.0.0-SNAPSHOT")
    implementation("org.incendo.interfaces:interfaces-kotlin:1.0.0-SNAPSHOT")
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:6.1.0")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.55"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
    }
}

idea { module { excludeDirs.addAll(files("run", ".kotlin")) } }

tasks {
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        destinationDirectory.set(layout.projectDirectory.dir("build/libs"))
        relocate("com.fasterxml.jackson", "$group.jackson")
        relocate("org.incendo", "$group.gui")
        minimize { exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*")) }
    }
    jar { enabled = false }
    runServer { minecraftVersion(mcVersion) }
    withType<JavaCompile> { options.encoding = "UTF-8" }
    withType(AbstractRun::class) { jvmArgs("-XX:+AllowEnhancedClassRedefinition") }
}

paperPluginYaml {
    main.set(group.toString())
    authors.add("Xodium")
    apiVersion.set(version)
    dependencies {
        server(name = "FastAsyncWorldEdit", load = PaperPluginYaml.Load.BEFORE, required = false, joinClasspath = true)
    }
}
