package org.xodium.illyriaplus

import org.bukkit.plugin.java.JavaPlugin

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
        logger.info("IllyriaKingdoms enabled!")
    }

    override fun onDisable() {
        logger.info("IllyriaKingdoms disabled!")
    }
}
