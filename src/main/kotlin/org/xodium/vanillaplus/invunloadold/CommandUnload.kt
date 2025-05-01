/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import net.md_5.bungee.api.ChatColor
import org.apache.commons.lang.StringUtils
import org.bukkit.Sound
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
import kotlin.collections.contains
import kotlin.ranges.contains
import kotlin.text.compareTo
import kotlin.text.contains
import kotlin.text.equals
import kotlin.text.lowercase
import kotlin.text.startsWith
import kotlin.text.uppercase

class CommandUnload(val main: Main) : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender, command: Command, label: String,
        args: Array<String>
    ): Boolean {
        /*		if(args.length > 1 && sender.hasPermission("invunload.others")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if(player == null) {
                        sender.sendMessage("Could not find player " + args[1]);
                        Bukkit.dispatchCommand(player,command.getName() + " " + args[0]);
                        return true;
                    }
                }*/

        //long startTime = System.nanoTime();

        if (args.size > 0 && args[0].equals("reload", ignoreCase = true)) {
            if (sender.hasPermission("invunload.reload")) {
                main.reloadCompleteConfig(true)
                sender.sendMessage(ChatColor.GREEN.toString() + "InvUnload has been reloaded.")
            } else {
                sender.sendMessage(main.getCommand("unload")!!.permissionMessage!!)
            }
            return true
        }

        if (sender !is Player) {
            return true
        }

        if (!CoolDown.check(sender)) {
            return true
        }

        val p = sender
        val setting: PlayerSetting? = main.getPlayerSetting(p)

        if (args.size > 0 && args[0].equals("hotbar", ignoreCase = true)) {
            if (command.name == "unload") {
                setting.unloadHotbar = !setting.unloadHotbar
                if (setting.unloadHotbar) {
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


        var radius: Int = main.groupUtils.getDefaultRadiusPerPlayer(p)
        var startSlot = 9
        val endSlot = 35
        var onlyMatchingStuff = false


        if (args.size > 0) {
            if (!StringUtils.isNumeric(args[0])) {
                p.sendMessage(main.messages.MSG_NOT_A_NUMBER)
                return true
            }
            val customRadius = args[0].toInt()
            if (customRadius > main.groupUtils.getMaxRadiusPerPlayer(p)) {
                p.sendMessage(
                    String.format(
                        main.messages.MSG_RADIUS_TOO_HIGH,
                        main.groupUtils.getMaxRadiusPerPlayer(p)
                    )
                )
                return true
            }
            radius = customRadius
        }


        if (command.name.equals("unload", ignoreCase = true)) {
            onlyMatchingStuff = true
            startSlot = if (setting.unloadHotbar) 0 else 9
        } else if (command.name.equals("dump", ignoreCase = true)) {
            onlyMatchingStuff = false
            startSlot = if (setting.dumpHotbar) 0 else 9
        }

        var chests: MutableList<Block?>? = BlockUtils.findChestsInRadius(p.location, radius)
        if (chests!!.size == 0) {
            p.sendMessage(main.messages.MSG_NO_CHESTS_NEARBY)
            return true
        }
        BlockUtils.sortBlockListByDistance(chests, p.location)

        val useableChests = ArrayList<Block>()
        for (block in chests) {
            if (PlayerUtils.canPlayerUseChest(block, p, main)) {
                useableChests.add(block!!)
            }
        }
        chests = null

        val affectedChests = ArrayList<Block?>()
        val summary = UnloadSummary()

        // Unload
        for (block in useableChests) {
            val inv: Inventory = (block.state as Container).inventory
            if (InvUtils.stuffInventoryIntoAnother(main, p, inv, true, startSlot, endSlot, summary)) {
                affectedChests.add(block)
            }
        }

        //Dump
        if (!onlyMatchingStuff) {
            for (block in useableChests) {
                val inv: Inventory = (block.state as Container).inventory
                if (InvUtils.stuffInventoryIntoAnother(main, p, inv, false, startSlot, endSlot, summary)) {
                    affectedChests.add(block)
                }
            }
        }
        if (main.getConfig().getBoolean("always-show-summary")) {
            summary.print(PrintRecipient.PLAYER, p)
        }

        if (affectedChests.size == 0) {
            val blackList: BlackList = main.getPlayerSetting(p).getBlacklist()
            // TODO: Fix this. Right now the blacklist message is disabled
            //boolean everythingBlackListed = true;
            var everythingBlackListed = false
            for (i in startSlot..endSlot) {
                val item: ItemStack? = p.inventory.getItem(i)
                if (item == null || item.getAmount() == 0 || item.getType() == Material.AIR) continue
                if (!blackList.contains(item.getType())) {
                    everythingBlackListed = false
                }
            }
            p.sendMessage(if (everythingBlackListed) main.messages.MSG_COULD_NOT_UNLOAD_BLACKLIST else main.messages.MSG_COULD_NOT_UNLOAD)
            return true
        }

        main.visualizer.save(p, affectedChests, summary)

        for (block in affectedChests) {
            main.visualizer.chestAnimation(block, p)
            if (main.getConfig().getBoolean("laser-animation")) {
                main.visualizer.play(p)
            }
            if (main.chestSortHook.shouldSort(p)) {
                main.chestSortHook.sort(block)
            }
        }

        if (main.getConfig().getBoolean("play-sound")) {
            if (main.getConfig().getBoolean("error-sound")) {
                main.logger.warning(
                    "Cannot play sound, because sound effect \"" + main.getConfig()
                        .getString("sound-effect") + "\" does not exist! Please check your config.yml"
                )
            } else {
                val sound = Sound.valueOf(main.getConfig().getString("sound-effect")!!.uppercase(Locale.getDefault()))
                p.playSound(p.location, sound, main.getConfig().getDouble("sound-volume", 1.0).toFloat(), 1f)
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
        val list: MutableList<kotlin.String?> = ArrayList<kotlin.String?>()
        list.add("hotbar")
        if (strings.size == 0) return list
        if ("hotbar".startsWith(strings[0].lowercase(Locale.getDefault()))) return list
        return null
    }
}
