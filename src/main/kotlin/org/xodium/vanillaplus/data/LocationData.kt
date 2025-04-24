/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location

/**
 * Represents the data of a specific location in a Minecraft world.
 *
 * This class encapsulates the world name, coordinates (x, y, z), and optionally
 * the orientation (yaw and pitch) of a location. It provides methods to convert
 * between raw data and the `Location` object commonly used in the Bukkit API.
 *
 * @property world The name of the world where the location exists.
 * @property x The x-coordinate of the location.
 * @property y The y-coordinate of the location.
 * @property z The z-coordinate of the location.
 * @property yaw The yaw (horizontal rotation) of the location, optional.
 * @property pitch The pitch (vertical rotation) of the location, optional.
 */
@Serializable
data class LocationData(
    private val world: String,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val yaw: Float? = null,
    private val pitch: Float? = null
) {
    /**
     * Converts the current `LocationData` instance into a `Location` object.
     *
     * @return A `Location` object representing the world and coordinates stored in this `LocationData`.
     *         Returns `null` if the world does not exist or cannot be resolved.
     */
    fun toLocation(): Location {
        val world = Bukkit.getWorld(this@LocationData.world)
        if (yaw != null && pitch != null) return Location(world, x, y, z, yaw, pitch)
        return Location(world, x, y, z)
    }

    companion object {
        /**
         * Converts a `Location` object into a `LocationData` representation.
         *
         * @param location The `Location` object to convert. This includes the world and the x, y, z coordinates.
         * @return A `LocationData` object that represents the given `Location` with its world name and coordinates.
         */
        fun fromLocation(location: Location): LocationData {
            return LocationData(location.world.name, location.x, location.y, location.z)
        }
    }
}
