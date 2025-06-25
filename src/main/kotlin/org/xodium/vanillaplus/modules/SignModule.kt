/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.SignChangeEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm

class SignModule : ModuleInterface<SignModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = ConfigManager.data.signModule.enabled

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: SignChangeEvent) {
        if (!enabled()) return

        val lines = event.lines()
        for (i in lines.indices) {
            lines[i] = PlainTextComponentSerializer.plainText().serialize(lines[i]).mm()
        }
    }

    data class Config(
        override val enabled: Boolean = true
    ) : ModuleInterface.Config
}