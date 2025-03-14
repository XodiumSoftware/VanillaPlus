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
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.AutoRefillModule
import org.xodium.vanillaplus.modules.AutoToolModule

/**
 * Class for registering GUI commands.
 */
object CommandManager {
    init {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal(instance.name.lowercase())
                    .requires { it.sender.hasPermission(Perms.Use.GENERAL) }
                    .executes(Command { Utils.tryCatch(it) { TODO() } }
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