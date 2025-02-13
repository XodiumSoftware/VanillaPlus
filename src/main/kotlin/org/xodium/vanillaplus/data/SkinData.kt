/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.xodium.vanillaplus.Utils.format
import java.util.*

/**
 * Represents the data of a skin that can be unlocked.
 *
 * @property entityType The type of entity that the boss represents, represented by the `EntityType` enum.
 * @property material The material used to represent the boss entity's GUI item.
 * @property model The data value representing the custom skin of the item.
 * @property entityName The name of the entity.
 * @property unlockedPlayers A mutable set of UUIDs representing players who have defeated the boss entity.
 */
data class SkinData(
    val entityType: EntityType,
    val material: Material,
    val model: String = entityType.name,
    val entityName: String = entityType.format(),
    val unlockedPlayers: MutableSet<UUID> = mutableSetOf()
)