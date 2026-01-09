@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.enchantments.NimbusEnchantment
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils
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
        if (Random.nextDouble() <= config.entityModule.entityEggDropChance) {
            event.drops.add(ItemStack.of(Material.matchMaterial("${event.entity.type.name}_SPAWN_EGG") ?: return))
        }
    }

    @EventHandler
    fun on(event: EntityEquipmentChangedEvent) = NimbusEnchantment.nimbus(event)

    @EventHandler
    fun on(event: EntitySpawnEvent) = randomizeEntityScale(event.entity)

    /**
     * Determines whether an entity's griefing behaviour should be cancelled based on configuration settings.
     * @param entity The entity whose griefing behaviour is being evaluated.
     * @return `true` if the entity's griefing behaviour should be cancelled; `false` otherwise.
     */
    private fun shouldCancelGrief(entity: Entity): Boolean =
        when (entity) {
            is WitherSkull -> config.entityModule.disableWitherGrief
            is Fireball -> config.entityModule.disableGhastGrief
            is Blaze -> config.entityModule.disableBlazeGrief
            is Creeper -> config.entityModule.disableCreeperGrief
            is EnderDragon -> config.entityModule.disableEnderDragonGrief
            is Enderman -> config.entityModule.disableEndermanGrief
            is Wither -> config.entityModule.disableWitherGrief
            else -> false
        }

    /**
     * Randomizes the scale of certain entities based on configuration settings.
     * @param entity The entity whose scale should be randomized.
     */
    private fun randomizeEntityScale(entity: Entity) {
        when (entity) {
            is Animals -> {
                val range = config.entityModule.animalScaleRange ?: return
                entity.getAttribute(Attribute.SCALE)?.baseValue = Random.nextDouble(range.min, range.max)
            }

            is Monster -> {
                val range = config.entityModule.monsterScaleRange ?: return
                entity.getAttribute(Attribute.SCALE)?.baseValue = Random.nextDouble(range.min, range.max)
            }

            is Villager -> {
                val range = config.entityModule.villagerScaleRange ?: return
                entity.getAttribute(Attribute.SCALE)?.baseValue = Random.nextDouble(range.min, range.max)
            }
        }
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var disableBlazeGrief: Boolean = true,
        var disableCreeperGrief: Boolean = true,
        var disableEnderDragonGrief: Boolean = true,
        var disableEndermanGrief: Boolean = true,
        var disableGhastGrief: Boolean = true,
        var disableWitherGrief: Boolean = true,
        var entityEggDropChance: Double = 0.001,
        var animalScaleRange: Utils.Range? = Utils.Range(0.8, 1.2),
        var monsterScaleRange: Utils.Range? = Utils.Range(0.9, 1.9),
        var villagerScaleRange: Utils.Range? = Utils.Range(0.9, 1.1),
    )
}
