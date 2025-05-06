/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.apache.commons.lang3.StringUtils
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils
import org.xodium.vanillaplus.utils.invunload.BlockUtils
import org.xodium.vanillaplus.utils.invunload.InvUtils
import org.xodium.vanillaplus.utils.invunload.PlayerUtils
import java.lang.String
import java.util.*
import kotlin.Boolean
import kotlin.Int
import kotlin.Suppress
import kotlin.collections.isEmpty
import kotlin.compareTo
import kotlin.text.isEmpty
import kotlin.text.toInt
import kotlin.text.uppercase

class InvSearchModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvSearchModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? {
        return Commands.literal("invsearch")
            .requires { it.sender.hasPermission(Perms.InvUnload.USE) }
            .executes { it -> Utils.tryCatch(it) { search(it.sender as Player) } }
    }

    private fun search(player: Player) {
        var radius: Int? = null
        var mat: Material? = null

        //TODO: Refactor to make use of the cmd() method
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
                        player.sendMessage("Invalid radius.")
                    }
                }
            }
        }

        if (args.size == 1) {
            if (StringUtils.isNumeric(args[0])) {
                radius = args[0].toInt()
                mat = player.inventory.itemInMainHand.type
            } else {
                mat = Material.getMaterial(args[0].uppercase(Locale.getDefault()))
            }
        }

        if (args.isEmpty()) mat = player.inventory.itemInMainHand.type

        if (mat == null) player.sendMessage("You must specify a valid material or hold something in your hand.")

        val chests = BlockUtils.findChestsInRadius(player.location, radius!!)
        BlockUtils.sortBlockListByDistance(chests, player.location)

        val useableChests = ArrayList<Block>()
        for (block in chests) if (PlayerUtils.canPlayerUseChest(block, player)) useableChests.add(block!!)

        if (useableChests.isEmpty()) player.sendMessage(String.format("", mat?.name))

        val affectedChests = ArrayList<Block>()
        val doubleChests = ArrayList<InventoryHolder?>()

        for (block in useableChests) {
            val inv = (block.state as Container).inventory
            if (inv.holder is DoubleChest) {
                val dc = inv.holder as DoubleChest?
                if (doubleChests.contains(dc?.leftSide)) continue
                doubleChests.add(dc?.leftSide)
            }
            if (InvUtils.searchItemInContainers(mat!!, inv, InvUnloadModule())) affectedChests.add(block)
        }

        InvUnloadModule().print(player)
        if (affectedChests.isEmpty()) player.sendMessage(String.format("", mat?.name))

        for (block in affectedChests) InvUnloadModule().chestEffect(block, player)
        InvUnloadModule().play(player, affectedChests)
    }
}