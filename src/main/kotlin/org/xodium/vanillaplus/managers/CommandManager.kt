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
import org.xodium.vanillaplus.Utils
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
                    .requires { it.sender.hasPermission(Perms.VanillaPlus.USE) }
                    .then(
                        Commands.literal("reload")
                            .requires { it.sender.hasPermission(Perms.VanillaPlus.RELOAD) }
                            .executes(Command { ctx ->
                                Utils.tryCatch(ctx) {
//                                    TODO: implement functionality.
                                    instance.logger.info("Reloaded successfully")
                                    (ctx.source.sender as Player).sendMessage("${VanillaPlus.PREFIX}<green>Reloaded successfully.".mm())
                                }
                            })
                    )
                    .then(
                        Commands.literal("faq")
                            .requires { it.sender.hasPermission(Perms.GuiModule.FAQ) }
                            .executes(Command { Utils.tryCatch(it) { gui.faqGUI().open(it.sender as Player) } })
                    )
                    .then(
                        Commands.literal("dims")
                            .requires { it.sender.hasPermission(Perms.GuiModule.DIMS) }
                            .executes(Command { Utils.tryCatch(it) { gui.dimsGUI().open(it.sender as Player) } })
                    )
                    .then(
                        Commands.literal("settings")
                            .requires { it.sender.hasPermission(Perms.GuiModule.SETTINGS) }
                            .executes(Command { Utils.tryCatch(it) { gui.settingsGUI().open(it.sender as Player) } })
                    )
                    .build(),
                "VanillaPlus plugin",
                mutableListOf("vp")
            )
        }
    }
}