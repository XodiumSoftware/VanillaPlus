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

/** Represents a module handling horde mechanics within the system. */
class HordeModule : ModuleInterface {
    override fun enabled(): Boolean = Config.HordeModule.ENABLED

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
        val isNewMoon = getMoonPhase(world) == 4
        val isNight = !world.isDayTime

        if (shouldActivateHorde(isNight, isNewMoon)) {
            activateHorde(world)
        } else if (shouldDeactivateHorde(isNight, isNewMoon)) {
            deactivateHorde(world)
        }

        if (world.time < 1000 && hordeState.hasTriggeredThisNewMoon) {
            hordeState.hasTriggeredThisNewMoon = false
        }
    }

    //TODO: check if this works.
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
                !hordeState.hasTriggeredThisNewMoon
    }

    /**
     * Determines if the horde should be deactivated based on the current time and state.
     * @param isNight Indicates if it's currently night.
     * @param isNewMoon Indicates if it's currently a new moon.
     */
    private fun shouldDeactivateHorde(isNight: Boolean, isNewMoon: Boolean): Boolean {
        return (!isNight || !isNewMoon) && hordeState.isActive
    }

    /**
     * Activates the horde by setting its state to active and showing the boss bar to all players.
     * Also sets the world to stormy and thundering.
     * @param world The world in which the horde is activated.
     */
    private fun activateHorde(world: World) {
        hordeState.isActive = true
        hordeState.hasTriggeredThisNewMoon = true
        instance.server.onlinePlayers.forEach { it.showBossBar(Config.HordeModule.BOSSBAR) }
        world.setStorm(true)
        world.isThundering = true
    }

    /**
     * Deactivates the horde by setting its state to inactive and hiding the boss bar from all players.
     * Also sets the world to clear weather.
     * @param world The world in which the horde is deactivated.
     */
    private fun deactivateHorde(world: World) {
        hordeState.isActive = false
        instance.server.onlinePlayers.forEach { it.hideBossBar(Config.HordeModule.BOSSBAR) }
        world.setStorm(false)
        world.isThundering = false
    }
}