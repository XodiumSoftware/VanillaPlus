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
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.TimeUtils
import org.xodium.vanillaplus.utils.WorldTimeUtils
import java.util.*

/** Represents a module handling blood-moon mechanics within the system. */
class BloodMoonModule : ModuleInterface {
    override fun enabled(): Boolean = Config.BloodMoonModule.ENABLED

    private var bloodMoonState = BloodMoonData()

    //TODO: add resourcepack fun via API.

    init {
        if (enabled()) schedule()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: CreatureSpawnEvent) {
        if (!bloodMoonState.isActive && !enabled()) return
        val entity = event.entity
        //TODO: check adjustments if its enough buff.
        Config.BloodMoonModule.MOB_ATTRIBUTE_ADJUSTMENTS.forEach { (attribute, adjust) ->
            entity.getAttribute(attribute)?.let { attr ->
                attr.baseValue = adjust(attr.baseValue)
                if (attribute == Attribute.MAX_HEALTH) {
                    entity.health = attr.baseValue
                }
            }
        }
    }

    /**
     * Schedules the blood moon event to run every 10 seconds.
     * It checks the world time and activates or deactivates the blood moon accordingly.
     */
    private fun schedule() {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable { bloodMoon() },
            0,
            TimeUtils.seconds(10)
        )
    }

    /**
     * Blood Moon event
     * Every 10 seconds, check if the world time is between 13000 and 23000
     * If it is, set isBloodMoon to true and broadcast a message
     * If it is not, set isBloodMoon to false and broadcast a message
     */
    private fun bloodMoon() {
        val world = instance.server.worlds.firstOrNull() ?: return
        if (world.time in WorldTimeUtils.NIGHT && !bloodMoonState.isActive) {
            if (Random().nextInt(10) == 0) {
                bloodMoonState.isActive = true
                //TODO: Perhaps use bossbar instead of broadcast?
                instance.server.broadcast("The Blood Moon Rises! Mobs grow stronger...".fireFmt().mm())
            }
        } else if (world.time < WorldTimeUtils.NIGHT.first && bloodMoonState.isActive) {
            bloodMoonState.isActive = false
            //TODO: Perhaps use bossbar instead of broadcast?
            instance.server.broadcast("The Blood Moon Sets! Mobs return to normal...".fireFmt().mm())
        }
    }
}