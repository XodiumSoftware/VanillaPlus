/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.InvUnloadSummaryData
import org.xodium.vanillaplus.hooks.ChestSortHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils
import org.bukkit.Sound as BukkitSound

/** Represents a module handling inv-unload mechanics within the system. */
class InvUnloadModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvUnloadModule.ENABLED

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
        val chests = Utils.findBlocksInRadius(player.location, 5)

        if (chests.isEmpty()) {
            player.sendActionBar("No chests found nearby".fireFmt().mm())
            return
        }

        chests.sortBy { it.location.distance(player.location) }

        val useableChests = ArrayList<Block>()

        for (block in chests) if (Utils.canPlayerUseChest(block, player)) useableChests.add(block)

        val affectedChests = mutableListOf<Block>()

        for (block in useableChests) {
            val inv = (block.state as Container).inventory
            if (Utils.stuffInventoryIntoAnother(player, inv, true, startSlot, endSlot)) {
                affectedChests.add(block)
            }
        }

        if (!onlyMatchingStuff) {
            for (block in useableChests) {
                val inv = (block.state as Container).inventory
                if (Utils.stuffInventoryIntoAnother(player, inv, false, startSlot, endSlot)) {
                    affectedChests.add(block)
                }
            }
        }

        if (affectedChests.isEmpty()) {
            player.sendActionBar("No items were unloaded".fireFmt().mm())
            return
        }

        Utils.print(player)

        for (i in startSlot..endSlot) {
            val item = player.inventory.getItem(i)
            if (item == null || item.amount == 0 || item.type == Material.AIR) continue
        }

        val materials = mutableMapOf<Material, Int>()

        save(player, affectedChests, materials)

        for (block in affectedChests) {
            Utils.chestEffect(block, player)
            Utils.play(player)
            if (ChestSortHook.shouldSort(player)) ChestSortHook.sort(block)
        }

        player.playSound(
            Sound.sound(
                BukkitSound.BLOCK_CHEST_CLOSE,
                Sound.Source.MASTER,
                1.0f,
                1.0f
            )
        )
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
        Utils.lastUnloads[player.uniqueId] = chests
        Utils.lastUnloadPositions[player.uniqueId] = player.location.clone().add(0.0, 0.75, 0.0)
        Utils.unloadSummaries[player.uniqueId] = InvUnloadSummaryData(
            chests = chests,
            materials = materials,
            playerLocation = player.location.clone()
        )
    }

    /**
     * Cleans up the unload data for the specified player.
     * @param player The player to clean up the unload data for.
     */
    private fun cleanup(player: Player) {
        val uuid = player.uniqueId
        Utils.lastUnloads.remove(uuid)
        Utils.lastUnloadPositions.remove(uuid)
        Utils.activeVisualizations.remove(uuid)
        Utils.unloadSummaries.remove(uuid)
    }
}