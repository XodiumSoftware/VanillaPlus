@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.core.JsonProcessingException
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.modules.*
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import kotlin.io.path.exists
import kotlin.time.measureTime

/** Represents the module manager within the system. */
@Suppress("MemberVisibilityCanBePrivate")
internal object ModuleManager {
    val booksModule: BooksModule = BooksModule()
    val chatModule: ChatModule = ChatModule()
    val dimensionsModule: DimensionsModule = DimensionsModule()
    val invModule: InvModule = InvModule()
    val locatorModule: LocatorModule = LocatorModule()
    val mobsModule: MobsModule = MobsModule()
    val motdModule: MotdModule = MotdModule()
    val openableModule: OpenableModule = OpenableModule()
    val paintingModule: PaintingModule = PaintingModule()
    val petModule: PetModule = PetModule()
    val recipiesModule: RecipiesModule = RecipiesModule()
    val signModule: SignModule = SignModule()
    val silkTouchModule: SilkTouchModule = SilkTouchModule()
    val sitModule: SitModule = SitModule()
    val sleepModule: SleepModule = SleepModule()
    val tabListModule: TabListModule = TabListModule()
    val nicknameModule: NicknameModule = NicknameModule(tabListModule)
    val treesModule: TreesModule = TreesModule()
    val wanderingTraderModule: WanderingTraderModule = WanderingTraderModule()

    private val modules =
        listOf(
            booksModule,
            chatModule,
            dimensionsModule,
            invModule,
            locatorModule,
            mobsModule,
            motdModule,
            nicknameModule,
            openableModule,
            paintingModule,
            petModule,
            recipiesModule,
            signModule,
            silkTouchModule,
            sitModule,
            sleepModule,
            tabListModule,
            treesModule,
            wanderingTraderModule,
        )

    private val configCmd =
        CommandData(
            Commands
                .literal("vanillaplus")
                .then(
                    Commands
                        .literal("reload")
                        .requires { it.sender.hasPermission(configPerm) }
                        .executes { ctx ->
                            ctx.tryCatch {
                                updateConfig()
                                if (it.sender is Player) {
                                    it.sender.sendMessage("$PREFIX <green>Config reloaded successfully".mm())
                                }
                            }
                        },
                ),
            "Main VanillaPlus command. Use subcommands for actions.",
            listOf("vp"),
        )

    private val configPerm =
        Permission(
            "${instance::class.simpleName}.reload".lowercase(),
            "Allows use of the vanillaplus reload command",
            PermissionDefault.OP,
        )

    private val configsToSave: MutableMap<String, ModuleInterface.Config> = mutableMapOf()
    private val commandsToRegister: MutableList<CommandData> = mutableListOf()

    init {
        pluginManager()
        lifecycleManager()
    }

    /** Loads configs, registers modules' events and permissions, and collects commands. */
    private fun pluginManager() {
        updateConfig()
        commandsToRegister.add(configCmd)
        instance.server.pluginManager.addPermission(configPerm)
        modules.filter { it.enabled() }.forEach { module ->
            instance.logger.info(
                "Loaded: ${module::class.simpleName} | Took ${
                    measureTime {
                        instance.server.pluginManager.registerEvents(module, instance)
                        @Suppress("UnstableApiUsage")
                        instance.server.pluginManager.addPermissions(module.perms())
                        commandsToRegister.addAll(module.cmds())
                    }.inWholeMilliseconds
                }ms",
            )
        }
    }

    /** Registers all commands in commandsToRegister with the lifecycle manager. */
    private fun lifecycleManager() {
        commandsToRegister.takeIf { it.isNotEmpty() }?.let { cmds ->
            instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                cmds.forEach { commandData ->
                    event.registrar().register(
                        commandData.builder.build(),
                        commandData.description,
                        commandData.aliases.toMutableList(),
                    )
                }
            }
        }
    }

    /** Updates the config. */
    private fun updateConfig() {
        val allConfigsNode = ConfigManager.load()
        when {
            allConfigsNode != null -> instance.logger.info("Config: Loaded successfully")
            !ConfigManager.configPath.exists() -> instance.logger.info("Config: No config file found, a new one will be created")
            else -> instance.logger.warning("Config: Failed to load, using defaults")
        }

        modules.forEach { module ->
            val configKey = getConfigKey(module)
            allConfigsNode?.get(configKey)?.let { moduleConfigNode ->
                try {
                    ConfigManager.objectMapper.readerForUpdating(module.config).readValue(moduleConfigNode)
                } catch (e: JsonProcessingException) {
                    instance.logger.warning(
                        "Failed to parse config for ${module::class.simpleName}. Using defaults. Error: ${e.message}",
                    )
                }
            }
            configsToSave[configKey] = module.config
        }

        ConfigManager.save(configsToSave)
    }

    /**
     * Generates a configuration key for a module.
     * @param module The module to generate the key for.
     * @return The generated configuration key.
     */
    private fun getConfigKey(module: ModuleInterface<*>): String = module::class.simpleName!!
}
