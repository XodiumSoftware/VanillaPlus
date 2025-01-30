/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.GuiModule

object SettingsCommand {
    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(
                Commands.literal("settings")
                    .executes(Command { ctx: CommandContext<CommandSourceStack?>? ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        try {
                            GuiModule().faqGUI().open(cs as Player)
                        } catch (e: Exception) {
                            instance.logger.severe("Failed to open GUI: ${e.message}")
                            cs.sendMessage(Utils.MM.deserialize("${VanillaPlus.PREFIX}<red>Failed to open GUI. Check server logs for details."))
                        }
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the Settings GUI",
            )
        }
    }
}
