package org.xodium.vanillaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.commands.ReloadCommand


/**
 * The `VanillaPlus` class serves as the main plugin class for the VanillaPlus Bukkit/Spigot plugin.
 * It extends the `JavaPlugin` class, allowing it to be loaded by the server. This class is responsible
 * for initializing the plugin, validating platform and server version compatibility, and managing
 * core operations at the plugin startup phase.
 *
 * The plugin verifies its compatibility with supported platforms and server versions upon enabling.
 * If the server fails these checks, the plugin disables itself and logs an appropriate error message.
 *
 * Key Responsibilities:
 * - Initializes the plugin configuration by generating a default configuration file if it's absent.
 * - Loads core components such as commands (`ReloadCommand`) and modules (`ModuleManager`).
 * - Ensures compatibility with predefined server versions and supported platforms.
 *
 * Compatibility:
 * `VanillaPlus` maintains a list of `SUPPORTED_VERSIONS` and `SUPPORTED_PLATFORMS` that the plugin
 * can operate on. If the running server does not meet these criteria, the plugin disables itself.
 *
 * Constants:
 * - `PREFIX`: A customizable string prefix used globally for in-game messages.
 *
 * Singleton Access:
 * Provides a global access point to the plugin instance via the `VanillaPlus.instance` property.
 */
class VanillaPlus : JavaPlugin() {
    companion object {
        private val SUPPORTED_VERSIONS = listOf("1.21.3")
        private val SUPPORTED_PLATFORMS = listOf("Paper")
        private val UNSUPPORTED_PLATFORM_MSG =
            "This plugin requires a supported server platform. Supported platforms: ${SUPPORTED_PLATFORMS.joinToString(", ")}."
        private val UNSUPPORTED_VERSION_MSG =
            "This plugin requires a supported server version. Supported versions: ${SUPPORTED_VERSIONS.joinToString(", ")}."

        const val PREFIX: String = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>"

        @JvmStatic
        lateinit var instance: VanillaPlus
            private set
    }

    override fun onEnable() {
        instance = this
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
}