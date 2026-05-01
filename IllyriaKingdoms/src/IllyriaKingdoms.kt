package org.xodium.illyriaplus

import org.bukkit.plugin.java.JavaPlugin
import org.xodium.illyriaplus.cmds.KingdomCmd
import org.xodium.illyriaplus.items.SceptreItem

/**
 * Main class for IllyriaKingdoms plugin.
 * A kingdoms/factions system for land claiming and territory management.
 */
internal class IllyriaKingdoms : JavaPlugin() {
    companion object {
        lateinit var instance: IllyriaKingdoms
            private set
    }

    override fun onEnable() {
        instance = this

        val unsupportedVersionMsg =
            "This plugin requires a supported server version. Supported versions: ${pluginMeta.version}."

        if (!server.version.contains(pluginMeta.version.substringBefore("+"))) disablePlugin(unsupportedVersionMsg)

        SceptreItem.register()
        KingdomCmd.register()
    }

    /**
     * Disable the plugin and log the message.
     *
     * @param msg The message to log.
     */
    private fun disablePlugin(msg: String): Nothing {
        logger.severe(msg)
        server.pluginManager.disablePlugin(instance)
        throw IllegalStateException(msg)
    }
}
