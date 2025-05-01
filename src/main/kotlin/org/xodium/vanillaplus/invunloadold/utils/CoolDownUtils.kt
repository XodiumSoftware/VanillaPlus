/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.command.CommandSender
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm

object CoolDownUtils {
    private val map = HashMap<CommandSender?, Long?>()

    @JvmStatic
    fun check(sender: CommandSender): Boolean {
        if (map.containsKey(sender)) {
            val lastTime: Long = map.get(sender)!!
            val okayTime = lastTime + (instance.config //TODO: use Config
                .getDouble("cooldown") * 1000).toLong()
            val isOkay = System.currentTimeMillis() >= okayTime
            if (!isOkay) {
                sender.sendMessage("".mm())
                return false
            }
            map.put(sender, System.currentTimeMillis())
            return true
        } else {
            map.put(sender, System.currentTimeMillis())
            return true
        }
    }
}
