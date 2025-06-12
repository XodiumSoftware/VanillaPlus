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
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.*
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.Utils
import kotlin.time.measureTime

/** Represents the module manager within the system. */
@Suppress("MemberVisibilityCanBePrivate")
object ModuleManager {
    val autoRestartModule: AutoRestartModule = AutoRestartModule()
    val booksModule: BooksModule = BooksModule()
    val dimensionsModule: DimensionsModule = DimensionsModule()
    val doorsModule: DoorsModule = DoorsModule()
    val invSearchModule: InvSearchModule = InvSearchModule()
    val invUnloadModule: InvUnloadModule = InvUnloadModule()
    val joinQuitModule: JoinQuitModule = JoinQuitModule()
    val motdModule: MotdModule = MotdModule()
    val recipiesModule: RecipiesModule = RecipiesModule()
    val tabListModule: TabListModule = TabListModule()
    val treesModule: TreesModule = TreesModule()

    private val modules = listOf(
        autoRestartModule,
        booksModule,
        dimensionsModule,
        doorsModule,
        invSearchModule,
        invUnloadModule,
        joinQuitModule,
        motdModule,
        recipiesModule,
        tabListModule,
        treesModule
    )

    @Suppress("UnstableApiUsage")
    private val commands = mutableListOf<LiteralArgumentBuilder<CommandSourceStack>>()

    init {
        modules()
        commands()
    }

    /** Registers the modules. */
    private fun modules() {
        modules.filter { it.enabled() }.forEach { module ->
            instance.logger.info(
                "Loaded: ${module::class.simpleName} | Took ${
                    measureTime {
                        instance.server.pluginManager.registerEvents(module, instance)
                        module.cmds()?.let { commands.addAll(it) }
                    }.inWholeMilliseconds
                }ms"
            )
        }
    }

    /** Registers commands for the modules. */
    private fun commands() {
        commands.add(ConfigManager.cmd())
        commands.takeIf { it.isNotEmpty() }?.let {
            @Suppress("UnstableApiUsage")
            instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                event.registrar().register(
                    Commands.literal(instance.name.lowercase())
                        .executes { it ->
                            Utils.tryCatch(it) {
                                (it.sender as Player).sendMessage(
                                    "$PREFIX v${instance.pluginMeta.version} | Click on me for more info!".mm()
                                        .clickEvent(ClickEvent.runCommand("/help ${instance.name.lowercase()}"))
                                )
                            }
                        }
                        .apply { commands.forEach(this::then) }
                        .build(),
                    "${instance.name} plugin",
                    mutableListOf("vp")
                )
            }
        }
    }
}