package org.xodium.vanillaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.commands.ReloadCommand

class VanillaPlus : JavaPlugin() {
    override fun onEnable() {
        when {
            !isSupportedVersion() -> disablePlugin(UNSUPPORTED_VERSION_MSG)
            !isSupportedPlatform() -> disablePlugin(UNSUPPORTED_PLATFORM_MSG)
            else -> {
                saveDefaultConfig()
                ReloadCommand
                ModuleManager
            }
        }
    }

    private fun disablePlugin(msg: String) {
        logger.severe(msg)
        server.pluginManager.disablePlugin(this)
    }

    private fun isSupportedVersion(): Boolean {
        return SUPPORTED_VERSIONS.any { k -> server.version.contains(k) }
    }

    private fun isSupportedPlatform(): Boolean {
        return SUPPORTED_PLATFORMS.any { k -> server.name.contains(k) }
    }

    companion object {
        private val SUPPORTED_VERSIONS = listOf("1.21.3")
        private val SUPPORTED_PLATFORMS = listOf("Paper")
        private val UNSUPPORTED_PLATFORM_MSG =
            "This plugin requires a supported server platform. Supported platforms: ${SUPPORTED_PLATFORMS.joinToString(", ")}."
        private val UNSUPPORTED_VERSION_MSG =
            "This plugin requires a supported server version. Supported versions: ${SUPPORTED_VERSIONS.joinToString(", ")}."

        const val PREFIX: String = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>"

        @JvmStatic
        val instance: VanillaPlus = getPlugin(VanillaPlus::class.java)
    }
}