/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import net.kyori.adventure.text.Component
import org.bukkit.Location

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
    /**
     * Calculates the XP cost to teleport to this waystone from a given origin location
     *
     * @param origin The location from which the player would teleport
     * @param calculator The function used to calculate teleportation cost
     * @return The XP cost as an integer
     */
    fun calcCost(
        origin: Location, calculator: (Location, Location) -> Int
    ): Int {
        return calculator(origin, location)
    }
}
