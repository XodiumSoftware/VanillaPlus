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
import org.xodium.vanillaplus.invunloadold.PlayerSetting
import org.xodium.vanillaplus.invunloadold.UnloadSummary
import org.xodium.vanillaplus.invunloadold.Visualizer
import org.xodium.vanillaplus.invunloadold.utils.*
import java.lang.String
import java.util.*
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.compareTo
import kotlin.let
import kotlin.text.equals
import kotlin.text.lowercase
import kotlin.text.startsWith
import kotlin.text.toFloat
import kotlin.text.uppercase

class CommandUnload() : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String,
        args: Array<String>
    ): Boolean {
        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
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
        val setting: PlayerSetting? = instance.getPlayerSetting(p)

        if (args.isNotEmpty() && args[0].equals("hotbar", ignoreCase = true)) {
            if (command.name == "unload") {
                setting?.unloadHotbar = !setting.unloadHotbar
                if (setting.unloadHotbar) {
                    p.sendMessage(
                        String.format(
                            instance.messages.MSG_WILL_USE_HOTBAR,
                            "/" + label.lowercase(Locale.getDefault())
                        )
                    )
                } else {
                    p.sendMessage(
                        String.format(
                            instance.messages.MSG_WILL_NOT_USE_HOTBAR,
                            "/" + label.lowercase(Locale.getDefault())
                        )
                    )
                }
            } else if (command.name == "dump") {
                setting.dumpHotbar = !setting.dumpHotbar
                if (setting.dumpHotbar) {
                    p.sendMessage(
                        String.format(
                            main.messages.MSG_WILL_USE_HOTBAR,
                            "/" + label.lowercase(Locale.getDefault())
                        )
                    )
                } else {
                    p.sendMessage(
                        String.format(
                            main.messages.MSG_WILL_NOT_USE_HOTBAR,
                            "/" + label.lowercase(Locale.getDefault())
                        )
                    )
                }
            }
            return true
        }

        var radius: Int = GroupUtils().getDefaultRadiusPerPlayer(p)
        var startSlot = 9
        val endSlot = 35
        var onlyMatchingStuff = false

        if (args.isNotEmpty()) {
            if (!StringUtils.isNumeric(args[0])) {
                p.sendMessage(Messages.MSG_NOT_A_NUMBER)
                return true
            }
            val customRadius = args[0].toInt()
            if (customRadius > GroupUtils().getMaxRadiusPerPlayer(p)) {
                p.sendMessage(
                    String.format(
                        Messages.MSG_RADIUS_TOO_HIGH,
                        GroupUtils().getMaxRadiusPerPlayer(p)
                    )
                )
                return true
            }
            radius = customRadius
        }


        if (command.name.equals("unload", ignoreCase = true)) {
            onlyMatchingStuff = true
            setting?.let { startSlot = if (it.unloadHotbar) 0 else 9 }
        } else if (command.name.equals("dump", ignoreCase = true)) {
            onlyMatchingStuff = false
            setting?.let { startSlot = if (it.dumpHotbar) 0 else 9 }
        }

        val chests: MutableList<Block?>? = BlockUtils.findChestsInRadius(p.location, radius)
        if (chests!!.isEmpty()) {
            p.sendMessage(Messages.MSG_NO_CHESTS_NEARBY)
            return true
        }
        BlockUtils.sortBlockListByDistance(chests, p.location)

        val useableChests = ArrayList<Block>()
        for (block in chests) {
            if (PlayerUtils.canPlayerUseChest(block, p, main)) {
                useableChests.add(block!!)
            }
        }

        val affectedChests = ArrayList<Block?>()
        val summary = UnloadSummary()

        for (block in useableChests) {
            val inv: Inventory = (block.state as Container).inventory
            if (InvUtils.stuffInventoryIntoAnother(main, p, inv, true, startSlot, endSlot, summary)) {
                affectedChests.add(block)
            }
        }

        if (!onlyMatchingStuff) {
            for (block in useableChests) {
                val inv: Inventory = (block.state as Container).inventory
                if (InvUtils.stuffInventoryIntoAnother(main, p, inv, false, startSlot, endSlot, summary)) {
                    affectedChests.add(block)
                }
            }
        }
        if (main.config.getBoolean("always-show-summary")) {
            summary.print(UnloadSummary.PrintRecipient.PLAYER, p)
        }

        if (affectedChests.isEmpty()) {
            for (i in startSlot..endSlot) {
                val item: ItemStack? = p.inventory.getItem(i)
                if (item == null || item.amount == 0 || item.type == Material.AIR) continue
            }
            p.sendMessage(main.messages.MSG_COULD_NOT_UNLOAD)
            return true
        }

        Visualizer().save(p, affectedChests, summary)

        for (block in affectedChests) {
            Visualizer().chestAnimation(block, p)
            if (main.config.getBoolean("laser-animation")) {
                Visualizer().play(p)
            }
            if (ChestSortHook.shouldSort(p)) {
                ChestSortHook.sort(block!!)
            }
        }

        if (main.config.getBoolean("play-sound")) {
            if (main.config.getBoolean("error-sound")) {
                main.logger.warning(
                    "Cannot play sound, because sound effect \"" + main.config
                        .getString("sound-effect") + "\" does not exist! Please check your config.yml"
                )
            } else {
                val sound = Sound.valueOf(main.config.getString("sound-effect")!!.uppercase(Locale.getDefault()))
                p.playSound(p.location, sound, main.config.getDouble("sound-volume", 1.0).toFloat(), 1f)
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
