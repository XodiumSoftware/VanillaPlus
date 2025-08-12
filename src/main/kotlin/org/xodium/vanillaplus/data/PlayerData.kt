@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.IOException
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * Represents the data structure for player data.
 * @param nickname The [nickname] of the player, if set.
 */
internal data class PlayerData(
    val nickname: String? = null,
    val signTutorial: Boolean = false,
) {
    companion object {
        private val mapper =
            jacksonObjectMapper()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        private val filePath = instance.dataFolder.toPath().resolve("playerdata.json")
        private val cache = mutableMapOf<UUID, PlayerData>()

        init {
            load()
        }

        /** Initializes the [PlayerData] cache and loads existing data from the file. */
        private fun load() {
            if (filePath.toFile().exists()) {
                try {
                    cache.clear()
                    cache.putAll(mapper.readValue(filePath.toFile()))
                    save()
                } catch (e: IOException) {
                    instance.logger.severe("Failed to load player data: ${e.message} | ${e.stackTraceToString()}")
                }
            }
        }

        /** Saves the current state of the [PlayerData] cache to the file asynchronously. */
        private fun save() {
            instance.server.scheduler.runTaskAsynchronously(
                instance,
                Runnable {
                    try {
                        filePath.parent.createDirectories()
                        filePath.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cache))
                    } catch (e: IOException) {
                        instance.logger.severe("Failed to write ${PlayerData::class.simpleName} to file: ${e.message}")
                        e.printStackTrace()
                    }
                },
            )
        }

        /**
         * Sets the [player] data for a specific [player].
         * @param player The [player] whose data is to be set.
         */
        fun set(player: Player) {
            cache.getOrPut(player.uniqueId) { PlayerData() }
            save()
        }

        /**
         * Retrieves the [player] data for a specific [player].
         * @param player The [player] whose data is to be retrieved.
         * @return The [PlayerData] associated with the [player].
         */
        fun get(player: Player): PlayerData = cache.getOrPut(player.uniqueId) { PlayerData() }

        /**
         * Updates the [player] data for a specific [player].
         * @param player The [player] whose data is to be updated.
         * @param data The new [PlayerData] to set for the [player].
         */
        fun update(
            player: Player,
            data: PlayerData,
        ) {
            cache[player.uniqueId] = data
            save()
        }
    }
}
