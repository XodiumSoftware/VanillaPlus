package org.xodium.illyriaplus.managers

import kotlinx.serialization.json.Json
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.Utils.prefix
import java.io.File

/** Manages JSON configuration files using kotlinx.serialization. */
internal class ConfigManager {
    private val json =
        Json {
            prettyPrint = true
            prettyPrintIndent = "  "
            ignoreUnknownKeys = true
            isLenient = true
        }

    init {
        if (!instance.dataFolder.exists()) instance.dataFolder.mkdirs()
    }

    /**
     * Loads a config file or creates it with default values if it doesn't exist.
     *
     * @param name The config file name (without .json extension)
     * @param default The default configuration to use if file doesn't exist
     * @return The loaded or default config
     */
    inline fun <reified T> load(
        name: String,
        default: T,
    ): T {
        val file = File(instance.dataFolder, "$name.json")

        if (!file.exists()) {
            save(name, default)
            return default
        }

        return runCatching {
            json.decodeFromString<T>(file.readText())
        }.getOrElse { e ->
            instance.logger.warning(
                "${instance.prefix} Failed to load config $name.json, using defaults. Error: ${e.message}",
            )
            default
        }
    }

    /**
     * Saves a config to a JSON file.
     *
     * @param name The config file name (without .json extension)
     * @param config The configuration object to save
     */
    inline fun <reified T> save(
        name: String,
        config: T,
    ) {
        runCatching {
            File(instance.dataFolder, "$name.json").writeText(json.encodeToString(config))
        }.onFailure { e ->
            instance.logger.warning("${instance.prefix} Failed to save config $name.json. Error: ${e.message}")
        }
    }
}
