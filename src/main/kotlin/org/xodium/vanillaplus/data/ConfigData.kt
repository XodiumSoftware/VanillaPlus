@file:Suppress("ktlint:standard:no-wildcard-imports")
@file:OptIn(ExperimentalSerializationApi::class)

package org.xodium.vanillaplus.data

import io.papermc.paper.command.brigadier.Commands
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.configData
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.*
import org.xodium.vanillaplus.strategies.CapitalizedStrategy
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.prefix
import kotlin.io.path.*
import kotlin.time.measureTime

/** Configuration data for the plugin. */
@Serializable
internal data class ConfigData(
    var armorStandModule: ArmorStandModule.Config = ArmorStandModule.Config(),
    var booksModule: BooksModule.Config = BooksModule.Config(),
    var chatModule: ChatModule.Config = ChatModule.Config(),
    var dimensionsModule: DimensionsModule.Config = DimensionsModule.Config(),
    var entityModule: EntityModule.Config = EntityModule.Config(),
    var inventoryModule: InventoryModule.Config = InventoryModule.Config(),
    var locatorModule: LocatorModule.Config = LocatorModule.Config(),
    var motdModule: MotdModule.Config = MotdModule.Config(),
    var openableModule: OpenableModule.Config = OpenableModule.Config(),
    var playerModule: PlayerModule.Config = PlayerModule.Config(),
    var scoreboardModule: ScoreBoardModule.Config = ScoreBoardModule.Config(),
    var serverInfoModule: ServerInfoModule.Config = ServerInfoModule.Config(),
    var signModule: SignModule.Config = SignModule.Config(),
    var sitModule: SitModule.Config = SitModule.Config(),
    var tabListModule: TabListModule.Config = TabListModule.Config(),
    var tameableModule: TameableModule.Config = TameableModule.Config(),
) {
    companion object {
        private val json =
            Json {
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
                namingStrategy = CapitalizedStrategy
            }

        val reloadCommand: CommandData =
            CommandData(
                Commands
                    .literal("vanillaplus")
                    .requires { ctx -> ctx.sender.hasPermission(reloadPermission) }
                    .then(
                        Commands
                            .literal("reload")
                            .executesCatching { ctx ->
                                configData = ConfigData().load("config.json")

                                if (ctx.source.sender is Player) {
                                    ctx.source.sender.sendMessage(
                                        MM.deserialize("${instance.prefix} <green>configuration reloaded!"),
                                    )
                                } else {
                                    instance.logger.info("Configuration reloaded!")
                                }
                            },
                    ),
                "Allows to plugin specific admin commands",
                listOf("vp"),
            )

        val reloadPermission: Permission =
            Permission(
                "${instance.javaClass.simpleName}.reload".lowercase(),
                "Allows use of the reload command",
                PermissionDefault.OP,
            )

        /**
         * Loads or creates the configuration file.
         * @param fileName The name of the configuration file.
         * @return The loaded configuration data.
         */
        inline fun <reified T> T.load(fileName: String): T {
            val file = instance.dataFolder.toPath() / fileName

            if (!instance.dataFolder.toPath().exists()) instance.dataFolder.toPath().createDirectories()

            val loadedConfig = if (file.exists()) json.decodeFromString(file.readText()) else this

            instance.logger.info(
                "${if (file.exists()) "Loaded configuration from $fileName" else "Created default $fileName"} | Took ${
                    measureTime { file.writeText(json.encodeToString(loadedConfig)) }.inWholeMilliseconds
                }ms",
            )

            return loadedConfig
        }
    }
}
