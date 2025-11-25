package org.xodium.vanillaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.features.*
import org.xodium.vanillaplus.managers.ModuleManager

/** Main class of the plugin. */
internal class VanillaPlus : JavaPlugin() {
    companion object {
        lateinit var instance: VanillaPlus
            private set
    }

    init {
        instance = this
    }

    /** Called when the plugin is enabled. */
    override fun onEnable() {
        val unsupportedVersionMsg =
            "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

        if (!server.version.contains(pluginMeta.version)) disablePlugin(unsupportedVersionMsg)

        ModuleManager.run {}
        BooksFeature.register()
        CauldronFeature.register()
        ChatFeature.register()
        DimensionsFeature.register()
        MotdFeature.register()
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
