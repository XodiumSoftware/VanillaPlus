/*
 * Copyright (c) 2025. Xodium.
 * All rights reserved.
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


/**
 * The `ReloadCommand` object represents a command handler for reloading the plugin configuration.
 * This command is registered during the plugin's lifecycle and can be executed by players
 * or the console to reload the plugin's configuration without restarting the server.
 *
 * The command only allows players with the appropriate permissions to execute the reload.
 * Upon successful execution, it notifies the sender with a confirmation message and logs
 * the reload action to the server console.
 *
 * Permissions:
 * - Requires the `<class_name_placeholder>.reload` permission to execute the reload command.
 */
object ReloadCommand {
    private val pcn: String = instance.javaClass.simpleName

    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(
                Commands.literal("vanillaplus")
                    .executes(Command { ctx: CommandContext<CommandSourceStack?>? ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        if (cs is Player && !cs.hasPermission("$pcn.reload")) {
                            cs.sendMessage(Utils.MM.deserialize("${VanillaPlus.PREFIX}<red>You do not have permission to use this command!"))
                            return@Command 0
                        }
                        instance.reloadConfig()
                        cs.sendMessage(Utils.MM.deserialize("${VanillaPlus.PREFIX}<green>Configuration reloaded successfully."))
                        instance.logger.info("Configuration reloaded successfully")
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the GUI",
                mutableListOf("vp")
            )
        }
    }
}
