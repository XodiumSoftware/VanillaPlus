@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.IOException
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

data class KingdomData(
    val kingdom: String,
) {
    companion object {
        private val mapper =
            jacksonObjectMapper()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        private val filePath = instance.dataFolder.toPath().resolve("kingdoms.json")
        private val cache = mutableMapOf<UUID, KingdomData>()

        init {
            load()
        }

        /** Initializes the [KingdomData] cache and loads existing data from the file. */
        private fun load() {
            if (filePath.toFile().exists()) {
                try {
                    cache.clear()
                    cache.putAll(mapper.readValue(filePath.toFile()))
                    save()
                } catch (e: IOException) {
                    instance.logger.severe("Failed to load kingdom data: ${e.message} | ${e.stackTraceToString()}")
                }
            }
        }

        /** Saves the current state of the [KingdomData] cache to the file asynchronously. */
        private fun save() {
            instance.server.scheduler.runTaskAsynchronously(
                instance,
                Runnable {
                    try {
                        filePath.parent.createDirectories()
                        filePath.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cache))
                    } catch (e: IOException) {
                        instance.logger.severe("Failed to write ${KingdomData::class.simpleName} to file: ${e.message}")
                        e.printStackTrace()
                    }
                },
            )
        }
    }
}
