plugins {
    id("java")
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "org.xodium.vanillaplus"
version = "1.0.0"
description = "Minecraft plugin that enhances the base gameplay."

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.9")

    implementation("net.kyori:adventure-api:4.18.0")
    implementation(kotlin("stdlib-jdk8"))
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

tasks {
    processResources {
        filesMatching("paper-plugin.yml") {
            expand(
                "version" to project.version,
                "description" to project.description,
            )
        }
    }
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        relocate("kotlin", "org.xodium.vanillaplus.kotlin")
        destinationDirectory.set(file(".server/plugins"))
        minimize()
    }
    jar { enabled = false }
    withType<JavaCompile> { options.encoding = "UTF-8" }
    register("printVersion") { doLast { println(project.version) } }
}