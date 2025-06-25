/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.modules.*
import kotlin.time.measureTime

/** Represents the module configuration data. */
data class AllConfigs(
    val autoRestart: AutoRestartModule.Config = AutoRestartModule.Config(),
    val books: BooksModule.Config = BooksModule.Config(),
    val chat: ChatModule.Config = ChatModule.Config(),
    val dimensions: DimensionsModule.Config = DimensionsModule.Config(),
    val doors: DoorsModule.Config = DoorsModule.Config(),
    val invSearch: InvSearchModule.Config = InvSearchModule.Config(),
    val invUnload: InvUnloadModule.Config = InvUnloadModule.Config(),
    val motd: MotdModule.Config = MotdModule.Config(),
    val nickname: NicknameModule.Config = NicknameModule.Config(),
    val recipies: RecipiesModule.Config = RecipiesModule.Config(),
    val sign: SignModule.Config = SignModule.Config(),
    val tabList: TabListModule.Config = TabListModule.Config(),
    val trees: TreesModule.Config = TreesModule.Config(),
    val trowel: TrowelModule.Config = TrowelModule.Config()
) : ModuleInterface.Config

/** Represents the module manager within the system. */
@Suppress("MemberVisibilityCanBePrivate")
object ModuleManager {
    val config: AllConfigs = AllConfigs()
    val autoRestartModule: AutoRestartModule = AutoRestartModule()
    val booksModule: BooksModule = BooksModule()
    val chatModule: ChatModule = ChatModule()
    val dimensionsModule: DimensionsModule = DimensionsModule()
    val doorsModule: DoorsModule = DoorsModule()
    val invSearchModule: InvSearchModule = InvSearchModule()
    val invUnloadModule: InvUnloadModule = InvUnloadModule()
    val motdModule: MotdModule = MotdModule()
    val recipiesModule: RecipiesModule = RecipiesModule()
    val signModule: SignModule = SignModule()
    val tabListModule: TabListModule = TabListModule()
    val nicknameModule: NicknameModule = NicknameModule(tabListModule)
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
        motdModule,
        nicknameModule,
        recipiesModule,
        signModule,
        tabListModule,
        treesModule,
        trowelModule
    )

    init {
        ConfigManager.load(config)
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
        //TODO: check if we can make this more compact.
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