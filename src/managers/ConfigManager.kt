package org.xodium.illyriaplus.managers

import kotlinx.serialization.json.Json
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import java.io.File

/**
 * Generic JSON configuration manager backed by kotlinx.serialization.
 *
 * Loads and saves typed data classes as pretty-printed JSON in the plugin data folder.
 * If the requested file does not exist, it is created with the provided default value.
 */
internal object ConfigManager {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    /**
     * Loads a typed configuration from the plugin data folder.
     *
     * @param fileName The name of the JSON file (relative to the data folder).
     * @param default The default value to return and persist if the file does not exist.
     * @return The deserialized [T] instance.
     */
    inline fun <reified T> load(
        fileName: String,
        default: T,
    ): T =
        File(instance.dataFolder, fileName).let {
            if (!it.exists()) default.also { save(fileName, default) } else json.decodeFromString(it.readText())
        }

    /**
     * Saves a typed configuration to the plugin data folder.
     *
     * @param fileName The name of the JSON file (relative to the data folder).
     * @param data The [T] instance to serialize and write.
     */
    inline fun <reified T> save(
        fileName: String,
        data: T,
    ) {
        File(instance.dataFolder, fileName).apply {
            parentFile?.mkdirs()
            writeText(json.encodeToString(data))
        }
    }
}
