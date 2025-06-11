/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.managers.ModuleManager
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

/** Main class of the plugin. */
class VanillaPlus : JavaPlugin() {
    companion object {
        private const val SUPPORTED_PLATFORM = "Paper"
        private const val UNSUPPORTED_PLATFORM_MSG =
            "This plugin requires a supported server platform. Supported platforms: ${SUPPORTED_PLATFORM}."

        val PREFIX: String = "${"[".mangoFmt(true)}${"VanillaPlus".fireFmt()}${"]".mangoFmt()}"

        @JvmStatic
        val instance: VanillaPlus by lazy { getPlugin(VanillaPlus::class.java) }
    }

    /** Called when the plugin is enabled. */
    override fun onEnable() {
        @Suppress("UnstableApiUsage")
        val supportedVersion =
            pluginMeta.apiVersion ?: throw IllegalStateException("Missing 'api-version' in paper-plugin.yml")
        when {
            !server.version.contains(supportedVersion) ->
                disablePlugin("This plugin requires Minecraft $supportedVersion (got ${server.version}).")

            !server.name.contains(SUPPORTED_PLATFORM) ->
                disablePlugin(UNSUPPORTED_PLATFORM_MSG)

            else -> {
                ConfigManager.load()
                Perms
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
