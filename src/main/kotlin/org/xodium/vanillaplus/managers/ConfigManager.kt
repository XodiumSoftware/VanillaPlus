package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import java.io.IOException
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/** Represents the config manager within the system. */
object ConfigManager {
    private val configPath = instance.dataFolder.toPath().resolve("config.json")
    internal val objectMapper = jacksonObjectMapper()
        .registerModules(JavaTimeModule())
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

    /**
     * Creates and returns the command data for the "vanillaplus" command.
     * @return CommandData object containing the registered command, description, and aliases.
     */
    fun cmds(): CommandData {
        return CommandData(
            listOf(
                Commands.literal("vanillaplus")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { ModuleManager.reload() } }
            ),
            "Allows players to reload VanillaPlus.",
            listOf("vp")
        )
    }

    /**
     * Retrieves a list of permissions associated with a module.
     * @return A list containing one or more [Permission] objects that represent
     * the access rights required for command execution.
     */
    fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.config.reload".lowercase(),
                "Allows use of the config reload command",
                PermissionDefault.OP
            )
        )
    }

    /**
     * Loads settings from the config file.
     * @param silent If true, suppresses logging messages during loading.
     * @return A JsonNode representing the configuration, or null on failure or if the file doesn't exist.
     */
    fun load(silent: Boolean = false): JsonNode? {
        try {
            if (configPath.toFile().exists()) {
                if (!silent) instance.logger.info("Config: Loading settings.")
                val node = objectMapper.readTree(configPath.toFile().readText())
                if (!silent) instance.logger.info("Config: Settings loaded successfully.")
                return node
            } else {
                if (!silent) instance.logger.info("Config: No config file found, creating new one.")
                return null
            }
        } catch (e: IOException) {
            instance.logger.severe("Config: Failed to load config file: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    /**
     * Saves the current settings to the config file.
     * @param data The map of configuration data to save.
     */
    fun save(data: Map<String, ModuleInterface.Config>) {
        try {
            instance.logger.info("Config: Saving settings.")
            configPath.parent.createDirectories()
            configPath.writeText(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data))
            instance.logger.info("Config: Settings saved successfully.")
        } catch (e: IOException) {
            instance.logger.severe("Config: Failed to save config file: ${e.message}")
            e.printStackTrace()
        }
    }
}