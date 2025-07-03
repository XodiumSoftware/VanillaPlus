/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

class PetModule : ModuleInterface<PetModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        if (!enabled()) return

        val source = event.player
        if (event.rightClicked !is Player) return
        if (source.inventory.itemInMainHand.type != Material.LEAD) return

        val target = event.rightClicked as Player
        val leashedEntity = source.getNearbyEntities(10.0, 10.0, 10.0).firstOrNull {
            it is LivingEntity && it.isLeashed && it.leashHolder == source
        }

        if (leashedEntity == null) return

        if (leashedEntity !is Tameable) {
            return source.sendActionBar("The leashed entity is not a pet.".fireFmt().mm())
        }

        if (!leashedEntity.isTamed || leashedEntity.owner != source) {
            return source.sendActionBar("You don't own this pet.".fireFmt().mm())
        }

        leashedEntity.owner = target
        leashedEntity.setLeashHolder(null)

        source.sendActionBar("You have transferred your pet to ${target.name}.".fireFmt().mm())
        target.sendActionBar("${source.name} has transferred their pet to you.".fireFmt().mm())

        event.isCancelled = true
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}