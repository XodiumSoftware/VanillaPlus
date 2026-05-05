package org.xodium.illyriaplus.mechanics.entity

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.xodium.illyriaplus.interfaces.MechanicInterface

/** Represents a mechanic handling mob griefing prevention within the system. */
internal object GriefingMechanic : MechanicInterface {
    private val GRIEF_CANCELLED_ENTITIES =
        setOf(
            EntityType.BLAZE,
            EntityType.CREEPER,
            EntityType.ENDER_DRAGON,
            EntityType.ENDERMAN,
            EntityType.FIREBALL,
            EntityType.SMALL_FIREBALL,
            EntityType.WITHER,
        )

    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (event.entityType in GRIEF_CANCELLED_ENTITIES) event.isCancelled = true
    }

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (event.entityType in GRIEF_CANCELLED_ENTITIES) event.blockList().clear()
    }
}
