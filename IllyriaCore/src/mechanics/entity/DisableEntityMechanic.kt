package org.xodium.illyriaplus.mechanics.entity

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.illyriaplus.interfaces.MechanicInterface

/** Disables natural spawning of specific entity types. */
internal object DisableEntityMechanic : MechanicInterface {
    private val DISABLED_ENTITIES = setOf(EntityType.PHANTOM)

    @EventHandler
    fun on(event: CreatureSpawnEvent) {
        if (event.entityType in DISABLED_ENTITIES) event.isCancelled = true
    }
}
