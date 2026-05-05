package org.xodium.illyriaplus.data

import org.bukkit.Location
import org.bukkit.World

/**
 * Represents the data structure for a teleport destination.
 *
 * @property world The world this teleport destination resides in.
 * @property location The specific location within the world to teleport to.
 */
internal data class TeleportAnchorData(
    val world: World,
    val location: Location,
)
