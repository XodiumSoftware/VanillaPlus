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
 * Represents a kingdom in the VanillaPlus plugin.
 * @property id The unique identifier of the kingdom.
 * @property name The display name of the kingdom.
 * @property ruler The UUID of the player who rules this kingdom.
 * @property creationDate The date when the kingdom was created.
 * @property members Mutable set of UUIDs representing kingdom members (excluding the ruler).
 */
data class KingdomData(
    val id: UUID,
    val name: String,
    val ruler: UUID,
    val creationDate: Date,
    val members: MutableSet<UUID> = mutableSetOf(),
) {
    /**
     * Gets the Player object of the kingdom's ruler if they are online.
     * @return The ruler as a Player object, or null if the ruler is offline.
     */
    fun getRuler(): Player? = instance.server.getPlayer(ruler)

    /**
     * Adds a player as a member of the kingdom.
     * @param player The player to add to the kingdom.
     * @return true if the player was added successfully, false if they were already a member.
     */
    fun addMember(player: Player): Boolean = members.add(player.uniqueId)

    /**
     * Checks if a player is a member of the kingdom.
     * @param player The player to check.
     * @return true if the player is the ruler or a member, false otherwise.
     */
    fun isMember(player: Player): Boolean = ruler == player.uniqueId || members.contains(player.uniqueId)

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
