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
import org.xodium.vanillaplus.Gui
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.AutoRefillModule
import org.xodium.vanillaplus.modules.AutoToolModule
import org.xodium.vanillaplus.modules.SkinsModule

/**
 * Class for registering GUI commands.
 */
object CommandManager {
    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal(instance.name.lowercase())
                    .requires { it.sender.hasPermission(Perms.Use.GENERAL) }
                    .executes(Command { Utils.tryCatch(it) { Gui.faqGUI().open(it.sender as Player) } })
                    .then(
                        Commands.literal("faq")
                            .requires { it.sender.hasPermission(Perms.Gui.FAQ) }
                            .executes(Command { Utils.tryCatch(it) { Gui.faqGUI().open(it.sender as Player) } })
                    )
                    .then(
                        Commands.literal("dims")
                            .requires { it.sender.hasPermission(Perms.Gui.DIMS) }
                            .executes(Command {
                                Utils.tryCatch(it) {
                                    val player = it.sender as Player
                                    Gui.dimsGUI(player).open(player)
                                }
                            })
                    )
                    .then(
                        Commands.literal("settings")
                            .requires { it.sender.hasPermission(Perms.Gui.SETTINGS) }
                            .executes(Command { Utils.tryCatch(it) { Gui.settingsGUI().open(it.sender as Player) } })
                    ).then(
                        Commands.literal("skins")
                            .requires { it.sender.hasPermission(Perms.Gui.SKINS) }
                            .executes(Command {
                                Utils.tryCatch(it) {
                                    val player = it.sender as Player
                                    SkinsModule().gui(player).open(player)
                                }
                            })
                    ).then(
                        Commands.literal("autotool")
                            .requires { it.sender.hasPermission(Perms.AutoTool.USE) }
                            .executes(Command { Utils.tryCatch(it) { AutoToolModule().toggle(it.sender as Player) } })
                    ).then(
                        Commands.literal("autorefill")
                            .requires { it.sender.hasPermission(Perms.AutoRefill.USE) }
                            .executes(Command { Utils.tryCatch(it) { AutoRefillModule().toggle(it.sender as Player) } })
                    )
                    .build(),
                "${instance.name} plugin",
                mutableListOf("vp")
            )
        }
    }
}