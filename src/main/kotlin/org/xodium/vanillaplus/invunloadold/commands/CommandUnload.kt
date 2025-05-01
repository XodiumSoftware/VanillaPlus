/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.commands

import net.md_5.bungee.api.ChatColor
import org.apache.commons.lang3.StringUtils
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.hooks.ChestSortHook
import org.xodium.vanillaplus.invunloadold.UnloadSummary
import org.xodium.vanillaplus.invunloadold.Visualizer
import org.xodium.vanillaplus.invunloadold.utils.*
import java.lang.String
import java.util.*
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.text.compareTo
import kotlin.text.lowercase
import kotlin.text.startsWith
import kotlin.text.uppercase

//TODO: convert to new command system.
class CommandUnload() : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String,
        args: Array<String>
    ): Boolean {
        if (args.isNotEmpty() && args[0].equals("reload")) {
            if (sender.hasPermission("invunload.reload")) {
                sender.sendMessage(ChatColor.GREEN.toString() + "InvUnload has been reloaded.")
            } else {
                sender.sendMessage(instance.getCommand("unload")!!.permissionMessage!!)
            }
            return true
        }

        if (sender !is Player) return true
        if (!CoolDownUtils.cooldown(sender)) return true

        val p = sender
        var radius: Int = GroupUtils().getDefaultRadiusPerPlayer(p)
        val startSlot = 9
        val endSlot = 35
        val onlyMatchingStuff = false

        if (args.isNotEmpty()) {
            if (!StringUtils.isNumeric(args[0])) {
                p.sendMessage("")
                return true
            }
            val customRadius = args[0].toInt()
            if (customRadius > GroupUtils().getMaxRadiusPerPlayer(p)) {
                p.sendMessage(
                    String.format(
                        "",
                        GroupUtils().getMaxRadiusPerPlayer(p)
                    )
                )
                return true
            }
            radius = customRadius
        }

        val chests: MutableList<Block?>? = BlockUtils.findChestsInRadius(p.location, radius)
        if (chests!!.isEmpty()) {
            p.sendMessage("")
            return true
        }
        BlockUtils.sortBlockListByDistance(chests, p.location)

        val useableChests = ArrayList<Block>()
        for (block in chests) if (PlayerUtils.canPlayerUseChest(block, p)) useableChests.add(block!!)

        val affectedChests = ArrayList<Block?>()
        val summary = UnloadSummary()

        for (block in useableChests) {
            val inv: Inventory = (block.state as Container).inventory
            if (InvUtils.stuffInventoryIntoAnother(p, inv, true, startSlot, endSlot, summary)) {
                affectedChests.add(block)
            }
        }

        if (!onlyMatchingStuff) {
            for (block in useableChests) {
                val inv: Inventory = (block.state as Container).inventory
                if (InvUtils.stuffInventoryIntoAnother(p, inv, false, startSlot, endSlot, summary)) {
                    affectedChests.add(block)
                }
            }
        }
        if (instance.config.getBoolean("always-show-summary")) { //TODO: use Config.
            summary.print(UnloadSummary.PrintRecipient.PLAYER, p)
        }

        if (affectedChests.isEmpty()) {
            for (i in startSlot..endSlot) {
                val item: ItemStack? = p.inventory.getItem(i)
                if (item == null || item.amount == 0 || item.type == Material.AIR) continue
            }
            p.sendMessage("")
            return true
        }

        Visualizer.save(p, affectedChests, summary)

        for (block in affectedChests) {
            Visualizer.chestAnimation(block, p)
            if (instance.config.getBoolean("laser-animation")) Visualizer.play(p)
            if (ChestSortHook.shouldSort(p)) ChestSortHook.sort(block!!)
        }

        if (instance.config.getBoolean("play-sound")) {
            if (instance.config.getBoolean("error-sound")) {
                instance.logger.warning(
                    "Cannot play sound, because sound effect \"" + instance.config
                        .getString("sound-effect") + "\" does not exist! Please check your config.yml"
                )
            } else {
                val sound = Sound.valueOf(instance.config.getString("sound-effect")!!.uppercase(Locale.getDefault()))
                p.playSound(p.location, sound, instance.config.getDouble("sound-volume", 1.0).toFloat(), 1f)
            }
        }

        return true
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        s: kotlin.String,
        strings: Array<kotlin.String>
    ): MutableList<kotlin.String?>? {
        if (strings.size > 1) return null
        val list: MutableList<kotlin.String?> = ArrayList()
        list.add("hotbar")
        if (strings.isEmpty()) return list
        if ("hotbar".startsWith(strings[0].lowercase(Locale.getDefault()))) return list
        return null
    }
}
