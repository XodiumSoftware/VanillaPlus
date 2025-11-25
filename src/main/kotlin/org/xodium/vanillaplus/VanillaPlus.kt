@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import kotlinx.serialization.json.Json
import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.features.*
import org.xodium.vanillaplus.hooks.WorldEditHook
import org.xodium.vanillaplus.recipes.RottenFleshRecipe
import org.xodium.vanillaplus.recipes.WoodLogRecipe
import java.io.File

/** Main class of the plugin. */
internal class VanillaPlus : JavaPlugin() {
    companion object {
        lateinit var instance: VanillaPlus
            private set

        lateinit var configData: ConfigData
            private set
    }

    init {
        instance = this
    }

    /** Called when the plugin is enabled. */
    override fun onEnable() {
        val unsupportedVersionMsg =
            "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

        if (!server.version.contains(pluginMeta.version)) disablePlugin(unsupportedVersionMsg)

        config()

        RottenFleshRecipe.register()
        WoodLogRecipe.register()

        BooksFeature.register()
        CauldronFeature.register()
        ChatFeature.register()
        DimensionsFeature.register()
        EntityFeature.register()
        InvFeature.register()
        LocatorFeature.register()
        MotdFeature.register()
        OpenableFeature.register()
        PetFeature.register()
        PlayerFeature.register()
        ScoreBoardFeature.register()
        SignFeature.register()
        SitFeature.register()
        TabListFeature.register()
        if (WorldEditHook.get()) TreesFeature.register()
    }

    /**
     * Loads the configuration from the config.json file.
     * If the file does not exist or fails to load, creates a new one with default values.
     */
    private fun config() {
        val json =
            Json {
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

        val file = File(dataFolder, "config.json")
        if (!dataFolder.exists()) dataFolder.mkdirs()

        if (file.exists()) {
            try {
                val text = file.readText()
                configData = json.decodeFromString(ConfigData.serializer(), text)
                logger.info("Configuration loaded successfully.")
            } catch (e: Exception) {
                logger.warning("Failed to load config.json, using defaults and writing new file: ${e.message}")
                configData = ConfigData()
                try {
                    file.writeText(json.encodeToString(ConfigData.serializer(), configData))
                } catch (e: Exception) {
                    logger.severe("Failed to write default config.json: ${e.message}")
                }
            }
        } else {
            configData = ConfigData()
            try {
                file.writeText(json.encodeToString(ConfigData.serializer(), configData))
                logger.info("Created default config.json")
            } catch (e: Exception) {
                logger.severe("Failed to create config.json: ${e.message}")
            }
        }
    }

    /**
     * Disable the plugin and log the message.
     * @param msg The message to log.
     */
    private fun disablePlugin(msg: String) {
        logger.severe(msg)
        server.pluginManager.disablePlugin(this)
    }
}
