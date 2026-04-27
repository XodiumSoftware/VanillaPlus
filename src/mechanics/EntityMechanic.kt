@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.mechanics

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Bat
import org.bukkit.entity.Blaze
import org.bukkit.entity.Camel
import org.bukkit.entity.Creeper
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Enderman
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fireball
import org.bukkit.entity.Husk
import org.bukkit.entity.Wither
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.random.Random

/** Represents a module handling entity mechanics within the system. */
internal object EntityMechanic : ModuleInterface {
    @EventHandler
    fun on(event: CreatureSpawnEvent) {
        if (event.entityType == EntityType.PHANTOM) event.isCancelled = true
    }

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
        dropEntitySpawnEgg(event)
        dropBatMembrane(event)
        dropHuskSand(event)
    }

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
     * Handles dropping a phantom membrane when a bat is killed by a player.
     * Mimics vanilla phantom drop behavior: 0-1 base, +0-1 per Looting level.
     * @param event The death event of the entity.
     */
    private fun dropBatMembrane(event: EntityDeathEvent) {
        if (event.entity !is Bat) return

        val killer = event.entity.killer ?: return

        if (Random.nextDouble() > Config.BAT_MEMBRANE_DROP_CHANCE) return

        val lootingLevel = killer.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LOOTING)
        val minAmount = Config.BAT_MEMBRANE_BASE_MIN
        val maxAmount = Config.BAT_MEMBRANE_BASE_MAX + (lootingLevel * Config.BAT_MEMBRANE_LOOTING_BONUS)
        val amount = Random.nextInt(minAmount, maxAmount + 1)

        if (amount > 0) event.drops.add(ItemStack.of(Material.PHANTOM_MEMBRANE, amount))
    }

    /**
     * Handles dropping sand when a husk dies.
     * Regular husks: 0-2 sand, Camel husks: 0-3 sand.
     * Looting adds extra sand per level.
     * @param event The death event of the entity.
     */
    private fun dropHuskSand(event: EntityDeathEvent) {
        if (event.entity !is Husk) return

        if (Random.nextDouble() > Config.HUSK_SAND_DROP_CHANCE) return

        val isCamelHusk = event.entity.vehicle is Camel
        val lootingLevel =
            event.entity.killer
                ?.inventory
                ?.itemInMainHand
                ?.getEnchantmentLevel(Enchantment.LOOTING) ?: 0
        val minAmount = Config.HUSK_SAND_BASE_MIN
        val maxAmount =
            if (isCamelHusk) {
                Config.CAMEL_HUSK_SAND_BASE_MAX + (lootingLevel * Config.CAMEL_HUSK_SAND_LOOTING_BONUS)
            } else {
                Config.HUSK_SAND_BASE_MAX + (lootingLevel * Config.HUSK_SAND_LOOTING_BONUS)
            }
        val amount = Random.nextInt(minAmount, maxAmount + 1)

        if (amount > 0) event.drops.add(ItemStack.of(Material.SAND, amount))
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
        const val BAT_MEMBRANE_DROP_CHANCE: Double = 1.0
        const val BAT_MEMBRANE_BASE_MIN: Int = 0
        const val BAT_MEMBRANE_BASE_MAX: Int = 1
        const val BAT_MEMBRANE_LOOTING_BONUS: Int = 1
        const val HUSK_SAND_DROP_CHANCE: Double = 1.0
        const val HUSK_SAND_BASE_MIN: Int = 0
        const val HUSK_SAND_BASE_MAX: Int = 2
        const val HUSK_SAND_LOOTING_BONUS: Int = 1
        const val CAMEL_HUSK_SAND_BASE_MAX: Int = 3
        const val CAMEL_HUSK_SAND_LOOTING_BONUS: Int = 2
    }
}
