import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import xyz.jpenilla.runtask.task.AbstractRun

plugins {
    id("java")
    id("idea")
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper")
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
    id("org.jetbrains.dokka") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
}

val mcVersion = rootProject.extra["mcVersion"] as String
val buildNumber = rootProject.extra["buildNumber"] as String
val javaVersion = rootProject.extra["javaVersion"] as String

group = "org.xodium.illyriaplus.IllyriaKingdoms"
version = "$mcVersion+build.$buildNumber"
description = "Minecraft kingdoms plugin for land claiming and factions"

repositories {
    mavenCentral()
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion.build.+")

    implementation(kotlin("stdlib"))
    implementation("xyz.xenondevs.invui:invui:2.0.0")
    implementation("xyz.xenondevs.invui:invui-kotlin:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
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
    moduleName.set("IllyriaKingdoms")

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
}
