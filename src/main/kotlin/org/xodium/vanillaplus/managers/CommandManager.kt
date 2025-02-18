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
import org.xodium.vanillaplus.modules.SkinsModule

/**
 * Class for registering GUI commands.
 */
object CommandManager {
    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal(instance.name.lowercase())
                    .requires { it.sender.hasPermission(Perms.VanillaPlus.USE) }
                    .then(
                        Commands.literal("faq")
                            .requires { it.sender.hasPermission(Perms.GuiModule.FAQ) }
                            .executes(Command { Utils.tryCatch(it) { Gui.faqGUI().open(it.sender as Player) } })
                    )
                    .then(
                        Commands.literal("dims")
                            .requires { it.sender.hasPermission(Perms.GuiModule.DIMS) }
                            .executes(Command { Utils.tryCatch(it) { Gui.dimsGUI().open(it.sender as Player) } })
                    )
                    .then(
                        Commands.literal("settings")
                            .requires { it.sender.hasPermission(Perms.GuiModule.SETTINGS) }
                            .executes(Command { Utils.tryCatch(it) { Gui.settingsGUI().open(it.sender as Player) } })
                    ).then(
                        Commands.literal("skins")
                            .requires { it.sender.hasPermission(Perms.GuiModule.SKINS) }
                            .executes(Command {
                                Utils.tryCatch(it) {
                                    SkinsModule().gui().open(it.sender as Player)
                                }
                            })
                    )
                    .build(),
                "${instance.name} plugin",
                mutableListOf("vp")
            )
        }
    }
}