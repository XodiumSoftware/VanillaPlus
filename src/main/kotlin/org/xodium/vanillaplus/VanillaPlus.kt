@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.data.ConfigData.Companion.load
import org.xodium.vanillaplus.modules.*
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

        lateinit var configData: ConfigData
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
                ConfigData.reloadCommand.builder.build(),
                ConfigData.reloadCommand.description,
                ConfigData.reloadCommand.aliases,
            )
        }

        instance.server.pluginManager.addPermission(ConfigData.reloadPermission)

        configData = ConfigData().load("config.json")

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

        val modules =
            listOf(
                BookshelfModule,
                BooksModule,
                ChatModule,
                DimensionsModule,
                EntityModule,
                InventoryModule,
                LocatorModule,
                MannequinModule,
                MapModule,
                MotdModule,
                OpenableModule,
                PlayerModule,
                ServerInfoModule,
                ScoreBoardModule,
                SitModule,
                TabListModule,
                TameableModule,
            ).filter { it.isEnabled }

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
