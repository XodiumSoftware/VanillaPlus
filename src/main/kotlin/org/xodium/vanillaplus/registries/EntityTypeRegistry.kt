/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.registries

import org.bukkit.entity.EntityType

/** Registry for entity types. */
object EntityTypeRegistry {
    val ARTHROPODS: Set<EntityType> = setOf(
        EntityType.BEE,
        EntityType.CAVE_SPIDER,
        EntityType.ENDERMITE,
        EntityType.SILVERFISH,
        EntityType.SPIDER,
    )

    val UNDEAD: Set<EntityType> = setOf(
        EntityType.DROWNED,
        EntityType.HUSK,
        EntityType.PHANTOM,
        EntityType.SKELETON,
        EntityType.SKELETON_HORSE,
        EntityType.STRAY,
        EntityType.WITHER,
        EntityType.WITHER_SKELETON,
        EntityType.ZOGLIN,
        EntityType.ZOMBIE,
        EntityType.ZOMBIE_HORSE,
        EntityType.ZOMBIE_VILLAGER,
        EntityType.ZOMBIFIED_PIGLIN
    )
}