/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.xodium.vanillaplus.invunloadold.BlackList
import org.xodium.vanillaplus.invunloadold.Main
import java.lang.String
import java.util.*
import java.util.stream.Collectors
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.arrayOf
import kotlin.arrayOfNulls
import kotlin.collections.contains
import kotlin.text.equals
import kotlin.text.format
import kotlin.text.lowercase
import kotlin.text.startsWith

class CommandBlacklist internal constructor(val main: Main) : CommandExecutor, TabCompleter {
    private fun inv2stringlist(inv: Inventory, startSlot: Int, endSlot: Int): ArrayList<String?> {
        val list = ArrayList<String?>()
        for (i in startSlot..endSlot) {
            if (inv.getItem(i) == null) continue
            if (!list.contains(inv.getItem(i)!!.type.name)) {
                list.add(inv.getItem(i)!!.type.name)
            }
        }
        return list
    }

    private fun matlist2string(list: MutableList<Material?>): String {
        return list.stream()
            .map<String?> { obj: Material? -> obj!!.name }
            .collect(Collectors.joining(", "))
    }

    override fun onCommand(
        commandSender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): Boolean {
        var args = args
        if (commandSender !is Player) {
            commandSender.sendMessage("This command is only available for players.")
            return true
        }

        val p = commandSender
        val b: BlackList = main.getPlayerSetting(p).getBlacklist()
        val currentItem = p.inventory.itemInMainHand

        val candidates = ArrayList<Material?>()
        val errors = ArrayList<String?>()
        ArrayList<Material?>()
        val successes = ArrayList<Material?>()

        val option: String?

        if (args.size == 0) {
            option = "show"
        } else {
            option = args[0].lowercase(Locale.getDefault())
            args[0] = null
        }

        when (option) {
            "show" -> {
                b.print(p, main)
                return true
            }

            "add", "remove" -> {
                if (args.size == 1) {
                    if (currentItem.type == Material.AIR) {
                        p.sendMessage(main.messages.BL_NOTHINGSPECIFIED)
                        return true
                    }
                    candidates.add(currentItem.type)
                } else if (args[1].equals("inv", ignoreCase = true)
                    || args[1].equals("inventory", ignoreCase = true)
                    || args[1].equals("hotbar", ignoreCase = true)
                ) {
                    val list = inv2stringlist(
                        p.inventory,
                        if (args[1].equals("hotbar", ignoreCase = true)) 0 else 9,
                        if (args[1].equals("hotbar", ignoreCase = true)) 8 else 35
                    )

                    val newArgs = arrayOfNulls<String>(1 + list.size)
                    newArgs[0] = args[0]
                    var i = 1
                    while (i < list.size + 1) {
                        newArgs[i] = list.get(i - 1)
                        i++
                    }
                    args = newArgs
                }


                for (s in args) {
                    if (s == null) continue
                    var m = Material.getMaterial(s.uppercase(Locale.getDefault()))
                    if (m == Material.AIR) m = null
                    if (m == null) {
                        errors.add(s)
                        continue
                    }
                    candidates.add(m)
                }


                for (mat in candidates) {
                    successes.add(mat)
                    if (option == "add") {
                        if (!b.contains(mat)) {
                            b.add(mat)
                        }
                    } else {
                        b.remove(mat)
                    }
                }

                if (errors.size > 0) {
                    p.sendMessage(String.format(main.messages.BL_INVALID, stringlist2string(errors)))
                }
                if (successes.size > 0) {
                    val message: kotlin.String
                    if (option == "add") {
                        message = main.messages.BL_ADDED
                    } else {
                        message = main.messages.BL_REMOVED
                    }
                    p.sendMessage(kotlin.String.format(message, matlist2string(successes)))
                }

                return true
            }

            "reset" -> {
                p.sendMessage(String.format(main.messages.BL_REMOVED, matlist2string(b.mats)))
                b.mats.clear()
                return true
            }

            else -> return false
        }
    }

    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        s: kotlin.String,
        args: Array<kotlin.String>
    ): MutableList<kotlin.String?>? {
        if (commandSender !is Player) return null

        val commands = arrayOf<kotlin.String>("show", "add", "remove", "reset")
        if (args.size == 0) return Arrays.asList<kotlin.String?>(*commands)
        val list = ArrayList<kotlin.String?>()

        if (args.size == 1) {
            for (string in commands) {
                if (string.lowercase(Locale.getDefault()).startsWith(args[0])) list.add(string)
            }
            return list
        }

        if (args.size >= 2 && args[0].equals("remove", ignoreCase = true)) {
            for (mat in main.getPlayerSetting(commandSender).getBlacklist().mats) {
                list.add(mat.name)
            }
            return list
        }

        if (args.size >= 2 && args[0] == "add") {
            return main.materialTabCompleter.onTabComplete(commandSender, command, s, args)
        }

        return null
    }

    private fun stringlist2string(list: MutableList<kotlin.String?>): kotlin.String {
        return String.join(", ", list)
    }
}
