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
                    .requires { cs -> cs.sender.hasPermission(Perms.Command.USE) }
                    .then(
                        Commands.literal("reload")
                            .requires { cs -> cs.sender.hasPermission(Perms.Command.RELOAD) }
                            .executes(Command { ctx ->
                                val sender = ctx?.source?.sender ?: return@Command 0
                                try {
                                    //TODO("Not yet implemented")
                                    //val pluginManager = instance.server.pluginManager
                                    //pluginManager.disablePlugin(instance)
                                    //pluginManager.enablePlugin(instance)
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
                            .requires { cs -> cs.sender.hasPermission(Perms.Command.Gui.FAQ) }
                            .executes(Command { ctx ->
                                val sender = ctx?.source?.sender ?: return@Command 0
                                try {
                                    gui.faqGUI().open(sender as Player)
                                } catch (e: Exception) {
                                    instance.logger.severe("Failed to open: ${e.message}")
                                    e.printStackTrace()
                                    sender.sendMessage("${VanillaPlus.PREFIX}<red>Failed to open. Check server logs for details.".mm())
                                }
                                Command.SINGLE_SUCCESS
                            })
                    )
                    .then(
                        Commands.literal("dims")
                            .requires { cs -> cs.sender.hasPermission(Perms.Command.Gui.DIMS) }
                            .executes(Command { ctx ->
                                val sender = ctx?.source?.sender ?: return@Command 0
                                try {
                                    gui.dimsGUI().open(sender as Player)
                                } catch (e: Exception) {
                                    instance.logger.severe("Failed to open: ${e.message}")
                                    e.printStackTrace()
                                    sender.sendMessage("${VanillaPlus.PREFIX}<red>Failed to open. Check server logs for details.".mm())
                                }
                                Command.SINGLE_SUCCESS
                            })
                    )
                    .then(
                        Commands.literal("settings")
                            .requires { cs -> cs.sender.hasPermission(Perms.Command.Gui.SETTINGS) }
                            .executes(Command { ctx ->
                                val sender = ctx?.source?.sender ?: return@Command 0
                                try {
                                    gui.settingsGUI().open(sender as Player)
                                } catch (e: Exception) {
                                    instance.logger.severe("Failed to open: ${e.message}")
                                    e.printStackTrace()
                                    sender.sendMessage("${VanillaPlus.PREFIX}<red>Failed to open. Check server logs for details.".mm())
                                }
                                Command.SINGLE_SUCCESS
                            })
                    )
                    .build(),
                "VanillaPlus plugin",
                mutableListOf("vp")
            )
        }
    }
}