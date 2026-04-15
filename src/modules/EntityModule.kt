@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
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
import org.xodium.vanillaplus.enchantments.NimbusEnchantment
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
    fun on(event: EntityDeathEvent) {
        if (Random.nextDouble() <= Config.entityEggDropChance) {
            Material.matchMaterial("${event.entity.type.name}_SPAWN_EGG")?.let { event.drops.add(ItemStack.of(it)) }
        }
    }

    @EventHandler
    fun on(event: EntityEquipmentChangedEvent) = NimbusEnchantment.onEntityEquipmentChanged(event)

    /**
     * Determines whether an entity's griefing behaviour should be cancelled.
     * @param entity The entity whose griefing behaviour is being evaluated.
     * @return `true` if the entity's type is in [Config.griefCancelTypes]; `false` otherwise.
     */
    private fun shouldCancelGrief(entity: Entity): Boolean = Config.griefCancelTypes.any { it.isInstance(entity) }

    /** Represents the config of the module. */
    object Config {
        val griefCancelTypes =
            setOf(Blaze::class, Creeper::class, EnderDragon::class, Enderman::class, Fireball::class, Wither::class)
        var entityEggDropChance: Double = 0.001
    }
}
