/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
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
                        @Suppress("UnstableApiUsage")
                        instance.server.pluginManager.addPermissions(module.perms())
                    }.inWholeMilliseconds
                }ms"
            )
        }
    }

    /** Registers commands for the modules. */
    private fun commands() {
        modules.filter { it.enabled() }.mapNotNull { it.cmds() }.takeIf { it.isNotEmpty() }?.let {
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
}