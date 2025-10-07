package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

/** Represents the config manager within the system. */
internal object ConfigManager {
    val configPath: Path = instance.dataFolder.toPath().resolve("config.json")
    internal val jsonMapper =
        JsonMapper
            .builder()
            .addModule(KotlinModule.Builder().build())
            .addModule(JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .build()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

    /**
     * Loads settings from the config file.
     * @return A JsonNode representing the configuration, or null on failure or if the file doesn't exist.
     */
    fun load(): JsonNode? {
        if (!configPath.exists()) return null
        return try {
            jsonMapper.readTree(configPath.inputStream())
        } catch (e: IOException) {
            instance.logger.severe("Config: Failed to load config file: ${e.message} | ${e.stackTraceToString()}")
            null
        }
    }

    /**
     * Saves the current settings to the config file.
     * @param data The map of configuration data to save.
     */
    fun save(data: Map<String, ModuleInterface.Config>) {
        try {
            configPath.parent.createDirectories()
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(configPath.outputStream(), data)
        } catch (e: IOException) {
            instance.logger.severe("Config: Failed to save config file: ${e.message} | ${e.stackTraceToString()}")
        }
    }
}
