@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.interfaces

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.IOException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.reflect.KClass

/** Represents a contract for data within the system. */
interface DataInterface<T : Any> {
    val dataClass: KClass<T>
    val fileName: String
    val cache: MutableMap<UUID, T>

    val mapper: ObjectMapper
        get() =
            jacksonObjectMapper()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

    val filePath: Path
        get() = instance.dataFolder.toPath().resolve(fileName)

    /** Initializes the cache and loads existing data from the file. */
    fun load() {
        if (filePath.toFile().exists()) {
            try {
                cache.clear()
                @Suppress("UNCHECKED_CAST")
                val rawMap: Map<String, Any> = mapper.readValue(filePath.toFile(), Map::class.java) as Map<String, Any>
                rawMap.forEach { (keyString, value) ->
                    val convertedValue =
                        when (value) {
                            is Map<*, *> -> mapper.convertValue(value, dataClass.java)
                            else -> value
                        }
                    @Suppress("UNCHECKED_CAST")
                    cache[UUID.fromString(keyString)] = convertedValue as T
                }
                save()
            } catch (e: IOException) {
                instance.logger.severe("Failed to load ${dataClass.simpleName}: ${e.message}")
            }
        }
    }

    /** Saves the current state of the cache to the file asynchronously. */
    private fun save() {
        instance.server.scheduler.runTaskAsynchronously(
            instance,
            Runnable {
                try {
                    filePath.parent.createDirectories()
                    val stringKeyMap = cache.mapKeys { it.key.toString() }
                    filePath.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stringKeyMap))
                } catch (e: IOException) {
                    instance.logger.severe("Failed to write ${dataClass.simpleName} to file: ${e.message}")
                }
            },
        )
    }

    /**
     * Returns the value associated with the specified key, or `null` if no value is associated.
     * @param key the UUID key whose associated value is to be returned.
     * @return the value associated with the specified key, or `null` if no mapping exists.
     */
    fun get(key: UUID): T? {
        if (!cache.containsKey(key) && filePath.toFile().exists()) load()
        return cache[key]
    }

    /**
     * Associates the specified value with the specified key in the cache and saves the changes.
     * @param key the UUID key with which the specified value is to be associated.
     * @param data the value to be associated with the specified key.
     */
    fun set(
        key: UUID,
        data: T,
    ) {
        cache[key] = data
        save()
    }
}
