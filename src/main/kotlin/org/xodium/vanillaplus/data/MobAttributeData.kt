/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType

/**
 * Data class to hold information about mob attributes.
 * @property types A list of [EntityType]s that this mob attribute data applies to.
 * @property attributes A map of [Attribute]s and their corresponding adjustment functions,
 *                      'it' is the original value of the attribute.
 * @property spawnRate The spawn rate of the mob.
 */
data class MobAttributeData(
    val types: List<EntityType>,
    val attributes: Map<Attribute, (Double) -> Double>,
    val spawnRate: Double
)