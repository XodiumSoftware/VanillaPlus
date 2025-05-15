/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.*
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.Utils
import kotlin.time.measureTime

/** Represents the module manager within the system. */
object ModuleManager {
    @Suppress("UnstableApiUsage")
    private val commandBuilders = mutableListOf<LiteralArgumentBuilder<CommandSourceStack>>()

    init {
        listOf(
            AutoRefillModule(),
            AutoRestartModule(),
            AutoToolModule(),
            BooksModule(),
            BroadcastModule(),
            DimensionsModule(),
            DoorsModule(),
            EclipseModule(),
            InvSearchModule(),
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
                            module.cmd()?.let { commandBuilders.addAll(it) }
                        }.inWholeMilliseconds
                    }ms"
                )
            }
        commandBuilders.takeIf { it.isNotEmpty() }?.let {
            @Suppress("UnstableApiUsage")
            instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                event.registrar().register(
                    Commands.literal(instance.name.lowercase())
                        .requires { it.sender.hasPermission(Perms.Use.GENERAL) }
                        .executes { it ->
                            Utils.tryCatch(it) {
                                (it.sender as Player).sendMessage(
                                    "${PREFIX}v${instance.pluginMeta.version} | Click on me for more info!".mm()
                                        .clickEvent(ClickEvent.suggestCommand("/help ${instance.name.lowercase()}"))
                                )
                            }
                        }
                        .apply { commandBuilders.forEach(this::then) }
                        .build(),
                    "${instance.name} plugin",
                    mutableListOf("vp")
                )
            }
        }
    }
}