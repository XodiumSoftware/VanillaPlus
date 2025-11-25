@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.managers

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.modules.*
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.prefix
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import kotlin.time.measureTime

/** Represents the module manager within the system. */
@Suppress("MemberVisibilityCanBePrivate")
internal object ModuleManager {
    val booksModule: BooksModule = BooksModule()
    val chatModule: ChatModule = ChatModule()
    val dimensionsModule: DimensionsModule = DimensionsModule()
    val entityModule: EntityModule = EntityModule()
    val invModule: InvModule = InvModule()
    val locatorModule: LocatorModule = LocatorModule()
    val openableModule: OpenableModule = OpenableModule()
    val petModule: PetModule = PetModule()
    val playerModule: PlayerModule = PlayerModule()
    val recipeModule: RecipeModule = RecipeModule()
    val scoreBoardModule: ScoreBoardModule = ScoreBoardModule()
    val signModule: SignModule = SignModule()
    val sitModule: SitModule = SitModule()
    val tabListModule: TabListModule = TabListModule()
    val treesModule: TreesModule = TreesModule()

    private val modules =
        listOf(
            booksModule,
            chatModule,
            dimensionsModule,
            entityModule,
            invModule,
            locatorModule,
            openableModule,
            petModule,
            playerModule,
            recipeModule,
            scoreBoardModule,
            signModule,
            sitModule,
            tabListModule,
            treesModule,
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
                                ConfigManager.update(modules)
                                if (it.sender is Player) {
                                    it.sender.sendMessage("${instance.prefix} <green>Config reloaded successfully".mm())
                                }
                            }
                        },
                ),
            "Main VanillaPlus command. Use subcommands for actions.",
            listOf("vp"),
        )

    private val configPerm =
        Permission(
            "${instance.javaClass.simpleName}.reload".lowercase(),
            "Allows use of the vanillaplus reload command",
            PermissionDefault.OP,
        )

    private val commandsToRegister: MutableList<CommandData> = mutableListOf()

    init {
        pluginManager()
        lifecycleManager()
    }

    /** Loads configs, registers modules' events and permissions, and collects commands. */
    private fun pluginManager() {
        ConfigManager.update(modules)
        commandsToRegister.add(configCmd)
        instance.server.pluginManager.addPermission(configPerm)
        modules.filter { it.enabled() }.forEach { module ->
            instance.logger.info(
                "Loaded: ${module.javaClass.simpleName} | Took ${
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
}
