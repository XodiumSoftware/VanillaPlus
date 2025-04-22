/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.xodium.vanillaplus.Config

/**
 * Represents data for a waystone in a Minecraft world.
 *
 * A waystone is a location players can use for fast travel or teleportation,
 * and this data class encapsulates its physical location and display properties.
 *
 * @property location The physical location of the waystone in the Minecraft world.
 *                    Encapsulates coordinates and dimension data.
 * @property displayName The visual name of the waystone, shown to players.
 *                       Supports rich text, allowing formatting and colors.
 */
data class WaystoneData(
    val location: Location,
    val displayName: Component
) {
    companion object {
        /**
         * Calculates the experience (XP) cost for traveling or teleporting between two locations.
         * @param origin The starting location from which the travel begins. Must include world and coordinates.
         * @param destination The target location to which the travel ends. Must include world and coordinates.
         * @return The calculated XP cost for traveling between the given origin and destination.
         */
        fun calculateXpCost(origin: Location, destination: Location): Int {
            return Config.WaystoneModule.BASE_XP_COST + when (origin.world) {
                destination.world -> (origin.distance(destination) * Config.WaystoneModule.DISTANCE_MULTIPLIER).toInt()
                else -> Config.WaystoneModule.DIMENSIONAL_MULTIPLIER
            }
        }

        /**
         * Converts a given `Location` object into a unique string key representation.
         * This key is composed of the world name and the block coordinates of the location.
         * @param location The `Location` object to convert into a string key. This location
         * must include the world, blockX, blockY, and blockZ data.
         * @return A string key in the format "waystone:<world_name>:<blockX>:<blockY>:<blockZ>".
         */
        fun serialize(location: Location): String {
            return "waystone:${location.world.name}:${location.blockX}:${location.blockY}:${location.blockZ}"
        }

        /**
         * Converts a string key representation of a location into a `Location` object.
         * The key is expected to be in the format "waystone:<world_name>:<x>:<y>:<z>".
         * If the key is invalid or the world specified cannot be found, this method returns null.
         *
         * @param key The string key representing the location. It must follow the expected format with valid values.
         * @return A `Location` object corresponding to the key, or null if the key is invalid or the world is not found.
         */
        fun deserialize(key: String): Location? {
            val parts = key.split(":")
            if (parts.size != 5) return null
            val world = Bukkit.getWorld(parts[1]) ?: return null
            val x = parts[2].toIntOrNull() ?: return null
            val y = parts[3].toIntOrNull() ?: return null
            val z = parts[4].toIntOrNull() ?: return null
            return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
        }
    }
}
