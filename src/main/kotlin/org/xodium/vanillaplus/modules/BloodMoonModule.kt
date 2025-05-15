/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.BloodMoonData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.WorldTimeUtils
import java.util.*

/** Represents a module handling blood-moon mechanics within the system. */
class BloodMoonModule : ModuleInterface {
    override fun enabled(): Boolean = Config.BloodMoonModule.ENABLED

    private var bloodMoonState = BloodMoonData()

    init {
        if (enabled()) schedule()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: CreatureSpawnEvent) {
        if (!bloodMoonState.isActive && !enabled()) return
        val entity = event.entity
        Config.BloodMoonModule.MOB_ATTRIBUTE_ADJUSTMENTS.forEach { (attribute, adjust) ->
            entity.getAttribute(attribute)?.let { attr ->
                attr.baseValue = adjust(attr.baseValue)
                if (attribute == Attribute.MAX_HEALTH) {
                    entity.health = attr.baseValue
                }
            }
        }
    }

    /** Holds all the schedules for this module. */
    private fun schedule() {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable { bloodMoon() },
            Config.BloodMoonModule.INIT_DELAY,
            Config.BloodMoonModule.INTERVAL
        )
    }

    /** Handles the blood-moon mechanics. */
    private fun bloodMoon() {
        val world = instance.server.worlds.firstOrNull() ?: return
        if (world.time in WorldTimeUtils.NIGHT && !bloodMoonState.isActive) {
            if (Random().nextInt(10) == 0) {
                bloodMoonState.isActive = true
                instance.server.onlinePlayers.forEach { it.showBossBar(Config.BloodMoonModule.BOSSBAR) }
            }
        } else if (world.time < WorldTimeUtils.NIGHT.first && bloodMoonState.isActive) {
            bloodMoonState.isActive = false
            instance.server.onlinePlayers.forEach { it.hideBossBar(Config.BloodMoonModule.BOSSBAR) }
        }
    }
}