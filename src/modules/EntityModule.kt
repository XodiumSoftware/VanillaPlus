@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.entity.Blaze
import org.bukkit.entity.Creeper
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Enderman
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.Wither
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.random.Random

/** Represents a module handling entity mechanics within the system. */
internal object EntityModule : ModuleInterface {
    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (shouldCancelGrief(event.entity)) event.isCancelled = true
    }

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (shouldCancelGrief(event.entity)) event.blockList().clear()
    }

    @EventHandler
    fun on(event: EntityDeathEvent) = dropEntitySpawnEgg(event)

    /**
     * Handles dropping a spawn egg when an entity dies, based on configured chance.
     * @param event The death event of the entity.
     */
    private fun dropEntitySpawnEgg(event: EntityDeathEvent) {
        if (Random.nextDouble() <= Config.ENTITY_EGG_DROP_CHANCE) {
            Material.matchMaterial("${event.entity.type.name}_SPAWN_EGG")?.let { event.drops.add(ItemStack.of(it)) }
        }
    }

    /**
     * Determines whether an entity's griefing behaviour should be cancelled.
     * @param entity The entity whose griefing behaviour is being evaluated.
     * @return `true` if the entity's type is in [Config.GRIEF_CANCEL_TYPES]; `false` otherwise.
     */
    private fun shouldCancelGrief(entity: Entity): Boolean = Config.GRIEF_CANCEL_TYPES.any { it.isInstance(entity) }

    /** Represents the config of the module. */
    object Config {
        val GRIEF_CANCEL_TYPES =
            setOf(Blaze::class, Creeper::class, EnderDragon::class, Enderman::class, Fireball::class, Wither::class)
        const val ENTITY_EGG_DROP_CHANCE: Double = 0.001
    }
}
