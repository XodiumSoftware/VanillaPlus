package org.xodium.illyriaplus.data

import org.bukkit.Location
import org.bukkit.World

/**
 * Represents the data structure for a teleport destination.
 *
 * @property world The world this teleport destination resides in.
 * @property location The specific location within the world to teleport to.
 * @property name The display name of this teleport anchor.
 */
internal data class TeleportAnchorData(
    val world: World,
    val location: Location,
    val name: String = "",
)
