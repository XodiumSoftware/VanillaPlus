import xyz.jpenilla.runtask.task.AbstractRun

plugins {
    id("java")
    id("idea")
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("com.gradleup.shadow") version "9.3.2"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
}

val mcVersion = "1.21.11"
val buildNumber =
    providers
        .exec { commandLine("git", "rev-list", "--count", "HEAD") }
        .standardOutput.asText
        .map { it.trim() }

group = "org.xodium.vanillaplus.VanillaPlus"
version = "$mcVersion+build.${buildNumber.get()}"
description = "Minecraft plugin that enhances the base gameplay"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion-R0.1-SNAPSHOT")

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
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
        minimize { exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*")) }
    }
    jar { enabled = false }
    register<Exec>("deploy") {
        dependsOn(shadowJar)
        doFirst {
            commandLine(
                "scp",
                "-P",
                "2222",
                shadowJar
                    .get()
                    .archiveFile
                    .get()
                    .asFile.absolutePath,
                "root@sftp.xodium.org:/var/lib/lxc/100/rootfs/opt/docker/data/plugins/update/",
            )
        }
    }
    runServer { minecraftVersion(mcVersion) }
    withType<JavaCompile> { options.encoding = "UTF-8" }
    withType(AbstractRun::class) { jvmArgs("-XX:+AllowEnhancedClassRedefinition") }
}

paperPluginYaml {
    main.set(group.toString())
    website.set("https://github.com/XodiumSoftware/VanillaPlus")
    authors.add("Xodium")
    apiVersion.set(mcVersion)
    bootstrapper.set("org.xodium.vanillaplus.VanillaPlusBootstrap")
}
