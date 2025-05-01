/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import org.apache.commons.lang.StringUtils
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.xodium.vanillaplus.invunloadold.Main
import java.lang.String
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.text.format

class CommandSearchitem internal constructor(private val main: Main) : CommandExecutor {
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
                if (p.inventory.itemInMainHand != null && p.inventory.itemInMainHand
                        .type != null
                ) {
                    mat = p.inventory.itemInMainHand.type
                }
            } else {
                mat = Material.getMaterial(args[0].uppercase(Locale.getDefault()))
                radius = main.groupUtils.getDefaultRadiusPerPlayer(p)
            }
        }

        if (args.size == 0 && p.inventory.itemInMainHand != null) {
            mat = p.inventory.itemInMainHand.type
            radius = main.groupUtils.getDefaultRadiusPerPlayer(p)
        }

        if (mat == null) {
            p.sendMessage("You must specify a valid material or hold something in your hand.")
            return true
        }

        if (radius == null || radius > main.groupUtils.getMaxRadiusPerPlayer(p)) {
            p.sendMessage(String.format(main.messages.MSG_RADIUS_TOO_HIGH, main.groupUtils.getMaxRadiusPerPlayer(p)))
            return true
        }

        if (mat == null) {
            p.sendMessage(kotlin.String.format("%s is not a valid material.", args[0]))
            return true
        }

        var chests: MutableList<Block?>? = BlockUtils.findChestsInRadius(p.location, radius)
        BlockUtils.sortBlockListByDistance(chests, p.location)

        val useableChests = ArrayList<Block>()
        for (block in chests!!) {
            if (PlayerUtils.canPlayerUseChest(block, p, main)) {
                useableChests.add(block!!)
            }
        }

        if (useableChests.size == 0) {
            p.sendMessage(String.format(main.messages.MSG_NOTHING_FOUND, mat.name))
            return true
        }

        chests = null

        val affectedChests = ArrayList<Block?>()
        val doubleChests: ArrayList<InventoryHolder?> = ArrayList<InventoryHolder?>()
        val summary = UnloadSummary()
        for (block in useableChests) {
            val inv: Inventory = (block.state as Container).inventory

            if (inv.getHolder() is DoubleChest) {
                val dc: DoubleChest? = inv.getHolder() as DoubleChest?
                if (doubleChests.contains(dc.getLeftSide())) continue
                doubleChests.add(dc.getLeftSide())
            }

            if (InvUtils.searchItemInContainers(mat, inv, summary)) {
                affectedChests.add(block)
            }
        }

        summary.print(UnloadSummary.PrintRecipient.PLAYER, p)

        if (affectedChests.size == 0) {
            p.sendMessage(String.format(main.messages.MSG_NOTHING_FOUND, mat.name))
            return true
        }

        for (block in affectedChests) {
            main.visualizer.chestAnimation(block, p)
        }
        main.visualizer.play(affectedChests, p)

        return true
    }
}
