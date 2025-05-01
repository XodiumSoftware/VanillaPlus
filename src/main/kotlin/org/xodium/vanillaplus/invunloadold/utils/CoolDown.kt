/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.command.CommandSender

object CoolDown {
    private val map = HashMap<CommandSender?, Long?>()

    @JvmStatic
    fun check(sender: CommandSender): Boolean {
        if (map.containsKey(sender)) {
            val lastTime: Long = map.get(sender)!!
            //System.out.println("lastTime: " + lastTime);
            val okayTime = lastTime + (Main.getInstance().getConfig()
                .getDouble("cooldown") * 1000).toLong()
            //System.out.println("okayTime: " + okayTime);
            //System.out.println("now Time: " + System.currentTimeMillis());
            val isOkay = System.currentTimeMillis() >= okayTime
            if (!isOkay) {
                sender.sendMessage(Main.getInstance().messages.MSG_COOLDOWN)
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
