@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.data.ConfigData.Companion.load
import org.xodium.vanillaplus.modules.*
import org.xodium.vanillaplus.modules.ArmorStandModule.info
import org.xodium.vanillaplus.recipes.PaintingRecipe
import org.xodium.vanillaplus.recipes.PaintingRecipe.info
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

    /** Called when the plugin is enabled. */
    override fun onEnable() {
        val unsupportedVersionMsg =
            "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

        if (!server.version.contains(pluginMeta.version)) disablePlugin(unsupportedVersionMsg)

        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                ConfigData.reloadCommand.builder.build(),
                ConfigData.reloadCommand.description,
                ConfigData.reloadCommand.aliases,
            )
        }
        instance.server.pluginManager.addPermission(ConfigData.reloadPermission)

        configData = ConfigData().load("config.json")

        logger.info(
            listOf(
                PaintingRecipe,
                RottenFleshRecipe,
                WoodLogRecipe,
            ),
        )

        logger.info(
            listOf(
                ArmorStandModule,
                BooksModule,
                ChatModule,
                DimensionsModule,
                EntityModule,
                InventoryModule,
                LocatorModule,
                MotdModule,
                OpenableModule,
                PlayerModule,
                ScoreBoardModule,
                SignModule,
                SitModule,
                TabListModule,
                TameableModule,
            ),
        )
    }

    /**
     * Disable the plugin and log the message.
     * @param msg The message to log.
     */
    private fun disablePlugin(msg: String) {
        logger.severe(msg)
        server.pluginManager.disablePlugin(this)
    }
}
