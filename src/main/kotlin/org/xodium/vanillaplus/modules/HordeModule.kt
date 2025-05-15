/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.HordeData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.WorldTimeUtils
import java.util.*

/** Represents a module handling horde mechanics within the system. */
class HordeModule : ModuleInterface {
    override fun enabled(): Boolean = Config.HordeModule.ENABLED

    private var hordeState = HordeData()
    private var hasTriggeredThisFullMoon = false

    init {
        if (enabled()) schedule()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: CreatureSpawnEvent) {
        if (!hordeState.isActive && !enabled()) return
        val entity = event.entity
        Config.HordeModule.MOB_ATTRIBUTE_ADJUSTMENTS.forEach { (attribute, adjust) ->
            entity.getAttribute(attribute)?.let { attr ->
                attr.baseValue = adjust(attr.baseValue)
                if (attribute == Attribute.MAX_HEALTH) {
                    entity.health = attr.baseValue
                }
            }
        }
    }

    private fun getMoonPhase(world: World): Int {
        return ((world.fullTime / 24000) % 8).toInt()
    }

    /** Holds all the schedules for this module. */
    private fun schedule() {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable { horde() },
            Config.HordeModule.INIT_DELAY,
            Config.HordeModule.INTERVAL
        )
    }

    /** Handles the horde mechanics. */
    private fun horde() {
        val world = instance.server.worlds.firstOrNull() ?: return
        val isFullMoon = getMoonPhase(world) == 0
        val isNight = world.time in WorldTimeUtils.NIGHT

        if (isNight && isFullMoon && !hordeState.isActive && !hasTriggeredThisFullMoon) {
            if (Random().nextInt(10) == 0) {
                hordeState.isActive = true
                hasTriggeredThisFullMoon = true
                instance.server.onlinePlayers.forEach { it.showBossBar(Config.HordeModule.BOSSBAR) }
            }
        } else if (world.time < WorldTimeUtils.NIGHT.first && hordeState.isActive) {
            hordeState.isActive = false
            instance.server.onlinePlayers.forEach { it.hideBossBar(Config.HordeModule.BOSSBAR) }
        }

        if (world.time < 1000 && hasTriggeredThisFullMoon) {
            hasTriggeredThisFullMoon = false
        }
    }
}