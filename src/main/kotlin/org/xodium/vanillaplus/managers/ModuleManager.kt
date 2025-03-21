/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.managers

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.*
import kotlin.time.measureTime

/**
 * The `ModuleManager` is responsible for managing and initializing modules in the VanillaPlus plugin.
 * It handles the registration of modules as Bukkit event listeners and ensures only enabled modules
 * are processed during the server startup phase.
 *
 * This object initializes its modules when the server starts and logs the loading time for each
 * enabled module for monitoring performance.
 */
object ModuleManager {
    /**
     * A list of command builders for the modules.
     */
    private val commandBuilders = mutableListOf<LiteralArgumentBuilder<CommandSourceStack>>()

    /**
     * Initializes the modules in the VanillaPlus plugin.
     */
    init {
        listOf(
            AutoRefillModule(),
            AutoRestartModule(),
            AutoToolModule(),
            BooksModule(),
            DimensionsModule(),
            DoorsModule(),
            InvUnloadModule(),
            MotdModule(),
            RecipiesModule(),
            TabListModule(),
            TreesModule(),
        ).filter { it.enabled() }
            .forEach { module ->
                instance.logger.info(
                    "Loaded: ${module::class.simpleName} | Took ${
                        measureTime {
                            instance.server.pluginManager.registerEvents(module, instance)
                            module.cmd()?.let { commandBuilders.add(it) }
                        }.inWholeMilliseconds
                    }ms"
                )
            }
        commandBuilders.takeIf { it.isNotEmpty() }?.let {
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
                        })
                        .apply { commandBuilders.forEach(this::then) }
                        .build(),
                    "${instance.name} plugin",
                    mutableListOf("vp")
                )
            }
        }
    }
}