/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.title.Title
import org.bukkit.*
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
import java.time.Duration
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
            cancelledEffects(player, "Failed to find a safe location after $maxTries tries")
            CooldownManager.setCooldown(player, rtpCooldownKey, 0)
            return
        }

        val initialLocation = player.location

        var countdown = ConfigManager.data.rtpModule.delay

        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                if (player.location.blockX != initialLocation.blockX ||
                    player.location.blockY != initialLocation.blockY ||
                    player.location.blockZ != initialLocation.blockZ
                ) {
                    cancelledEffects(player, "You moved! Teleportation cancelled")
                    CooldownManager.setCooldown(player, rtpCooldownKey, 0)
                    return@Runnable
                }
                if (countdown > 0) {
                    player.showTitle(teleportTitle(countdown))
                    player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1f, 1.5f))
                    countdown--
                } else {
                    beforeEffects(player)
                    player.teleport(Location(world, x, y, z))
                    afterEffects(player)
                    instance.server.scheduler.cancelTask(this.hashCode())
                }
            },
            0L,
            ConfigManager.data.rtpModule.period
        )
    }

    /**
     * Creates a teleportation title for the player.
     * @param countdown The countdown value to display.
     */
    private fun teleportTitle(countdown: Long): Title {
        return Title.title(
            "Teleporting in".fireFmt().mm(),
            countdown.toString().mangoFmt().mm(),
            Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(100))
        )
    }

    /**
     * Plays the cancelled teleportation effects.
     * @param player The player to apply effects to.
     * @param msg The message to display to the player.
     */
    private fun cancelledEffects(player: Player, msg: String) {
        player.sendActionBar(msg.fireFmt().mm())
        player.playSound(Sound.sound(Key.key("block.beacon.deactivate"), Sound.Source.PLAYER, 1f, 1f))
    }

    /**
     * Plays the before teleportation effects.
     * @param player The player to apply effects to.
     */
    private fun beforeEffects(player: Player) {
        player.playEffect(EntityEffect.TELEPORT_ENDER)
        player.playSound(Sound.sound(Key.key("block.beacon.activate"), Sound.Source.PLAYER, 1f, 1f))
    }

    /**
     * Plays the after teleportation effects.
     * @param player The player to apply effects to.
     */
    private fun afterEffects(player: Player) {
        player.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.PLAYER, 1f, 1f))
        player.world.spawnParticle(Particle.PORTAL, player.location.add(0.0, 0.5, 0.0), 50, 0.5, 1.0, 0.5, 0.1)
    }

    /**
     * Checks if the player is on cooldown for the RTP command.
     * @param player The player to check.
     * @return True if the player is on cooldown, false otherwise.
     */
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