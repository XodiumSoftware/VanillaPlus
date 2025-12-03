package org.xodium.vanillaplus.managers

import io.papermc.paper.command.brigadier.Commands
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.prefix
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import java.io.File
import kotlin.time.measureTime

/** Manages loading and saving the configuration file. */
internal object ConfigManager {
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    var config: ConfigData? = null
        private set

    val reloadCommand: CommandData =
        CommandData(
            Commands
                .literal("vanillaplus")
                .requires { it.sender.hasPermission(reloadPermission) }
                .then(
                    Commands
                        .literal("reload")
                        .executes { ctx ->
                            ctx.tryCatch {
                                if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                config = load()
                                it.sender.sendMessage("${instance.prefix} <green>configuration reloaded!".mm())
                            }
                        },
                ),
            "Allows to plugin specific admin commands",
            listOf("vp"),
        )

    val reloadPermission: Permission =
        Permission(
            "${instance.javaClass.simpleName}.reload".lowercase(),
            "Allows use of the reload command",
            PermissionDefault.OP,
        )

    /**
     * Loads or creates the configuration file.
     * @param fileName The name of the configuration file.
     * @return The loaded configuration data.
     */
    fun load(fileName: String = "config.json"): ConfigData {
        val file = File(instance.dataFolder, fileName)

        if (!instance.dataFolder.exists()) instance.dataFolder.mkdirs()

        val config = getOrCreateConfig(file)

        instance.logger.info(
            "${if (file.exists()) "Loaded configuration from $fileName" else "Created default $fileName"} | Took ${
                measureTime { file.writeText(json.encodeToString(ConfigData.serializer(), config)) }.inWholeMilliseconds
            }ms",
        )

        return config
    }

    /**
     * Gets the existing configuration or creates a default one.
     * @param file The configuration file.
     * @return The configuration data.
     */
    private fun getOrCreateConfig(file: File): ConfigData =
        if (file.exists()) json.decodeFromString(ConfigData.serializer(), file.readText()) else ConfigData()
}
