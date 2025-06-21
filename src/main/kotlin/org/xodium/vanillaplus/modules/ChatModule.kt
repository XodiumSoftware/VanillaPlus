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

class ChatModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.chatModule.enabled

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: AsyncChatEvent) {
        if (!enabled()) return
        event.renderer { _, displayName, message, _ ->
            ConfigManager.data.chatModule.chatFormat.mm(
                Placeholder.component("player", displayName),
                Placeholder.component("message", message),
            )
        }
    }
}