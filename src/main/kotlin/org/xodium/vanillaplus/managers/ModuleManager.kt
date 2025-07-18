package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.databind.JsonNode
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
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
    val invModule: InvModule = InvModule()
    val locatorModule: LocatorModule = LocatorModule()
    val motdModule: MotdModule = MotdModule()
    val petModule: PetModule = PetModule()
    val recipiesModule: RecipiesModule = RecipiesModule()
    val signModule: SignModule = SignModule()
    val sitModule: SitModule = SitModule()
    val sleepModule: SleepModule = SleepModule()
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
        invModule,
        locatorModule,
        motdModule,
        nicknameModule,
        petModule,
        recipiesModule,
        signModule,
        sitModule,
        sleepModule,
        tabListModule,
        treesModule,
        trowelModule
    )

    init {
        val allConfigsNode: JsonNode? = ConfigManager.load()
        val configsToSave = mutableMapOf<String, ModuleInterface.Config>()
        modules.forEach { module ->
            val configKey = module::class.simpleName!!.removeSuffix("Module").replaceFirstChar { it.lowercase() }
            allConfigsNode?.get(configKey)?.let { moduleConfigNode ->
                try {
                    ConfigManager.objectMapper.readerForUpdating(module.config).readValue(moduleConfigNode)
                } catch (e: Exception) {
                    instance.logger.warning("Failed to parse config for ${module::class.simpleName}. Using defaults. Error: ${e.message}")
                }
            }
            configsToSave[configKey] = module.config
        }
        ConfigManager.save(configsToSave)

        val commandsToRegister = mutableListOf<CommandData>()
        commandsToRegister.addAll(ConfigManager.cmds())
        @Suppress("UnstableApiUsage")
        instance.server.pluginManager.addPermissions(ConfigManager.perms())
        modules.filter { it.enabled() }.forEach { module ->
            instance.logger.info(
                "Loaded: ${module::class.simpleName} | Took ${
                    measureTime {
                        instance.server.pluginManager.registerEvents(module, instance)
                        @Suppress("UnstableApiUsage")
                        instance.server.pluginManager.addPermissions(module.perms())
                        commandsToRegister.addAll(module.cmds())
                    }.inWholeMilliseconds
                }ms"
            )
        }
        commandsToRegister.takeIf { it.isNotEmpty() }?.let { cmds ->
            instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                cmds.forEach { commandData ->
                    event.registrar().register(
                        commandData.builder.build(),
                        commandData.description,
                        commandData.aliases.toMutableList()
                    )
                }
            }
        }
    }
}