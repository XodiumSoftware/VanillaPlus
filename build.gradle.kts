object PluginConfig {
    const val AUTHOR = "Xodium"
    const val GROUP = "org.xodium.vanillaplus"
    const val VERSION = "1.21.8"
    const val DESCRIPTION = "Minecraft plugin that enhances the base gameplay."
}

plugins {
    id("java")
    id("idea")
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0"
}

val pluginVersion = project.findProperty("buildVersion")?.toString() ?: PluginConfig.VERSION
val apiVersion = Regex("""^(\d+\.\d+\.\d+)""").find(pluginVersion)?.groupValues?.get(1) ?: PluginConfig.VERSION

group = PluginConfig.GROUP
version = pluginVersion
description = PluginConfig.DESCRIPTION

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
    val props =
        mapOf(
            "version" to pluginVersion,
            "description" to PluginConfig.DESCRIPTION,
            "author" to PluginConfig.AUTHOR,
        )
    processResources { filesMatching("paper-plugin.yml") { expand(props) } }
    shadowJar {
        dependsOn(processResources)
        archiveClassifier.set("")
        destinationDirectory.set(layout.projectDirectory.dir("libs"))
        relocate("com.fasterxml.jackson", "${PluginConfig.GROUP}.jackson")
        minimize { exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*")) }
    }
    register<Copy>("copyJar") {
        dependsOn(shadowJar)
        from(shadowJar)
        into(layout.projectDirectory.dir(".server/plugins/update"))
    }
    jar { enabled = false }
    withType<JavaCompile> { options.encoding = "UTF-8" }
    register("printVersion") { doLast { println(project.version) } }
    register("acceptEula") { doLast { file(".server/eula.txt").writeText("eula=true\n") } }
    register<Exec>("runDevServer") {
        dependsOn("copyJar", "acceptEula")
        workingDir = file(".server/").apply { mkdirs() }
        standardInput = System.`in`
        commandLine =
            listOf(
                project.extensions
                    .getByType(JavaToolchainService::class.java)
                    .launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) }
                    .get()
                    .executablePath.asFile.absolutePath,
                "-XX:+AllowEnhancedClassRedefinition",
                "-jar",
                "server.jar",
                "nogui",
            )
    }
}

idea { module { excludeDirs.add(file(".server")) } }
