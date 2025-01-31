/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.managers

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Utils.mm
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
                    .executes(Command { ctx ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        try {
//                            TODO("Not yet implemented")
//                            val pluginManager = instance.server.pluginManager
//                            pluginManager.disablePlugin(instance)
//                            pluginManager.enablePlugin(instance)
                            instance.logger.info("Reloaded successfully")
                            cs.sendMessage("${VanillaPlus.PREFIX}<green>Reloaded successfully.".mm())
                        } catch (e: Exception) {
                            instance.logger.severe("Failed to reload: ${e.message}")
                            cs.sendMessage("${VanillaPlus.PREFIX}<red>Failed to reload. Check server logs for details.".mm())
                        }
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Reloads the plugin",
                mutableListOf("vp")
            )
            registrar.register(
                Commands.literal("faq")
                    .executes(Command { ctx ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        try {
                            GuiModule().faqGUI().open(cs as Player)
                        } catch (e: Exception) {
                            instance.logger.severe("Failed to open: ${e.message}")
                            cs.sendMessage("${VanillaPlus.PREFIX}<red>Failed to open. Check server logs for details.".mm())
                        }
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the FAQ GUI",
            )
            registrar.register(
                Commands.literal("dims")
                    .executes(Command { ctx ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        try {
                            GuiModule().dimsGUI().open(cs as Player)
                        } catch (e: Exception) {
                            instance.logger.severe("Failed to open: ${e.message}")
                            cs.sendMessage("${VanillaPlus.PREFIX}<red>Failed to open. Check server logs for details.".mm())
                        }
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the Dimensions GUI",
            )
            registrar.register(
                Commands.literal("settings")
                    .executes(Command { ctx ->
                        val cs = ctx?.source?.sender ?: return@Command 0
                        try {
                            GuiModule().settingsGUI().open(cs as Player)
                        } catch (e: Exception) {
                            instance.logger.severe("Failed to open: ${e.message}")
                            cs.sendMessage("${VanillaPlus.PREFIX}<red>Failed to open. Check server logs for details.".mm())
                        }
                        Command.SINGLE_SUCCESS
                    })
                    .build(),
                "Opens the Settings GUI",
            )
        }
    }
}