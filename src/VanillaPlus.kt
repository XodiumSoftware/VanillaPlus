@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.modules.BooksModule
import org.xodium.vanillaplus.modules.BookshelfModule
import org.xodium.vanillaplus.modules.ChatModule
import org.xodium.vanillaplus.modules.DimensionsModule
import org.xodium.vanillaplus.modules.EntityModule
import org.xodium.vanillaplus.modules.InventoryModule
import org.xodium.vanillaplus.modules.LocatorModule
import org.xodium.vanillaplus.modules.MapModule
import org.xodium.vanillaplus.modules.MotdModule
import org.xodium.vanillaplus.modules.OpenableModule
import org.xodium.vanillaplus.modules.PlayerModule
import org.xodium.vanillaplus.modules.ScoreBoardModule
import org.xodium.vanillaplus.modules.ServerInfoModule
import org.xodium.vanillaplus.modules.SitModule
import org.xodium.vanillaplus.modules.TabListModule
import org.xodium.vanillaplus.modules.TameableModule
import org.xodium.vanillaplus.recipes.ChainmailRecipe
import org.xodium.vanillaplus.recipes.DiamondRecycleRecipe
import org.xodium.vanillaplus.recipes.PaintingRecipe
import org.xodium.vanillaplus.recipes.RottenFleshRecipe
import org.xodium.vanillaplus.recipes.WoodLogRecipe

/** Main class of the plugin. */
internal class VanillaPlus : JavaPlugin() {
    companion object {
        lateinit var instance: VanillaPlus
            private set
    }

    init {
        instance = this
    }

    override fun onEnable() {
        val unsupportedVersionMsg =
            "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

        if (!server.version.contains(pluginMeta.version.substringBefore("+"))) disablePlugin(unsupportedVersionMsg)

        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            it.registrar().register(
                ConfigManager.reloadCommand.builder.build(),
                ConfigManager.reloadCommand.description,
                ConfigManager.reloadCommand.aliases,
            )
        }

        instance.server.pluginManager.addPermission(ConfigManager.reloadPermission)

        ConfigManager.load("config.json")

        val recipes =
            listOf(
                ChainmailRecipe,
                DiamondRecycleRecipe,
                PaintingRecipe,
                RottenFleshRecipe,
                WoodLogRecipe,
            )

        logger.info(
            "Registered: ${recipes.sumOf { it.recipes.size }} recipes(s) | Took ${recipes.sumOf { it.register() }}ms",
        )

        val allModules =
            listOf(
                BookshelfModule,
                BooksModule,
                ChatModule,
                DimensionsModule,
                EntityModule,
                InventoryModule,
                LocatorModule,
                MapModule,
                MotdModule,
                OpenableModule,
                PlayerModule,
                ServerInfoModule,
                ScoreBoardModule,
                SitModule,
                TabListModule,
                TameableModule,
            )

        ConfigManager.prune()
        ConfigManager.save("config.json")

        val startupEnabled = allModules.associate { it::class.simpleName!! to it.config.enabled }

        ConfigManager.onReload {
            allModules
                .filter { it.config.enabled != startupEnabled[it::class.simpleName] }
                .forEach {
                    ConfigManager.addReloadWarning(
                        "${it::class.simpleName} 'Enabled' changed — restart required for this to take effect.",
                    )
                }
        }

        val modules = allModules.filter { it.config.enabled }

        logger.info(
            "Registered: ${modules.size} module(s) | Took ${modules.sumOf { it.register() }}ms",
        )
    }

    override fun onDisable() {
        server.messenger.unregisterOutgoingPluginChannel(instance)
    }

    /**
     * Disable the plugin and log the message.
     * @param msg The message to log.
     */
    private fun disablePlugin(msg: String): Nothing {
        logger.severe(msg)
        server.pluginManager.disablePlugin(instance)
        throw IllegalStateException(msg)
    }
}
