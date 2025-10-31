@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.interfaces

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.toSnakeCase
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/** Represents a contract for data within the system. */
interface DataInterface<K, T : Any> {
    val serializer: KSerializer<T>
    val keySerializer: KSerializer<K>
    val dataClassName: String
    val cache: MutableMap<K, T>
    val fileName: String
        get() = "${dataClassName.toSnakeCase()}.json"
    val filePath: Path
        get() = instance.dataFolder.toPath().resolve(fileName)
    val json: Json
        get() =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                classDiscriminator = "type"
            }

    /** Initializes the cache and loads existing data from the file. */
    fun load() {
        if (filePath.toFile().exists()) {
            try {
                cache.clear()
                val mapSerializer = MapSerializer(keySerializer, serializer)
                val rawMap: Map<K, T> = json.decodeFromString(mapSerializer, filePath.readText())
                cache.putAll(rawMap)
                save()
            } catch (e: IOException) {
                instance.logger.severe("Failed to load $dataClassName: ${e.message}")
            } catch (e: Exception) {
                instance.logger.severe("Failed to deserialize $dataClassName: ${e.message}")
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
                    val mapSerializer = MapSerializer(keySerializer, serializer)
                    filePath.writeText(json.encodeToString(mapSerializer, cache))
                } catch (e: IOException) {
                    instance.logger.severe("Failed to write $dataClassName to file: ${e.message}")
                } catch (e: Exception) {
                    instance.logger.severe("Failed to serialize $dataClassName: ${e.message}")
                }
            },
        )
    }

    /**
     * Returns the value associated with the specified key, or `null` if no value is associated.
     * @param key the UUID key whose associated value is to be returned.
     * @return the value associated with the specified key, or `null` if no mapping exists.
     */
    fun get(key: K): T? {
        if (cache.isEmpty() && filePath.toFile().exists()) load()
        return cache[key]
    }

    /**
     * Associates the specified value with the specified key in the cache and saves the changes.
     * @param key the UUID key with which the specified value is to be associated.
     * @param data the value to be associated with the specified key.
     */
    fun set(
        key: K,
        data: T,
    ) {
        cache[key] = data
        save()
    }
}
