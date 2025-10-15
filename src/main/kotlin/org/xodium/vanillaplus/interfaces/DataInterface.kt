@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.interfaces

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.toSnakeCase
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.reflect.KClass

/** Represents a contract for data within the system. */
interface DataInterface<K, T : Any> {
    val dataClass: KClass<T>
    val cache: MutableMap<K, T>
    val fileName: String
        get() = "${dataClass.simpleName?.toSnakeCase()}.json"
    val jsonMapper: ObjectMapper
        get() =
            JsonMapper
                .builder()
                .addModule(KotlinModule.Builder().build())
                .addModule(JavaTimeModule())
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
                .build()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

    val filePath: Path
        get() = instance.dataFolder.toPath().resolve(fileName)

    /** Initializes the cache and loads existing data from the file. */
    fun load() {
        if (filePath.toFile().exists()) {
            try {
                cache.clear()
                val type = jsonMapper.typeFactory.constructMapType(cache::class.java, Any::class.java, dataClass.java)
                val rawMap: Map<K, T> = jsonMapper.readValue(filePath.toFile(), type)
                cache.putAll(rawMap)
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
                    filePath.writeText(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cache))
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
