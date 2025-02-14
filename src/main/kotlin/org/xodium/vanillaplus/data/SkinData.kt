/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.Utils.format
import org.xodium.vanillaplus.modules.SkinsModule
import java.util.*

/**
 * Represents the data of a skin that can be unlocked.
 *
 * @property entityType The type of entity that the boss represents, represented by the `EntityType` enum.
 * @property material The material used to represent the boss entity's GUI item.
 * @property model The data value representing the custom skin of the item.
 * @property entityName The name of the entity.
 */
data class SkinData(
    val entityType: EntityType,
    val material: Material,
    val model: String = entityType.name.lowercase(),
    val entityName: String = entityType.format()
) {
    companion object {
        /**
         * Gets a skin data by the entity type.
         *
         * @param type The entity type to get the skin data for.
         * @return The skin data for the entity type, or `null` if it does not exist.
         */
        fun List<SkinData>.getByEntityType(type: EntityType): SkinData? = find { it.entityType == type }

        /**
         * Gets a skin data by the model.
         *
         * @param model The model to get the skin data for.
         * @return The skin data for the model, or `null` if it does not exist.
         */
        fun List<SkinData>.getByModel(model: String): SkinData? = find { it.model == model }

        /**
         * Checks if a player has unlocked a skin.
         *
         * @param uuid The UUID of the player to check.
         * @param skinData The skin data to check against.
         * @return `true` if the player has unlocked the skin, `false` otherwise.
         */
        fun hasUnlocked(uuid: UUID, skinData: SkinData): Boolean =
            Database.getData(
                SkinsModule::class,
                uuid.toString()
            )?.split(",")?.contains(skinData.model) == true

        /**
         * Unlocks a skin for a player.
         *
         * @param uuid The UUID of the player to unlock the skin for.
         * @param skinData The skin data to unlock.
         */
        fun setUnlocked(uuid: UUID, skinData: SkinData) =
            Database.setData(
                SkinsModule::class,
                uuid.toString(),
                (Database.getData(SkinsModule::class, uuid.toString())?.split(",")?.toMutableSet()
                    ?: mutableSetOf()).apply { add(skinData.model) }.joinToString(",")
            )
    }
}