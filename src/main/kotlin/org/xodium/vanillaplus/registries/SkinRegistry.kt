/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.registries

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.xodium.vanillaplus.data.SkinData

object SkinRegistry {
    val skins: List<SkinData> = listOf(
        SkinData(EntityType.WITHER, Material.WITHER_SPAWN_EGG),
        SkinData(EntityType.ELDER_GUARDIAN, Material.ELDER_GUARDIAN_SPAWN_EGG),
        SkinData(EntityType.WARDEN, Material.WARDEN_SPAWN_EGG),
        SkinData(EntityType.ENDER_DRAGON, Material.ENDER_DRAGON_SPAWN_EGG)
    )

    fun getByEntityType(type: EntityType): SkinData? = skins.find { it.entityType == type }

    fun getByModel(model: String): SkinData? = skins.find { it.model == model }
}