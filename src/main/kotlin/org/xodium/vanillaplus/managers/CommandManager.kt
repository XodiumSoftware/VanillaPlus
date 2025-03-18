/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.managers

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.AutoRefillModule

/**
 * The `CommandManager` is responsible for managing and initializing commands in the VanillaPlus plugin.
 * It handles the registration of commands as Bukkit event listeners and ensures only enabled commands
 * are processed during the server startup phase.
 *
 * This object initializes its commands when the server starts and logs the loading time for each
 * enabled command for monitoring performance.
 */
object CommandManager {
    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal(instance.name.lowercase())
                    .requires { it.sender.hasPermission(Perms.Use.GENERAL) }
                    .executes(Command {
                        Utils.tryCatch(it) {
                            (it.sender as Player).sendMessage(
                                "$PREFIX v${instance.pluginMeta.version} | Click on me for more info!".mm()
                                    .clickEvent(ClickEvent.suggestCommand("/help ${instance.name.lowercase()}"))
                            )
                        }
                    }
//                    ).then(
//                        Commands.literal("autotool")
//                            .requires { it.sender.hasPermission(Perms.AutoTool.USE) }
//                            .executes(Command { Utils.tryCatch(it) { AutoToolModule().toggle(it.sender as Player) } })
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