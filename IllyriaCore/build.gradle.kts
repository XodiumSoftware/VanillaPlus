// IllyriaCore - Core gameplay enhancements

import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import xyz.jpenilla.runtask.task.AbstractRun

plugins {
    id("java")
    id("idea")
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
    id("org.jetbrains.dokka") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
}

val mcVersion = rootProject.extra["mcVersion"] as String
val buildNumber = rootProject.extra["buildNumber"] as String
val javaVersion = rootProject.extra["javaVersion"] as String

group = "org.xodium.illyriaplus.IllyriaPlus"
version = "$mcVersion+build.$buildNumber"
description = "Minecraft plugin that enhances the base gameplay"

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion.build.+")
    implementation(kotlin("stdlib"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
    }
}

sourceSets { main { kotlin { srcDirs("src") } } }

dokka {
    moduleName.set("IllyriaCore")

    dokkaSourceSets.main {
        documentedVisibilities.set(
            setOf(
                VisibilityModifier.Public,
                VisibilityModifier.Internal,
            ),
        )
        sourceRoots.from("src")
    }

    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("docs"))
    }
}

tasks {
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        destinationDirectory.set(layout.projectDirectory.dir("build/libs"))
        minimize()
    }
    jar { enabled = false }
    runServer { minecraftVersion(mcVersion) }
    withType<JavaCompile> { options.encoding = "UTF-8" }
    withType(AbstractRun::class) { jvmArgs("-XX:+AllowEnhancedClassRedefinition") }
}

paperPluginYaml {
    main.set(group.toString())
    website.set("https://github.com/XodiumSoftware/IllyriaPlus")
    authors.add("Xodium")
    apiVersion.set(mcVersion)
    bootstrapper.set("org.xodium.illyriaplus.IllyriaPlusBootstrap")
}
