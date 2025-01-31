/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.managers

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.GuiModule

/**
 * Class for registering GUI commands.
 */
object CommandManager {
    private val gui = GuiModule()

    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal("vanillaplus")
                    .requires { it.sender.hasPermission(Perms.Command.USE) }
                    .then(
                        Commands.literal("reload")
                            .requires { it.sender.hasPermission(Perms.Command.RELOAD) }
                            .executes(Command { ctx ->
                                val sender = ctx?.source?.sender ?: return@Command 0
                                try {
                                    //TODO("Not yet implemented")
                                    instance.logger.info("Reloaded successfully")
                                    sender.sendMessage("${VanillaPlus.PREFIX}<green>Reloaded successfully.".mm())
                                } catch (e: Exception) {
                                    instance.logger.severe("Failed to reload: ${e.message}")
                                    e.printStackTrace()
                                    sender.sendMessage("${VanillaPlus.PREFIX}<red>Failed to reload. Check server logs for details.".mm())
                                }
                                Command.SINGLE_SUCCESS
                            })
                    )
                    .then(
                        Commands.literal("faq")
                            .requires { it.sender.hasPermission(Perms.Command.Gui.FAQ) }
                            .executes(Command { tryCatch(it) { gui.faqGUI().open(it) } })
                    )
                    .then(
                        Commands.literal("dims")
                            .requires { it.sender.hasPermission(Perms.Command.Gui.DIMS) }
                            .executes(Command { tryCatch(it) { gui.dimsGUI().open(it) } })
                    )
                    .then(
                        Commands.literal("settings")
                            .requires { it.sender.hasPermission(Perms.Command.Gui.SETTINGS) }
                            .executes(Command { tryCatch(it) { gui.settingsGUI().open(it) } })
                    )
                    .build(),
                "VanillaPlus plugin",
                mutableListOf("vp")
            )
        }
    }

    /**
     * Helper function to execute actions with standardized error handling.
     *
     * @param ctx The CommandContext to get the Player from.
     * @param action The action to execute, receiving a Player as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    private fun tryCatch(ctx: CommandContext<CommandSourceStack>, action: (Player) -> Unit): Int {
        val sender = ctx.source?.sender as Player
        return try {
            action(sender)
            Command.SINGLE_SUCCESS
        } catch (e: Exception) {
            instance.logger.severe("An Error has occured: ${e.message}")
            e.printStackTrace()
            sender.sendMessage("${VanillaPlus.PREFIX}<red>An Error has occured. Check server logs for details.".mm())
            Command.SINGLE_SUCCESS
        }
    }
}