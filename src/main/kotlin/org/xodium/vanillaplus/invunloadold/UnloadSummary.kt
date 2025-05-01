/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.util.*

class UnloadSummary internal constructor() {
    private val unloads: HashMap<Location?, EnumMap<Material?, Int?>?> = HashMap<Location?, EnumMap<Material?, Int?>?>()

    fun protocolUnload(loc: Location?, mat: Material?, amount: Int) {
        if (amount == 0) return
        if (!unloads.containsKey(loc)) {
            unloads.put(loc, EnumMap<Material?, Int?>(Material::class.java))
            unloads.get(loc)!!.put(mat, amount)
        } else {
            if (unloads.get(loc)!!.containsKey(mat)) {
                unloads.get(loc)!!.put(mat, unloads.get(loc)!!.get(mat)!! + amount)
            } else {
                unloads.get(loc)!!.put(mat, amount)
            }
        }
    }

    private fun loc2str(loc: Location): String {
        val x = loc.blockX
        val y = loc.blockY
        val z = loc.blockZ
        var name: String? = loc.block.type.name
        val state = loc.world.getBlockAt(x, y, z).state
        if (state is Container) {
            val container = state
            if (container.customName != null) {
                name = container.customName
            }
        }
        return String.format(
            ChatColor.LIGHT_PURPLE.toString() + "§l%s   §r§a§lX: §f%d §a§lY: §f%d §a§lZ: §f%d",
            name,
            x,
            y,
            z
        )
    }

    private fun amount2str(amount: Int): String {
        return String.format(ChatColor.DARK_PURPLE.toString() + "|§7%5dx  ", amount)
    }

    fun print(recipient: PrintRecipient?, p: Player) {
        if (unloads.isNotEmpty()) printTo(recipient, p, " ")
        for (entry in unloads.entries) {
            printTo(recipient, p, " ")
            printTo(recipient, p, loc2str(entry.key!!))
            val map: EnumMap<Material?, Int?> = entry.value!!
            for (entry2 in map.entries) {
                printTo(
                    recipient, p,
                    amount2str(entry2.value!!) + ChatColor.GOLD + entry2.key!!.name
                )
            }
        }
    }

    enum class PrintRecipient {
        PLAYER, CONSOLE
    }

    private fun printTo(recipient: PrintRecipient?, p: Player, text: String) {
        if (recipient == PrintRecipient.CONSOLE) {
            instance.logger.info(text)
        } else {
            p.sendMessage(text)
        }
    }
}
