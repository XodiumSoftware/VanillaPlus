/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("unused")

package org.xodium.vanillaplus.managers

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.modules.*
import kotlin.reflect.full.memberProperties
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

    private val modules: List<ModuleInterface> by lazy {
        ModuleManager::class.memberProperties
            .filterNot { it.name == "modules" }
            .mapNotNull { it.get(this) as? ModuleInterface }
    }

    init {
        val commandsToRegister = mutableListOf<CommandData>()
        modules.filter { it.enabled() }.forEach { module ->
            instance.logger.info(
                "Loaded: ${module::class.simpleName} | Took ${
                    measureTime {
                        instance.server.pluginManager.registerEvents(module, instance)
                        @Suppress("UnstableApiUsage")
                        instance.server.pluginManager.addPermissions(module.perms())
                        module.cmds()?.let { commandsToRegister.add(it) }
                    }.inWholeMilliseconds
                }ms"
            )
        }
        commandsToRegister.takeIf { it.isNotEmpty() }?.let {
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