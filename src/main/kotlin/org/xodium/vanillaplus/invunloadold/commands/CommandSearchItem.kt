/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.commands

import org.apache.commons.lang3.StringUtils
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.invunloadold.UnloadSummary
import org.xodium.vanillaplus.invunloadold.Visualizer
import org.xodium.vanillaplus.invunloadold.utils.BlockUtils
import org.xodium.vanillaplus.invunloadold.utils.InvUtils
import org.xodium.vanillaplus.invunloadold.utils.PlayerUtils
import java.lang.String
import java.util.*
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.text.toInt
import kotlin.text.uppercase

//TODO: convert to new command system.
class CommandSearchItem : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to run this command.")
            return true
        }

        val p = sender
        var radius: Int? = null
        var mat: Material? = null

        if (args.size >= 2) {
            if (StringUtils.isNumeric(args[0])) {
                radius = args[0].toInt()
                if (Material.getMaterial(args[1].uppercase(Locale.getDefault())) != null) {
                    mat = Material.getMaterial(args[1].uppercase(Locale.getDefault()))
                }
            } else {
                if (Material.getMaterial(args[0].uppercase(Locale.getDefault())) != null) {
                    mat = Material.getMaterial(args[0].uppercase(Locale.getDefault()))
                    if (StringUtils.isNumeric(args[1])) {
                        radius = args[1].toInt()
                    } else {
                        p.sendMessage("Invalid radius.")
                        return true
                    }
                }
            }
        }

        if (args.size == 1) {
            if (StringUtils.isNumeric(args[0])) {
                radius = args[0].toInt()
                mat = p.inventory.itemInMainHand.type
            } else {
                mat = Material.getMaterial(args[0].uppercase(Locale.getDefault()))
                radius = GroupUtils().getDefaultRadiusPerPlayer(p)
            }
        }

        if (args.isEmpty()) {
            mat = p.inventory.itemInMainHand.type
            radius = GroupUtils().getDefaultRadiusPerPlayer(p)
        }

        if (mat == null) {
            p.sendMessage("You must specify a valid material or hold something in your hand.")
            return true
        }

        if (radius == null || radius > GroupUtils().getMaxRadiusPerPlayer(p)) {
            p.sendMessage(String.format("", GroupUtils().getMaxRadiusPerPlayer(p)))
            return true
        }

        val chests: MutableList<Block?>? = BlockUtils.findChestsInRadius(p.location, radius)
        BlockUtils.sortBlockListByDistance(chests!!, p.location)

        val useableChests = ArrayList<Block>()
        for (block in chests) {
            if (PlayerUtils.canPlayerUseChest(block, p, instance)) {
                useableChests.add(block!!)
            }
        }

        if (useableChests.isEmpty()) {
            p.sendMessage(String.format("", mat.name))
            return true
        }

        val affectedChests = ArrayList<Block>()
        val doubleChests: ArrayList<InventoryHolder?> = ArrayList()
        val summary = UnloadSummary()
        for (block in useableChests) {
            val inv: Inventory = (block.state as Container).inventory
            if (inv.holder is DoubleChest) {
                val dc: DoubleChest? = inv.holder as DoubleChest?
                if (doubleChests.contains(dc?.leftSide)) continue
                doubleChests.add(dc?.leftSide)
            }
            if (InvUtils.searchItemInContainers(mat, inv, summary)) {
                affectedChests.add(block)
            }
        }

        summary.print(UnloadSummary.PrintRecipient.PLAYER, p)
        if (affectedChests.isEmpty()) {
            p.sendMessage(String.format("", mat.name))
            return true
        }

        for (block in affectedChests) {
            Visualizer.chestAnimation(block, p)
        }
        Visualizer.play(affectedChests, p)
        return true
    }
}
