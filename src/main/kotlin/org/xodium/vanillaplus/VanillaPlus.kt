package org.xodium.vanillaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.managers.ModuleManager

/** Main class of the plugin. */
internal class VanillaPlus : JavaPlugin() {
    companion object {
        private const val SUPPORTED_PLATFORM = "Paper"
        private const val UNSUPPORTED_PLATFORM_MSG =
            "This plugin requires a supported server platform. Supported platforms: ${SUPPORTED_PLATFORM}."

        @JvmStatic
        val instance: VanillaPlus by lazy { getPlugin(VanillaPlus::class.java) }
    }

    private val unsupportedVersionMsg =
        "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

    /** Called when the plugin is enabled. */
    override fun onEnable() {
        when {
            !server.version.contains(pluginMeta.version) -> disablePlugin(unsupportedVersionMsg)
            !server.name.contains(SUPPORTED_PLATFORM) -> disablePlugin(UNSUPPORTED_PLATFORM_MSG)
            else -> ModuleManager.run {}
        }
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
