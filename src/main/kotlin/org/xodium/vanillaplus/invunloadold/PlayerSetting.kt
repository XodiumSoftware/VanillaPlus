/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class PlayerSetting {
    private val blacklist: BlackList
    var unloadHotbar: Boolean = false
    var dumpHotbar: Boolean = false

    internal constructor() {
        blacklist = BlackList()
    }

    internal constructor(file: File) {
        val yaml = YamlConfiguration.loadConfiguration(file)
        blacklist = BlackList(yaml.getStringList("blacklist"))
        unloadHotbar = yaml.getBoolean("unloadHotbar", false)
        dumpHotbar = yaml.getBoolean("dumpHotbar", false)
    }

    fun save(file: File, main: Main) {
        val yaml = YamlConfiguration()
        yaml.set("blacklist", blacklist.toStringList())
        yaml.set("unloadHotbar", unloadHotbar)
        yaml.set("dumpHotbar", dumpHotbar)
        try {
            yaml.save(file)
        } catch (e: IOException) {
            main.logger.warning("Could not save playerdata file " + file.path)
            //e.printStackTrace();
        }
    }
}
