/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.File

class GroupUtils(yamlFile: File) {
    private var yaml: YamlConfiguration?
    private var groups: LinkedHashMap<String?, Group?>

    //TODO: use Config
    init {
        if (!yamlFile.exists()) {
            instance.logger.info("groups.yml does not exist, skipping custom group settings.")
        }
        this.yaml = YamlConfiguration.loadConfiguration(yamlFile)
        groups = LinkedHashMap<String?, Group?>()

        for (groupName in yaml!!.getKeys(true)) {
            val defaultRadius = yaml!!.getInt("$groupName.default-chest-radius", -1)
            val maxRadius = yaml!!.getInt("$groupName.max-chest-radius", -1)
            groups.put(groupName, Group(defaultRadius, maxRadius))
        }
    }

    fun getDefaultRadiusPerPlayer(p: Player): Int {
        if (yaml == null) return instance.config.getInt("default-chest-radius")
        val it = groups.keys.iterator()
        var bestValueFound = -1
        while (it.hasNext()) {
            val group = it.next()
            if (!p.hasPermission("invunload.group.$group")) continue
            val defaultRadius = groups[group]!!.defaultRadius
            bestValueFound = if (defaultRadius > bestValueFound) defaultRadius else bestValueFound
        }
        return if (bestValueFound == -1) instance.config.getInt("default-chest-radius") else bestValueFound
    }

    fun getMaxRadiusPerPlayer(p: Player): Int {
        if (yaml == null) return instance.config.getInt("max-chest-radius")
        val it = groups.keys.iterator()
        var bestValueFound = -1
        while (it.hasNext()) {
            val group = it.next()
            if (!p.hasPermission("invunload.group.$group")) continue
            val maxRadius = groups[group]!!.maxRadius
            bestValueFound = if (maxRadius > bestValueFound) maxRadius else bestValueFound
        }
        return if (bestValueFound == -1) instance.config.getInt("max-chest-radius") else bestValueFound
    }

    internal class Group(val defaultRadius: Int, val maxRadius: Int)
}
