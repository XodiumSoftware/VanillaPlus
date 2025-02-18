/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.modules.SkinsModule
import java.util.*

/**
 * Data class representing dimension data.
 *
 * @property guiIndex The index of the item in the GUI.
 * @property worldName The name of the world.
 * @property displayName The display name of the dimension.
 * @property itemMaterial The material of the item.
 * @property requiredBossDefeated The entity type of the boss that must be defeated to unlock the dimension.
 */
data class DimensionData(
    val guiIndex: Int,
    val worldName: String,
    val displayName: String,
    val itemMaterial: Material,
    val requiredBossDefeated: List<EntityType>? = null
) {
    companion object {
        /**
         * Checks if a player has unlocked the dimension.
         *
         * @param uuid The UUID of the player to check.
         * @param entities The entity types of the bosses that must be defeated to unlock the dimension.
         * @return `true` if the player has unlocked the skin, `false` otherwise.
         */
        fun hasUnlocked(uuid: UUID, entities: List<EntityType>?): Boolean =
            entities.isNullOrEmpty() || entities.all {
                Database.getData(SkinsModule::class, uuid.toString())
                    ?.split(",")
                    ?.contains(it.name.lowercase()) == true
            }
    }
}
