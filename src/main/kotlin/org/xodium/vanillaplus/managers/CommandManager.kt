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
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.GuiModule

/**
 * Class for registering GUI commands.
 */
object CommandManager {
    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            registrar.register(
                Commands.literal("vanillaplus")
                    .executes(Command { ctx: CommandContext<CommandSourceStack?>? ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        try {
                            TODO("Not yet implemented")
//                            val pluginManager = instance.server.pluginManager
//                            pluginManager.disablePlugin(instance)
//                            pluginManager.enablePlugin(instance)
                            instance.logger.info("Reloaded successfully")
                            cs.sendMessage(Utils.MM.deserialize("${VanillaPlus.PREFIX}<green>Reloaded successfully."))
                        } catch (e: Exception) {
                            instance.logger.severe("Failed to reload: ${e.message}")
                            cs.sendMessage(Utils.MM.deserialize("${VanillaPlus.PREFIX}<red>Failed to reload. Check server logs for details."))
                        }
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Reloads the VanillaPlus plugin",
                mutableListOf("vp")
            )
            registrar.register(
                Commands.literal("faq")
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
                "Opens the FAQ GUI",
            )
            registrar.register(
                Commands.literal("dims")
                    .executes(Command { ctx: CommandContext<CommandSourceStack?>? ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        try {
                            GuiModule().dimsGUI().open(cs as Player)
                        } catch (e: Exception) {
                            instance.logger.severe("Failed to open GUI: ${e.message}")
                            cs.sendMessage(Utils.MM.deserialize("${VanillaPlus.PREFIX}<red>Failed to open GUI. Check server logs for details."))
                        }
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the Dimensions GUI",
            )
            registrar.register(
                Commands.literal("settings")
                    .executes(Command { ctx: CommandContext<CommandSourceStack?>? ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        try {
                            GuiModule().settingsGUI().open(cs as Player)
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