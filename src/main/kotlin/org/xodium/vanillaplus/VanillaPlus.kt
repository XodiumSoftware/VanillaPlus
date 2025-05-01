/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.managers.ModuleManager

/** Main class of the plugin */
class VanillaPlus : JavaPlugin() {
    companion object {
        private val SUPPORTED_VERSIONS = setOf("1.21.5")
        private val SUPPORTED_PLATFORMS = setOf("Paper")
        private val UNSUPPORTED_PLATFORM_MSG =
            "This plugin requires a supported server platform. Supported platforms: ${SUPPORTED_PLATFORMS.joinToString(", ")}."
        private val UNSUPPORTED_VERSION_MSG =
            "This plugin requires a supported server version. Supported versions: ${SUPPORTED_VERSIONS.joinToString(", ")}."

        const val PREFIX: String = "<gold>[<dark_aqua>VanillaPlus<gold>] <reset>"

        @JvmStatic
        val instance: VanillaPlus by lazy { getPlugin(VanillaPlus::class.java) }
    }

    /** Called when the plugin is enabled */
    override fun onEnable() {
        when {
            !isSupportedVersion() -> disablePlugin(UNSUPPORTED_VERSION_MSG)
            !isSupportedPlatform() -> disablePlugin(UNSUPPORTED_PLATFORM_MSG)
            else -> {
                Perms
                Database
                ModuleManager
            }
        }
    }

    /**
     * Disable the plugin and log the message
     * @param msg The message to log
     */
    private fun disablePlugin(msg: String) {
        logger.severe(msg)
        server.pluginManager.disablePlugin(this)
    }

    /** Check if the server version is supported */
    private fun isSupportedVersion() = SUPPORTED_VERSIONS.any(server.version::contains)

    /** Check if the server platform is supported */
    private fun isSupportedPlatform() = SUPPORTED_PLATFORMS.any(server.name::contains)
}
