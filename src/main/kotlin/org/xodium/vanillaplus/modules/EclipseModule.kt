/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Difficulty
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.EclipseData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils

/** Represents a module handling eclipse mechanics within the system. */
class EclipseModule : ModuleInterface {
    override fun enabled(): Boolean = Config.EclipseModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmd(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("eclipse")
                .requires { it.sender.hasPermission(Perms.Eclipse.ECLIPSE) }
                .executes { it -> Utils.tryCatch(it) { skipToEclipse(it.sender as Player) } })
    }

    private var hordeState = EclipseData()

    init {
        if (enabled()) schedule()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: CreatureSpawnEvent) {
        val entity = event.entity
        val world = entity.world

        if (!hordeState.isActive && !enabled()) return
        if (world.environment != World.Environment.NORMAL) return
        if (world.difficulty != Difficulty.HARD) return

        Config.EclipseModule.MOB_ATTRIBUTE_ADJUSTMENTS.forEach { (attribute, adjust) ->
            entity.getAttribute(attribute)?.let { attr ->
                attr.baseValue = adjust(attr.baseValue)
                if (attribute == Attribute.MAX_HEALTH) {
                    entity.health = attr.baseValue
                }
            }
        }

        if (hordeState.isActive && event.spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) {
            repeat(Config.EclipseModule.SPAWN_RATE) {
                entity.world.spawnEntity(
                    entity.location.clone().add(
                        (-5..5).random().toDouble(),
                        0.0,
                        (-5..5).random().toDouble()
                    ), entity.type
                )
            }
        }
    }

    /** Holds all the schedules for this module. */
    private fun schedule() {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable { eclipse() },
            Config.EclipseModule.INIT_DELAY,
            Config.EclipseModule.INTERVAL
        )
    }

    /** Handles the eclipse mechanics. */
    private fun eclipse() {
        val world = instance.server.worlds.firstOrNull() ?: return
        val isNewMoon = getMoonPhase(world) == 4
        val isNight = !world.isDayTime

        if (shouldActivateEclipse(isNight, isNewMoon)) {
            activateEclipse(world)
        } else if (shouldDeactivateEclipse(isNight, isNewMoon)) {
            deactivateEclipse(world)
        }

        if (world.time < 1000 && hordeState.hasTriggeredThisEclipse) {
            hordeState.hasTriggeredThisEclipse = false
        }
    }

    /**
     * Determines if the eclipse should be activated based on the current time and state.
     * @param isNight Indicates if it's currently night.
     * @param isEclipse Indicates if it's currently an eclipse.
     */
    private fun shouldActivateEclipse(isNight: Boolean, isEclipse: Boolean): Boolean {
        return isNight &&
                isEclipse &&
                !hordeState.isActive &&
                !hordeState.hasTriggeredThisEclipse
    }

    /**
     * Determines if the eclipse should be deactivated based on the current time and state.
     * @param isNight Indicates if it's currently night.
     * @param isEclipse Indicates if it's currently an eclipse.
     */
    private fun shouldDeactivateEclipse(isNight: Boolean, isEclipse: Boolean): Boolean {
        return (!isNight || !isEclipse) && hordeState.isActive
    }

    /**
     * Activates the eclipse by setting its state to active and showing the boss bar to all players.
     * Also sets the world to stormy and thundering.
     * @param world The world in which the eclipse is activated.
     */
    private fun activateEclipse(world: World) {
        hordeState.isActive = true
        hordeState.hasTriggeredThisEclipse = true
        instance.server.onlinePlayers.forEach {
            it.sendActionBar(Config.EclipseModule.ECLIPSE_START_MSG.mm())
            it.playSound(Config.EclipseModule.ECLIPSE_START_SOUND)
        }
        world.setStorm(true)
        world.isThundering = true
    }

    /**
     * Deactivates the eclipse by setting its state to inactive and hiding the boss bar from all players.
     * Also sets the world to clear weather.
     * @param world The world in which the eclipse is deactivated.
     */
    private fun deactivateEclipse(world: World) {
        hordeState.isActive = false
        instance.server.onlinePlayers.forEach {
            it.sendActionBar(Config.EclipseModule.ECLIPSE_END_MSG.mm())
            it.playSound(Config.EclipseModule.ECLIPSE_END_SOUND)
        }
        world.setStorm(false)
        world.isThundering = false
    }

    /**
     * Gets the current moon phase based on the world time.
     * @param world The world from which to get the moon phase.
     * @return The current moon phase.
     */
    private fun getMoonPhase(world: World): Int = ((world.fullTime / 24000) % 8).toInt()

    /**
     * Skips to the next new moon by adjusting the world time.
     * @param player The player who executed the command.
     */
    private fun skipToEclipse(player: Player) {
        val world = player.world
        val currentDay = world.fullTime / 24000
        val currentPhase = getMoonPhase(world)
        val daysToNewMoon = (4 - currentPhase + 8) % 8
        val newMoonDay = currentDay + daysToNewMoon
        world.fullTime = newMoonDay * 24000 + 13000
        player.sendMessage("Skipped to the next new moon night!".fireFmt().mm())
    }
}