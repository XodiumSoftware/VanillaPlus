@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

/** Represents a module handling entity mechanics within the system. */
internal class EntityModule : ModuleInterface<EntityModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (!enabled()) return
        if (shouldCancelGrief(event.entity)) event.isCancelled = true
    }

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (!enabled()) return
        if (shouldCancelGrief(event.entity)) event.blockList().clear()
    }

    @EventHandler
    fun on(event: EntityDeathEvent) {
        if (!enabled() || event.entity.killer == null) return
        if (Random.nextDouble() <= config.entityEggDropChance) {
            event.drops.add(ItemStack.of(Material.matchMaterial("${event.entity.type.name}_SPAWN_EGG") ?: return))
        }
        val entity = event.entity as? HappyGhast ?: return
        val location = entity.location
        if (location.y <= 187) return
        // TODO: implement nimbus
    }

    /**
     * Determines whether an entity's griefing behaviour should be cancelled based on configuration settings.
     * @param entity The entity whose griefing behaviour is being evaluated.
     * @return `true` if the entity's griefing behaviour should be cancelled; `false` otherwise.
     */
    private fun shouldCancelGrief(entity: Entity): Boolean =
        when (entity) {
            is WitherSkull -> config.disableWitherGrief
            is Fireball -> config.disableGhastGrief
            is Blaze -> config.disableBlazeGrief
            is Creeper -> config.disableCreeperGrief
            is EnderDragon -> config.disableEnderDragonGrief
            is Enderman -> config.disableEndermanGrief
            is Wither -> config.disableWitherGrief
            else -> false
        }

    data class Config(
        var disableBlazeGrief: Boolean = true,
        var disableCreeperGrief: Boolean = true,
        var disableEnderDragonGrief: Boolean = true,
        var disableEndermanGrief: Boolean = true,
        var disableGhastGrief: Boolean = true,
        var disableWitherGrief: Boolean = true,
        var entityEggDropChance: Double = 0.1,
    ) : ModuleInterface.Config
}
