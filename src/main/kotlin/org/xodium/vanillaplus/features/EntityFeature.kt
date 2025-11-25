@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.features

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.enchantments.NimbusEnchantment
import org.xodium.vanillaplus.interfaces.FeatureInterface
import kotlin.random.Random

/** Represents a feature handling entity mechanics within the system. */
internal object EntityFeature : FeatureInterface {
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
        if (Random.nextDouble() <= config.entityFeature.entityEggDropChance) {
            event.drops.add(ItemStack.of(Material.matchMaterial("${event.entity.type.name}_SPAWN_EGG") ?: return))
        }
    }

    @EventHandler
    fun on(event: EntityEquipmentChangedEvent) = NimbusEnchantment.nimbus(event)

    /**
     * Determines whether an entity's griefing behaviour should be cancelled based on configuration settings.
     * @param entity The entity whose griefing behaviour is being evaluated.
     * @return `true` if the entity's griefing behaviour should be cancelled; `false` otherwise.
     */
    private fun shouldCancelGrief(entity: Entity): Boolean =
        when (entity) {
            is WitherSkull -> config.entityFeature.disableWitherGrief
            is Fireball -> config.entityFeature.disableGhastGrief
            is Blaze -> config.entityFeature.disableBlazeGrief
            is Creeper -> config.entityFeature.disableCreeperGrief
            is EnderDragon -> config.entityFeature.disableEnderDragonGrief
            is Enderman -> config.entityFeature.disableEndermanGrief
            is Wither -> config.entityFeature.disableWitherGrief
            else -> false
        }
}
