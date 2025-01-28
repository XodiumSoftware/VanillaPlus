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
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.VanillaPlus.Companion.instance


/**
 * The `ReloadCommand` object represents a command handler for reloading the plugin.
 * This command is registered during the plugin's lifecycle and can be executed by players
 * or the console to reload the plugin without restarting the server.
 *
 * Upon successful execution, it notifies the sender with a confirmation message and logs
 * the reload action to the server console.
 */
object ReloadCommand {
    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(
                Commands.literal("vanillaplus")
                    .executes(Command { ctx: CommandContext<CommandSourceStack?>? ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        val pluginManager = instance.server.pluginManager
                        pluginManager.disablePlugin(instance)
                        pluginManager.enablePlugin(instance)
                        instance.logger.info("Reloaded successfully")
                        cs.sendMessage(Utils.MM.deserialize("${VanillaPlus.PREFIX}<green>Reloaded successfully."))
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the GUI",
                mutableListOf("vp")
            )
        }
    }
}
