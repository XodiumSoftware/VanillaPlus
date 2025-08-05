@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling mobs mechanics within the system. */
internal class MobsModule : ModuleInterface<MobsModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (!enabled()) return
        if (when (event.entity) {
                is Creeper -> config.disableCreeperGrief
                is Wither -> config.disableWitherGrief
                is WitherSkull -> config.disableWitherSkullGrief
                is EnderDragon -> config.disableEnderDragonGrief
                else -> false
            }
        ) {
            event.blockList().clear()
        }
    }

    @EventHandler
    fun on(event: ProjectileHitEvent) {
        if (!enabled() || !config.disableGhastGrief) return

        val projectile = event.entity
        if (projectile is Fireball && projectile.shooter is Ghast) {
            event.hitBlock?.let {
                event.isCancelled = true
                projectile.remove()
            }
        }
    }

    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (!enabled()) return
        if (event.entity is Enderman && config.disableEndermanGrief) event.isCancelled = true
    }

    @EventHandler
    fun on(event: BlockIgniteEvent) {
        if (!enabled()) return
        if (event.ignitingEntity is Blaze && config.disableBlazeGrief) event.isCancelled = true
    }

    data class Config(
        override var enabled: Boolean = true,
        var disableCreeperGrief: Boolean = true,
        var disableGhastGrief: Boolean = true,
        var disableWitherGrief: Boolean = true,
        var disableWitherSkullGrief: Boolean = true,
        var disableEnderDragonGrief: Boolean = true,
        var disableEndermanGrief: Boolean = true,
        var disableBlazeGrief: Boolean = true,
    ) : ModuleInterface.Config
}
