/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.managers.CooldownManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.Utils
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

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

    private val rtpCooldownKey = NamespacedKey(instance, "rtp_cooldown")

    /**
     * Teleports the player to a random location within the world border.
     * @param player The player to teleport.
     */
    private fun rtp(player: Player) {
        if (cooldown(player)) return

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

        val maxTries = ConfigManager.data.rtpModule.maxTries

        do {
            x = centerX + Random.nextDouble(-radius, radius)
            z = centerZ + Random.nextDouble(-radius, radius)
            y = world.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble() + 1
            tries++
        } while (!isSafeLocation(world, x, y, z) && tries < maxTries)

        if (tries >= maxTries) {
            player.sendActionBar("Could not find a safe location to teleport".fireFmt().mm())
            CooldownManager.setCooldown(player, rtpCooldownKey, 0)
            return
        }

        val initialLocation = player.location

        //TODO: add cool effects + sounds.
        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                if (player.location.blockX != initialLocation.blockX ||
                    player.location.blockY != initialLocation.blockY ||
                    player.location.blockZ != initialLocation.blockZ
                ) {
                    player.sendActionBar("You moved! Teleportation cancelled.".fireFmt().mm())
                    CooldownManager.setCooldown(player, rtpCooldownKey, 0)
                    return@Runnable
                }
                player.teleport(Location(world, x, y, z))
            },
            ConfigManager.data.rtpModule.delay
        )
    }

    private fun cooldown(player: Player): Boolean {
        val cooldownDuration = ConfigManager.data.rtpModule.cooldown.seconds.inWholeMilliseconds
        if (CooldownManager.isOnCooldown(player, rtpCooldownKey, cooldownDuration)) {
            val lastUsed = CooldownManager.getCooldown(player, rtpCooldownKey)
            val remaining = (lastUsed + cooldownDuration - System.currentTimeMillis()) / 1000
            player.sendActionBar(
                "You must wait ${remaining.toString().mangoFmt()}s before using RTP again.".fireFmt().mm()
            )
            return true
        }

        CooldownManager.setCooldown(player, rtpCooldownKey, System.currentTimeMillis())
        return false
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