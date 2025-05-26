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
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.EclipseData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils
import kotlin.random.Random

/** Represents a module handling eclipse mechanics within the system. */
class EclipseModule : ModuleInterface {
    override fun enabled(): Boolean = Config.EclipseModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("eclipse")
                .requires { it.sender.hasPermission(Perms.Eclipse.ECLIPSE) }
                .executes { it -> Utils.tryCatch(it) { skipToEclipse(it.sender as Player) } })
    }

    private var hordeState = EclipseData()

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable { eclipse() },
                Config.EclipseModule.INIT_DELAY,
                Config.EclipseModule.INTERVAL
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: CreatureSpawnEvent) {
        val entity = event.entity
        val world = entity.world

        if (!enabled() || !hordeState.isActive) return
        if (world.environment != World.Environment.NORMAL) return
        if (world.difficulty != Difficulty.HARD) return
        if (entity.type in Config.EclipseModule.EXCLUDED_MOBS) return

        Config.EclipseModule.MOB_ATTRIBUTE
            .filter { it.types.contains(entity.type) }
            .forEach { mobAttr ->
                mobAttr.attributes.forEach { (attribute, adjust) ->
                    entity.getAttribute(attribute)?.let { attr ->
                        attr.baseValue = adjust(attr.baseValue)
                        if (attribute == Attribute.MAX_HEALTH) {
                            entity.health = attr.baseValue
                        }
                    }
                }
            }

        val equipment = entity.equipment ?: return

        if (Config.EclipseModule.MOB_EQUIPMENT.isNotEmpty()) {
            Config.EclipseModule.MOB_EQUIPMENT.forEach { config ->
                when (config.slot) {
                    EquipmentSlot.HEAD -> {
                        equipment.helmet = config.item.clone()
                        equipment.helmetDropChance = config.dropChance
                    }

                    EquipmentSlot.CHEST -> {
                        equipment.chestplate = config.item.clone()
                        equipment.chestplateDropChance = config.dropChance
                    }

                    EquipmentSlot.LEGS -> {
                        equipment.leggings = config.item.clone()
                        equipment.leggingsDropChance = config.dropChance
                    }

                    EquipmentSlot.FEET -> {
                        equipment.boots = config.item.clone()
                        equipment.bootsDropChance = config.dropChance
                    }

                    EquipmentSlot.HAND -> {
                        equipment.setItemInMainHand(config.item.clone())
                        equipment.itemInMainHandDropChance = config.dropChance
                    }

                    EquipmentSlot.OFF_HAND -> {
                        equipment.setItemInOffHand(config.item.clone())
                        equipment.itemInOffHandDropChance = config.dropChance
                    }

                    else -> {}
                }
            }
        }

        if (entity.type == EntityType.CREEPER && Random.nextBoolean()) {
            (entity as Creeper).isPowered = Config.EclipseModule.RANDOM_POWERED_CREEPERS
        }

        if (hordeState.isActive && event.spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) {
            val specific = Config.EclipseModule.MOB_ATTRIBUTE
                .firstOrNull { it.types.size == 1 && it.types.contains(entity.type) }
            val general = Config.EclipseModule.MOB_ATTRIBUTE
                .firstOrNull { it.types.containsAll(EntityType.entries) }
            val spawnRate = (specific ?: general)?.spawnRate?.toInt() ?: 1

            repeat(spawnRate - 1) {
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
        player.sendMessage("${PREFIX}${"Skipped to the next eclipse!".fireFmt()}".mm())
    }
}