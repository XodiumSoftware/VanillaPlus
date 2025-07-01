/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ChestAccessManager
import org.xodium.vanillaplus.managers.CooldownManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.TimeUtils
import org.xodium.vanillaplus.utils.Utils
import org.xodium.vanillaplus.utils.Utils.tryCatch
import java.util.concurrent.CompletableFuture

/** Represents a module handling inv-search mechanics within the system. */
class InvSearchModule : ModuleInterface<InvSearchModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                Commands.literal("invsearch")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands.argument("material", StringArgumentType.word())
                            .suggests { ctx, builder ->
                                Material.entries
                                    .map { it.name.lowercase() }
                                    .filter { it.startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }
                            .executes { ctx -> ctx.tryCatch { handleSearch(ctx) } }
                    )
                    .executes { ctx -> ctx.tryCatch { handleSearch(ctx) } }
            ),
            "Allows players to search inventories for specific materials.",
            listOf("search", "searchinv", "invs")
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.invsearch.use".lowercase(),
                "Allows use of the autorestart command",
                PermissionDefault.TRUE
            )
        )
    }

    /**
     * Handles the search command execution.
     * @param ctx The command context containing the command source and arguments.
     * @return An integer indicating the result of the command execution.
     */
    private fun handleSearch(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as? Player ?: return 0
        val materialName = runCatching { StringArgumentType.getString(ctx, "material") }.getOrNull()
        val material =
            materialName?.let { Material.getMaterial(it.uppercase()) } ?: player.inventory.itemInMainHand.type
        if (material == Material.AIR) {
            player.sendActionBar("You must specify a valid material or hold something in your hand".fireFmt().mm())
            return 0
        }

        search(player, material)
        return 1
    }

    /**
     * Searches for chests within the specified radius of the player that contain the specified material.
     * @param player The player who initiated the search.
     * @param material The material to search for in the chests.
     */
    private fun search(player: Player, material: Material) {
        val cooldownKey = NamespacedKey(instance, "invsearch_cooldown")
        val cooldownDuration = config.cooldown
        if (CooldownManager.isOnCooldown(player, cooldownKey, cooldownDuration)) {
            return player.sendActionBar("You must wait before using this again.".fireFmt().mm())
        }
        CooldownManager.setCooldown(player, cooldownKey, System.currentTimeMillis())

        val deniedChestKey = NamespacedKey(instance, "denied_chest")
        val chests = Utils.findBlocksInRadius(player.location, config.searchRadius)
            .filter { ChestAccessManager.isAllowed(player, deniedChestKey, it) }
        if (chests.isEmpty()) {
            return player.sendActionBar("No usable chests found for ${"$material".roseFmt()}".fireFmt().mm())
        }

        val seenDoubleChests = mutableSetOf<InventoryHolder?>()
        val affectedChests = chests.filter { block ->
            val inventory = (block.state as Container).inventory
            val holder = inventory.holder
            if (holder is DoubleChest) {
                if (!seenDoubleChests.add(holder.leftSide)) return@filter false
            }
            searchItemInContainers(material, inventory)
        }
        if (affectedChests.isEmpty()) {
            return player.sendActionBar("No chests contain ${"$material".roseFmt()}".fireFmt().mm())
        }

        affectedChests.forEach { Utils.chestEffect(player, it) }
        laserEffectSchedule(player, affectedChests)
    }

    /**
     * Searches for a specific item in the given inventory and its containers.
     * @param material The material to search for.
     * @param destination The inventory to search in.
     * @return True if the item was found in the inventory or its containers, false otherwise.
     */
    private fun searchItemInContainers(material: Material, destination: Inventory): Boolean {
        if (Utils.doesChestContain(destination, ItemStack(material))) {
            destination.location?.let {
                Utils.protocolUnload(
                    it,
                    material,
                    doesChestContainCount(destination, material)
                )
            }
            return true
        }
        return false
    }

    /**
     * Get the amount of a specific material in a chest.
     * @param inventory The inventory to check.
     * @param material The material to count.
     * @return The amount of the material in the chest.
     */
    private fun doesChestContainCount(inventory: Inventory, material: Material): Int {
        return inventory.contents.filter { it?.type == material }.sumOf { it?.amount ?: 0 }
    }

    /**
     * Creates a laser effect for the specified player and chests.
     * @param player The player to play the effect for.
     * @param affectedChests The list of chests to affect. If null, uses the last unloaded chests.
     */
    private fun laserEffectSchedule(player: Player, affectedChests: List<Block>? = null) {
        val chests = affectedChests ?: Utils.lastUnloads[player.uniqueId] ?: return

        Utils.activeVisualizations[player.uniqueId] = instance.server.scheduler.scheduleSyncRepeatingTask(
            instance,
            { laserEffect(chests, player, 0.3, 2, Particle.CRIT, 0.001, 128) },
            0L,
            2L
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                Utils.activeVisualizations[player.uniqueId]?.let {
                    instance.server.scheduler.cancelTask(it)
                    Utils.activeVisualizations.remove(player.uniqueId)
                }
            },
            TimeUtils.seconds(5)
        )
    }

    /**
     * Creates a laser effect between the player and the specified blocks.
     * @param destinations The list of blocks to create the laser effect towards.
     * @param player The player to create the laser effect for.
     * @param interval The interval between each particle spawn.
     * @param count The number of particles to spawn at each location.
     * @param particle The type of particle to spawn.
     * @param speed The speed of the particles.
     * @param maxDistance The maximum distance for the laser effect.
     */
    private fun laserEffect(
        destinations: List<Block>,
        player: Player,
        interval: Double,
        count: Int,
        particle: Particle,
        speed: Double,
        maxDistance: Int
    ) {
        destinations.forEach { destination ->
            val start = player.location.clone()
            val end = Utils.getCenterOfBlock(destination).add(0.0, -0.5, 0.0)
            val direction = end.toVector().subtract(start.toVector()).normalize()
            val distance = start.distance(destination.location)
            if (distance < maxDistance) {
                var i = 1.0
                while (i <= distance) {
                    val point = start.clone().add(direction.clone().multiply(i))
                    player.spawnParticle(particle, point, count, 0.0, 0.0, 0.0, speed)
                    i += interval
                }
            }
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var cooldown: Long = 1L * 1000L,
        var searchRadius: Int = 5,
    ) : ModuleInterface.Config
}