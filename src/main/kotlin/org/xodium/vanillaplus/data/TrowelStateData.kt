/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * Represents the state data for the Trowel module, containing a list of UUIDs.
 * This class provides methods to load and save the state data from/to a JSON file.
 * @property uuids List of UUIDs representing the state of the Trowel module. Defaults to an empty list.
 */
data class TrowelStateData(
    private val uuids: List<UUID> = emptyList()
) {
    companion object {
        private val mapper = jacksonObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        private val filePath: Path = instance.dataFolder.toPath().resolve("trowel.json")
        private val cache = mutableSetOf<UUID>()

        init {
            load()
        }

        /**
         * Reads the TrowelStateData from the JSON file.
         * @return TrowelStateData read from the file or an empty instance if the file does not exist.
         */
        private fun readState(): TrowelStateData {
            return if (Files.exists(filePath)) {
                mapper.readValue(Files.readString(filePath), TrowelStateData::class.java)
            } else {
                TrowelStateData()
            }
        }

        /**
         * Loads the TrowelStateData from the JSON file.
         * @return TrowelStateData loaded from the file or an empty instance if the file does not exist.
         */
        private fun load(): TrowelStateData {
            val state = readState()
            cache.clear()
            cache.addAll(state.uuids)
            return state
        }

        /**
         * Writes the TrowelStateData to the JSON file in ASync.
         * @param state The TrowelStateData to write to the file.
         */
        private fun write(state: TrowelStateData) {
            instance.server.scheduler.runTaskAsynchronously(instance, Runnable {
                try {
                    Files.createDirectories(filePath.parent)
                    Files.writeString(filePath, mapper.writeValueAsString(state))
                } catch (e: IOException) {
                    instance.logger.severe("Failed to write TrowelStateData to file: ${e.message}")
                    e.printStackTrace()
                }
            })

        }

        /**
         * Checks if a player is currently active in the Trowel module.
         * @param player The player to check.
         * @return True if the player is active, false otherwise.
         */
        fun isActive(player: Player): Boolean = cache.contains(player.uniqueId)

        /**
         * Toggles the Trowel state for a player.
         * @param player The player whose Trowel state is to be toggled.
         * @return True if the player was added (enabled), false if they were removed (disabled).
         */
        fun toggle(player: Player): Boolean {
            val enabled = cache.add(player.uniqueId)
            if (!enabled) cache.remove(player.uniqueId)
            write(TrowelStateData(cache.toList()))
            return enabled
        }
    }
}
