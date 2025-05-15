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
import kotlin.random.Random

/** Represents a module handling horde mechanics within the system. */
class HordeModule : ModuleInterface {
    override fun enabled(): Boolean = Config.HordeModule.ENABLED

    private companion object {
        const val NEW_MOON_PHASE = 4
        const val HORDE_CHANCE = 10
    }

    private var hordeState = HordeData()

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
        val isNewMoon = getMoonPhase(world) == NEW_MOON_PHASE
        val isNight = !world.isDayTime

        if (shouldActivateHorde(isNight, isNewMoon)) {
            hordeState.isActive = true
            hordeState.hasTriggeredThisNewMoon = true
            instance.server.onlinePlayers.forEach { it.showBossBar(Config.HordeModule.BOSSBAR) }
        } else if (shouldDeactivateHorde(isNight, isNewMoon)) {
            hordeState.isActive = false
            instance.server.onlinePlayers.forEach { it.hideBossBar(Config.HordeModule.BOSSBAR) }
        }

        if (world.time < 1000 && hordeState.hasTriggeredThisNewMoon) {
            hordeState.hasTriggeredThisNewMoon = false
        }
    }

    /** Returns the current moon phase based on the world time. */
    private fun getMoonPhase(world: World): Int = ((world.time / 24000) % 8).toInt()

    /**
     * Determines if the horde should be activated based on the current time and state.
     * @param isNight Indicates if it's currently night.
     * @param isNewMoon Indicates if it's currently a new moon.
     */
    private fun shouldActivateHorde(isNight: Boolean, isNewMoon: Boolean): Boolean {
        return isNight &&
                isNewMoon &&
                !hordeState.isActive &&
                !hordeState.hasTriggeredThisNewMoon &&
                Random.Default.nextInt(HORDE_CHANCE) == 0
    }

    /**
     * Determines if the horde should be deactivated based on the current time and state.
     * @param isNight Indicates if it's currently night.
     * @param isNewMoon Indicates if it's currently a new moon.
     */
    private fun shouldDeactivateHorde(isNight: Boolean, isNewMoon: Boolean): Boolean {
        return (!isNight || !isNewMoon) && hordeState.isActive
    }
}