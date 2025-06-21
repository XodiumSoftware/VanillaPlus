/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils
import kotlin.random.Random

class RtpModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.rtpModule.enabled

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("rtp")
                .requires { it.sender.hasPermission(perms()[0]) }
                .executes { ctx -> Utils.tryCatch(ctx) { rtp(it.sender as Player) } })
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.rtp.use".lowercase(),
                "Allows use of the rtp command",
                PermissionDefault.TRUE
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return

        val player = event.player
        if (!player.hasPlayedBefore()) rtp(player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        if (!enabled()) return

        val player = event.entity
        if (player.respawnLocation == null) rtp(player)
    }

    /**
     * Teleports the player to a random location within the world border.
     * @param player The player to teleport.
     */
    private fun rtp(player: Player) {
        val world = player.world
        val border = world.worldBorder
        val margin = 16.0

        val centerX = border.center.x
        val centerZ = border.center.z
        val radius = (border.size / 2.0) - margin

        var x: Double
        var z: Double
        var y: Double
        var tries = 0

        val maxTries = 10

        do {
            x = centerX + Random.nextDouble(-radius, radius)
            z = centerZ + Random.nextDouble(-radius, radius)
            y = world.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble() + 1
            tries++
        } while (!isSafeLocation(world, x, y, z) && tries < maxTries)

        if (tries >= maxTries) {
            return player.sendActionBar("Could not find a safe location to teleport".fireFmt().mm())
        }

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { player.teleport(Location(world, x, y, z)) },
            1L
        )
    }

    /**
     * Checks if the location is safe for teleportation.
     * @param world The world to check.
     * @param x The x-coordinate of the location.
     * @param y The y-coordinate of the location.
     * @param z The z-coordinate of the location.
     * @return True if the location is safe, false otherwise.
     */
    private fun isSafeLocation(world: World, x: Double, y: Double, z: Double): Boolean {
        val blockBelow = world.getBlockAt(x.toInt(), y.toInt() - 1, z.toInt())
        val blockAtFeet = world.getBlockAt(x.toInt(), y.toInt(), z.toInt())
        val blockAtHead = world.getBlockAt(x.toInt(), y.toInt() + 1, z.toInt())
        return blockBelow.type.isSolid && blockAtFeet.type.isAir && blockAtHead.type.isAir
    }
}