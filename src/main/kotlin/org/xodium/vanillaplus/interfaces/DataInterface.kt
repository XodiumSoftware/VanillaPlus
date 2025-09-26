@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.interfaces

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
                val map: Map<String, T> = mapper.readValue(filePath.toFile())
                map.forEach { (keyString, value) -> cache[UUID.fromString(keyString)] = value }
                save()
            } catch (e: IOException) {
                instance.logger.severe("Failed to load ${dataClass.simpleName}: ${e.message}")
            }
        }
    }

    /** Saves the current state of the cache to the file asynchronously. */
    fun save() {
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

    fun set(
        key: UUID,
        data: T,
    ) {
        cache[key] = data
        save()
    }

    fun get(key: UUID): T? = cache[key]

    fun update(
        key: UUID,
        data: T,
    ) {
        cache[key] = data
        save()
    }
}
