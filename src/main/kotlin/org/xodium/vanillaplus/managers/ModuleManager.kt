/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.modules.*
import kotlin.time.measureTime

/** Represents the module manager within the system. */
@Suppress("MemberVisibilityCanBePrivate")
object ModuleManager {
    val autoRestartModule: AutoRestartModule = AutoRestartModule()
    val booksModule: BooksModule = BooksModule()
    val chatModule: ChatModule = ChatModule()
    val dimensionsModule: DimensionsModule = DimensionsModule()
    val doorsModule: DoorsModule = DoorsModule()
    val invSearchModule: InvSearchModule = InvSearchModule()
    val invUnloadModule: InvUnloadModule = InvUnloadModule()
    val joinQuitModule: JoinQuitModule = JoinQuitModule()
    val motdModule: MotdModule = MotdModule()
    val nicknameModule: NicknameModule = NicknameModule()
    val recipiesModule: RecipiesModule = RecipiesModule()
    val signModule: SignModule = SignModule()
    val tabListModule: TabListModule = TabListModule()
    val treesModule: TreesModule = TreesModule()
    val trowelModule: TrowelModule = TrowelModule()

    private val modules = listOf(
        autoRestartModule,
        booksModule,
        chatModule,
        dimensionsModule,
        doorsModule,
        invSearchModule,
        invUnloadModule,
        joinQuitModule,
        motdModule,
        nicknameModule,
        recipiesModule,
        signModule,
        tabListModule,
        treesModule,
        trowelModule,
    )

    private val commands = mutableListOf<CommandData>()

    init {
        modules()
        commands()
        permissions()
    }

    /** Registers the modules. */
    private fun modules() {
        modules.filter { it.enabled() }.forEach { module ->
            instance.logger.info(
                "Loaded: ${module::class.simpleName} | Took ${
                    measureTime {
                        instance.server.pluginManager.registerEvents(module, instance)
                        module.cmds()?.let { commands.add(it) }
                    }.inWholeMilliseconds
                }ms"
            )
        }
    }

    /** Registers commands for the modules. */
    private fun commands() {
        commands.takeIf { it.isNotEmpty() }?.let {
            @Suppress("UnstableApiUsage")
            instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                it.forEach { commandData ->
                    commandData.commands.forEach { command ->
                        event.registrar().register(
                            command.build(),
                            commandData.description,
                            commandData.aliases.toMutableList()
                        )
                    }
                }
            }
        }
    }

    /** Registers permissions for the modules. */
    private fun permissions() {
        modules.takeIf { it.isNotEmpty() }?.let {
            it.forEach { module ->
                module.perms().forEach { perm ->
                    instance.server.pluginManager.addPermission(perm)
                }
            }
        }
    }
}