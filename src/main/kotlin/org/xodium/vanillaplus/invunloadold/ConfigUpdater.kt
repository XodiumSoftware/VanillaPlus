/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.xodium.vanillaplus.invunloadold.Main
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

class ConfigUpdater(val main: Main) {
    // Admins hate config updates. Just relax and let ChestSort update to the newest
    // config version
    // Don't worry! Your changes will be kept
    fun updateConfig() {
        try {
            Files.deleteIfExists(
                File(
                    main.dataFolder.absolutePath + File.separator + "config.old.yml"
                ).toPath()
            )
        } catch (ignored: IOException) {
        }

        FileUtils.renameFileInPluginDir(main, "config.yml", "config.old.yml")
        main.saveDefaultConfig()

        val oldConfigFile = File(main.dataFolder.absolutePath + File.separator + "config.old.yml")
        val oldConfig: FileConfiguration = YamlConfiguration.loadConfiguration(oldConfigFile)

        try {
            oldConfig.load(oldConfigFile)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
        }

        val oldValues = oldConfig.getValues(false)

        // Read default config to keep comments
        val linesInDefaultConfig = ArrayList<String>()
        try {
            val scanner = Scanner(
                File(main.dataFolder.absolutePath + File.separator + "config.yml"), "UTF-8"
            )
            while (scanner.hasNextLine()) {
                linesInDefaultConfig.add(scanner.nextLine() + "")
            }
            scanner.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        val newLines = ArrayList<String?>()
        for (line in linesInDefaultConfig) {
            var newline: String? = line
            if (line.startsWith("config-version:")) {
                // dont replace config-version
            } else {
                for (node in oldValues.keys) {
                    if (line.startsWith(node + ":")) {
                        var quotes = ""

                        if (node.startsWith("message-"))  // needs double quotes
                            quotes = "\""

                        newline = node + ": " + quotes + oldValues.get(node).toString() + quotes
                        break
                    }
                }
            }
            if (newline != null) {
                newLines.add(newline)
            }
        }

        //FileWriter fw;
        val fw: BufferedWriter?
        val linesArray = newLines.toTypedArray<String?>()
        try {
            fw = Files.newBufferedWriter(
                File(main.dataFolder.absolutePath, "config.yml").toPath(),
                StandardCharsets.UTF_8
            )
            for (s in linesArray) {
                //System.out.println("WRITING LINE: "+linesArray[i]);
                fw.write(s + "\n")
            }
            fw.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        // Utils.renameFileInPluginDir(plugin, "config.yml.default", "config.yml");
    }
}
