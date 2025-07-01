/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.managers.ModuleManager
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt


/** Main class of the plugin. */
class VanillaPlus : JavaPlugin() {
    companion object {
        private const val SUPPORTED_VERSION = "1.21.7"
        private const val SUPPORTED_PLATFORM = "Paper"
        private const val UNSUPPORTED_PLATFORM_MSG =
            "This plugin requires a supported server platform. Supported platforms: ${SUPPORTED_PLATFORM}."
        private const val UNSUPPORTED_VERSION_MSG =
            "This plugin requires a supported server version. Supported versions: ${SUPPORTED_VERSION}."

        val PREFIX: String = "${"[".mangoFmt(true)}${"VanillaPlus".fireFmt()}${"]".mangoFmt()}"

        @JvmStatic
        val instance: VanillaPlus by lazy { getPlugin(VanillaPlus::class.java) }

        lateinit var advancementAPI: UltimateAdvancementAPI
            private set
    }

    /** Called when the plugin is enabled. */
    override fun onEnable() {
        when {
            !server.version.contains(SUPPORTED_VERSION) -> disablePlugin(UNSUPPORTED_VERSION_MSG)
            !server.name.contains(SUPPORTED_PLATFORM) -> disablePlugin(UNSUPPORTED_PLATFORM_MSG)
            else -> {
                advancementAPI = UltimateAdvancementAPI.getInstance(this)
                ModuleManager
            }
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
