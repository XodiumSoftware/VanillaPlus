/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.xodium.vanillaplus.invunloadold.Main
import java.util.*

class MaterialTabCompleter internal constructor(val main: Main) : TabCompleter {
    val mats: ArrayList<String>

    init {
        mats = ArrayList<String>()
        for (mat in Material.entries) {
            mats.add(mat.name)
        }
        mats.add("inv")
        mats.add("inventory")
        mats.add("hotbar")
    }


    override fun onTabComplete(
        commandSender: CommandSender,
        command: Command,
        s: String,
        args: Array<String>
    ): MutableList<String?>? {
        if (args.size == 0) return null

        val results = ArrayList<String?>()
        val lastArg = args[args.size - 1]

        for (mat in mats) {
            if (main.getConfig().getBoolean("strict-tabcomplete")) {
                if (mat.startsWith(lastArg.uppercase(Locale.getDefault()))) results.add(mat)
            } else {
                if (mat.contains(lastArg.uppercase(Locale.getDefault()))) results.add(mat)
            }
        }

        return results
    }
}
