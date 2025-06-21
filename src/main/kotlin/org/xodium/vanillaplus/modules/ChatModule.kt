/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

class ChatModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.chatModule.enabled

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: AsyncChatEvent) {
        if (!enabled()) returna
        event.renderer { player, _, message, _ ->
            "<gold>[</gold><player><gold>]</gold> <message>".mangoFmt(true).mm(
                Placeholder.component("player", player.displayName()),
                Placeholder.component("message", message)
            )
        }
    }
}