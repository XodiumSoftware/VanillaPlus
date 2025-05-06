/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Location
import java.util.*

/**
 * Data class representing a player's interaction with a block.
 * @param playerId The unique identifier of the player.
 * @param blockLocation The location of the block.
 */
data class PlayerBlockData(private val playerId: UUID, private val blockLocation: Location)
