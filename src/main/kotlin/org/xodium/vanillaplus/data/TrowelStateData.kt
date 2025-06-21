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
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * Represents the state data for the Trowel module, containing a list of UUIDs.
 * This class provides methods to load and save the state data from/to a JSON file.
 * @property uuids List of UUIDs representing the state of the Trowel module. Defaults to an empty list.
 */
data class TrowelStateData(
    val uuids: List<UUID> = emptyList()
) {
    companion object {
        private val mapper = jacksonObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        private val filePath = instance.dataFolder.toPath().resolve("trowel.json")
        private val cache = mutableSetOf<UUID>()

        init {
            load()
        }

        private fun load() {
            if (filePath.toFile().exists()) {
                cache.clear()
                cache.addAll(mapper.readValue(filePath.toFile(), TrowelStateData::class.java).uuids)
            }
        }

        private fun save() {
            instance.server.scheduler.runTaskAsynchronously(instance, Runnable {
                try {
                    filePath.parent.createDirectories()
                    filePath.writeText(mapper.writeValueAsString(TrowelStateData(cache.toList())))
                } catch (e: IOException) {
                    instance.logger.severe("Failed to write ${TrowelStateData::class.simpleName} to file: ${e.message}")
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
            save()
            return enabled
        }
    }
}
