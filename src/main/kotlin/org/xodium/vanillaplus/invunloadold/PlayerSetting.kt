/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import org.bukkit.configuration.file.YamlConfiguration
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.File
import java.io.IOException

//TODO: use PlayerData
class PlayerSetting {
    var unloadHotbar: Boolean = false
    var dumpHotbar: Boolean = false

    internal constructor(file: File) {
        val yaml = YamlConfiguration.loadConfiguration(file)
        unloadHotbar = yaml.getBoolean("unloadHotbar", false)
        dumpHotbar = yaml.getBoolean("dumpHotbar", false)
    }

    fun save(file: File) {
        val yaml = YamlConfiguration()
        yaml.set("unloadHotbar", unloadHotbar)
        yaml.set("dumpHotbar", dumpHotbar)
        try {
            yaml.save(file)
        } catch (_: IOException) {
            instance.logger.warning("Could not save playerdata file " + file.path)
        }
    }
}
