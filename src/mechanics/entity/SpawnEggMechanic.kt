package org.xodium.illyriaplus.mechanics.entity

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.interfaces.MechanicInterface
import kotlin.random.Random

/** Handles spawn egg drops when entities die. */
internal object SpawnEggMechanic : MechanicInterface {
    private const val SPAWN_EGG_DROP_CHANCE: Double = 0.001

    @EventHandler
    fun on(event: EntityDeathEvent) {
        if (Random.nextDouble() <= SPAWN_EGG_DROP_CHANCE) {
            Material.matchMaterial("${event.entityType.name}_SPAWN_EGG")?.let { event.drops.add(ItemStack.of(it)) }
        }
    }
}
