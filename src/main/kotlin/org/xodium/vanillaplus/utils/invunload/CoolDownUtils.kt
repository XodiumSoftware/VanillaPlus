/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils.invunload

import org.bukkit.entity.Player
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

//TODO: Move to a more generic location.
object CoolDownUtils {
    private val map = mutableMapOf<Player, Long?>()

    /**
     * Checks if the player is on cooldown.
     * @param player The player to check.
     * @return true if the player is not on cooldown, false otherwise.
     */
    fun cooldown(player: Player): Boolean {
        return System.currentTimeMillis().let {
            if (it >= (map[player] ?: 0L) + Config.InvUnloadModule.COOLDOWN) {
                map[player] = it
                true
            } else {
                player.sendActionBar(("${"InvUnload:".fireFmt()} You must wait before using this again".mangoFmt()).mm())
                false
            }
        }
    }
}
