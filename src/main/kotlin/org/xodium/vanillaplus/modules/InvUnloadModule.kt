/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.InvUnloadSummaryData
import org.xodium.vanillaplus.hooks.ChestSortHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.TimeUtils
import org.xodium.vanillaplus.utils.Utils
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Sound as BukkitSound

/** Represents a module handling inv-unload mechanics within the system. */
class InvUnloadModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvUnloadModule.ENABLED

    private val lastUnloads = ConcurrentHashMap<UUID, List<Block>>()
    private val lastUnloadPositions = ConcurrentHashMap<UUID, Location>()
    private val activeVisualizations = ConcurrentHashMap<UUID, Int>()
    private val unloadSummaries = ConcurrentHashMap<UUID, InvUnloadSummaryData>()
    private val unloads = ConcurrentHashMap<Location, MutableMap<Material, Int>>()

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? {
        return Commands.literal("invunload")
            .requires { it.sender.hasPermission(Perms.InvUnload.USE) }
            .executes { it -> Utils.tryCatch(it) { unload(it.sender as Player) } }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (enabled()) cleanup(event.player)
    }

    /**
     * Unloads the inventory of the specified player.
     * @param player The player whose inventory to unload.
     */
    private fun unload(player: Player) {
        if (!Utils.cooldown(
                player,
                Config.InvUnloadModule.COOLDOWN,
                NamespacedKey(instance, "${InvUnloadModule::class.simpleName?.lowercase()}_cooldown")
            )
        ) return

        val startSlot = 9
        val endSlot = 35
        val onlyMatchingStuff = false
        val chests: MutableList<Block>? = Utils.findBlocksInRadius(player.location, 5)

        if (chests!!.isEmpty()) return

        chests.sortBy { it.location.distance(player.location) }

        val useableChests = ArrayList<Block>()

        for (block in chests) if (Utils.canPlayerUseChest(block, player)) useableChests.add(block)

        val affectedChests = mutableListOf<Block>()

        for (block in useableChests) {
            val inv: Inventory = (block.state as Container).inventory
            if (Utils.stuffInventoryIntoAnother(player, inv, true, startSlot, endSlot, InvUnloadModule())) {
                affectedChests.add(block)
            }
        }

        if (!onlyMatchingStuff) {
            for (block in useableChests) {
                val inv: Inventory = (block.state as Container).inventory
                if (Utils.stuffInventoryIntoAnother(player, inv, false, startSlot, endSlot, InvUnloadModule())) {
                    affectedChests.add(block)
                }
            }
        }
        print(player)

        for (i in startSlot..endSlot) {
            val item = player.inventory.getItem(i)
            if (item == null || item.amount == 0 || item.type == Material.AIR) continue
        }

        val materials = mutableMapOf<Material, Int>()

        save(player, affectedChests, materials)

        for (block in affectedChests) {
            chestEffect(block, player)
            play(player)
            if (ChestSortHook.shouldSort(player)) ChestSortHook.sort(block)
        }

        player.playSound(
            Sound.sound(
                BukkitSound.BLOCK_FENCE_GATE_CLOSE, //TODO change sound.
                Sound.Source.MASTER,
                1.0f,
                1.0f
            )
        )
    }

    /**
     * Unloads the specified amount of material from the given location.
     * @param loc The location to unload from.
     * @param mat The material to unload.
     * @param amount The amount of material to unload.
     */
    fun protocolUnload(loc: Location, mat: Material, amount: Int) {
        if (amount == 0) return
        unloads.computeIfAbsent(loc) { mutableMapOf() }.merge(mat, amount, Int::plus)
    }

    /**
     * Converts a location to a string representation.
     * @param loc The location to convert.
     * @return A string representation of the location.
     */
    private fun loc2str(loc: Location): Component {
        val x = loc.blockX
        val y = loc.blockY
        val z = loc.blockZ
        val world = loc.world
        val name = if (world != null) {
            val state = world.getBlockAt(x, y, z).state
            (state as? Container)?.customName()?.toString() ?: state.type.name
        } else {
            "Unknown"
        }
        return """
            <light_purple><b>$name</b>
            <green><b>X:</b></green> <white>$x</white>
            <green><b>Y:</b></green> <white>$y</white>
            <green><b>Z:</b></green> <white>$z</white>
        """.trimIndent().mm()
    }

    /**
     * Converts an amount to a string representation.
     * @param amount The amount to convert.
     * @return A string representation of the amount.
     */
    private fun amount2str(amount: Int): Component {
        return "<dark_purple>|</dark_purple><gray>${"%5d".format(amount)}x  </gray>".mm()
    }

    /**
     * Prints the unload summary for the specified player.
     * @param player The player to print the unload summary for.
     */
    fun print(player: Player) {
        val summary = unloadSummaries[player.uniqueId] ?: return
        player.sendMessage("<gray><b>Unload Summary:</b></gray>".mm())
        val separator = "<gray>${"-".repeat(20)}</gray>"
        player.sendMessage(separator.mm())
        player.sendMessage(loc2str(summary.playerLocation))
        summary.materials.forEach { (mat, amount) ->
            player.sendMessage(
                Component.join(
                    JoinConfiguration.noSeparators(),
                    amount2str(amount),
                    "<gold>${mat.name}</gold>".mm()
                )
            )
        }
    }

    /**
     * Saves the unload summary for the specified player.
     * @param player The player to save the unload summary for.
     * @param chests The list of chests involved on unload.
     * @param materials The map of materials and their amounts.
     */
    private fun save(
        player: Player,
        chests: List<Block>,
        materials: Map<Material, Int>
    ) {
        lastUnloads[player.uniqueId] = chests
        lastUnloadPositions[player.uniqueId] = player.location.clone().add(0.0, 0.75, 0.0)
        unloadSummaries[player.uniqueId] = InvUnloadSummaryData(
            chests = chests,
            materials = materials,
            playerLocation = player.location.clone()
        )
    }

    /**
     * Plays the unload effect for the specified player.
     * @param player The player to play the effect for.
     * @param affectedChests The list of chests to affect. If null, uses the last unloaded chests.
     */
    fun play(player: Player, affectedChests: List<Block>? = null) {
        val chests = affectedChests ?: lastUnloads[player.uniqueId] ?: return

        activeVisualizations[player.uniqueId] = instance.server.scheduler.scheduleSyncRepeatingTask(
            instance,
            { laserEffect(chests, player, 0.3, 2, Particle.CRIT, 0.001, 128) },
            0L,
            2L
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                activeVisualizations[player.uniqueId]?.let {
                    instance.server.scheduler.cancelTask(it)
                    activeVisualizations.remove(player.uniqueId)
                }
            },
            TimeUtils.seconds(5)
        )
    }

    /**
     * Creates a chest effect for the specified block and player.
     * @param block The block to create the laser effect towards.
     * @param player The player to create the laser effect for.
     */
    fun chestEffect(block: Block, player: Player) {
        player.spawnParticle(Particle.CRIT, Utils.getCenterOfBlock(block), 10, 0.0, 0.0, 0.0)
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

    /**
     * Cleans up the unload data for the specified player.
     * @param player The player to clean up the unload data for.
     */
    private fun cleanup(player: Player) {
        val uuid = player.uniqueId
        lastUnloads.remove(uuid)
        lastUnloadPositions.remove(uuid)
        activeVisualizations.remove(uuid)
        unloadSummaries.remove(uuid)
    }
}