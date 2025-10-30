@file:Suppress("ktlint:standard:no-wildcard-imports")

import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml
import xyz.jpenilla.runtask.task.AbstractRun

plugins {
    id("java")
    id("idea")
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "9.2.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

val mcVersion = "1.21.10"

group = "org.xodium.vanillaplus.VanillaPlus"
version = mcVersion
description = "Minecraft plugin that enhances the base gameplay"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$version-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("$version-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.17") // TODO("Move away from WorldEdit")

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.0")
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:6.1.0")
    implementation("io.netty:netty-buffer:4.2.7.Final")
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
        server(name = "WorldEdit", load = PaperPluginYaml.Load.BEFORE, required = false, joinClasspath = true)
    }
}
