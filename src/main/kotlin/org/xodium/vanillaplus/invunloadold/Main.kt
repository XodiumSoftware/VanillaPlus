/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.invunloadold

import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.File
import java.util.*

class Main : JavaPlugin(), Listener {
    private var useChestSort: Boolean = false
    var messages: Messages? = null
    private var chestSortHook: ChestSortHook? = null
    private var commandUnload: CommandUnload? = null
    private var commandUnloadInfo: CommandUnloadInfo? = null
    private var commandSearchItem: CommandSearchitem? = null
    private var commandBlacklist: CommandBlacklist? = null
    var materialTabCompleter: MaterialTabCompleter? = null
    private var playerSettings: HashMap<UUID?, PlayerSetting?>? = null

    override fun onDisable() {
        saveAllPlayerSettings()
    }

    override fun onEnable() {
        if (!config.getBoolean("use-chestsort") || instance.server.pluginManager.getPlugin("ChestSort") == null) {
            useChestSort = false
        } else {
            try {
                Class.forName("de.jeff_media.chestsort.api.ChestSortAPI")
                useChestSort = true
                logger.info("Succesfully hooked into ChestSort")
            } catch (e: ClassNotFoundException) {
                logger.warning("Your version of ChestSort is too old, disabling ChestSort integration. Please upgrade ChestSort to version 11.0.0 or later.")
            }
        }

        chestSortHook = _root_ide_package_.org.xodium.vanillaplus.hooks.ChestSortHook(this)
        registerCommands()
    }

    private fun registerCommands() {
        commandUnload = CommandUnload(this)
        commandUnloadInfo = CommandUnloadInfo(this)
        commandSearchItem = CommandSearchitem(this)
        commandBlacklist = CommandBlacklist(this)
        materialTabCompleter = MaterialTabCompleter(this)
        getCommand("unload")!!.setExecutor(commandUnload)
        getCommand("dump")!!.setExecutor(commandUnload)
        getCommand("unloadinfo")!!.setExecutor(commandUnloadInfo)
        getCommand("searchitem")!!.setExecutor(commandSearchItem)
        getCommand("searchitem")!!.tabCompleter = materialTabCompleter
        getCommand("blacklist")!!.setExecutor(commandBlacklist)
        getCommand("blacklist")!!.tabCompleter = commandBlacklist
    }

    fun getPlayerSetting(p: Player): PlayerSetting? {
        if (playerSettings!!.containsKey(p.uniqueId)) {
            return playerSettings!!.get(p.uniqueId)
        }
        val setting = if (getPlayerFile(p.uniqueId).exists()) {
            PlayerSetting(getPlayerFile(p.uniqueId))
        } else {
            PlayerSetting()
        }
        playerSettings!!.put(p.uniqueId, setting)
        return setting
    }

    private fun getPlayerFile(uuid: UUID): File {
        return File(dataFolder.toString() + File.separator + "playerdata" + File.separator + uuid.toString() + ".yml")
    }

    private fun saveAllPlayerSettings() {
        for (entry in playerSettings!!.entries) {
            entry.value.save(getPlayerFile(entry.key!!), this)
        }
    }
}
