/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.old

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class AutoToolNotifyEvent(player: Player, val block: Block?) : PlayerEvent(player) {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}