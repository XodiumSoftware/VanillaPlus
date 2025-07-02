/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
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
 * @param nickname The nickname of the player, if set.
 * @param quests A list of quests associated with the player.
 */
data class PlayerData(
    val nickname: String? = null,
    val quests: List<QuestData> = emptyList(),
) {
    companion object {
        private val mapper = jacksonObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        private val filePath = instance.dataFolder.toPath().resolve("playerdata.json")
        private val cache = mutableMapOf<UUID, PlayerData>()

        init {
            load()
        }

        /** Initializes the PlayerData cache and loads existing data from the file. */
        private fun load() {
            if (filePath.toFile().exists()) {
                try {
                    cache.clear()
                    cache.putAll(mapper.readValue(filePath.toFile()))
                } catch (e: IOException) {
                    instance.logger.severe("Failed to load player data: ${e.message}")
                }
            }
        }

        /** Saves the current state of the PlayerData cache to the file asynchronously. */
        private fun save() {
            instance.server.scheduler.runTaskAsynchronously(instance, Runnable {
                try {
                    filePath.parent.createDirectories()
                    filePath.writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cache))
                } catch (e: IOException) {
                    instance.logger.severe("Failed to write ${PlayerData::class.simpleName} to file: ${e.message}")
                    e.printStackTrace()
                }
            })
        }

        /**
         * Sets the player data for a specific player.
         * @param player The player whose data is to be set.
         */
        fun set(player: Player) {
            cache.getOrPut(player.uniqueId) { PlayerData() }
            save()
        }

        /**
         * Retrieves the player data for a specific player.
         * @param player The player whose data is to be retrieved.
         * @return The PlayerData associated with the player.
         */
        fun get(player: Player): PlayerData {
            return cache.getOrPut(player.uniqueId) { PlayerData() }
        }

        /**
         * Updates the player data for a specific player.
         * @param player The player whose data is to be updated.
         * @param data The new PlayerData to set for the player.
         */
        fun update(player: Player, data: PlayerData) {
            cache[player.uniqueId] = data
            save()
        }

        /**
         * Retrieves the nickname for a given player.
         * @param player The player whose nickname is to be retrieved.
         * @return The nickname if it exists, null otherwise.
         */
        fun getNickname(player: Player): String? = get(player).nickname

        /**
         * Sets the nickname for a given player.
         * @param player The player whose nickname is to be set.
         * @param nickname The nickname to set.
         */
        fun setNickname(player: Player, nickname: String) {
            val data = get(player)
            update(player, data.copy(nickname = nickname))
        }

        /**
         * Removes the nickname for a given player.
         * @param player The player whose nickname is to be removed.
         */
        fun removeNickname(player: Player) {
            val data = get(player)
            if (data.nickname != null) {
                update(player, data.copy(nickname = null))
            }
        }
    }
}
