/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.mapify

import kotlinx.io.IOException
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.xodium.vanillaplus.mapify.util.Cache
import org.xodium.vanillaplus.mapify.util.Util
import java.awt.Image
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.ExecutionException

class Mapify : JavaPlugin() {
    var imageCache: Cache<URL?, Image?>? = null
    var config: PluginConfig? = null
    var dataHandler: DataHandler? = null

    init {
        INSTANCE = this
    }

    override fun onEnable() {
        try {
            Util.isLatestVersion.thenAccept { latest ->
                latest?.let {
                    if (!it) {
                        this.logger.warning("Mapify has an update!")
                        this.logger.warning("Get it from https://modrinth.com/plugin/mapify")
                    }
                }
            }.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }

        this.dataHandler = DataHandler()
        val maps = dataHandler!!.data!!.mapData.size
        this.logger.info("Loaded " + maps + " map" + (if (maps == 1) "" else "s") + ".")

        // Periodically save the data if changed (every 5 minutes)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this,
            { this.dataHandler!!.trySaveData(false) },
            5 * 60 * 20L,
            5 * 60 * 20L
        ) // 5 minutes * 60 seconds * 20 ticks

        // Periodically force save the data (hourly)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this,
            { this.dataHandler!!.trySaveData(true) },
            (60 + 3) * 60 * 20L,  // wait 63 minutes so the two tasks never run at the same time
            60 * 60 * 20L
        ) // 60 minutes * 60 seconds * 20 ticks


        try {
            this.loadConfig()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        // Clear the expired items every hour
        this.imageCache?.let {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(
                this,
                it::clearExpired,
                0,
                60 * 60 * 20L
            )
        } // 60 minutes * 60 seconds * 20 ticks
    }

    @Throws(IOException::class)
    fun loadConfig() {
        this.reloadConfig()
        config = PluginConfig(INSTANCE)

        if (this.config!!.saveImages) {
            val imgDir = Path.of(this.dataFolder.path, "img").toFile()
            if (!imgDir.exists()) {
                val mkdir = imgDir.mkdirs()
                if (!mkdir) {
                    this.logger.severe("Unable to create img directory.")
                }
            }
        }

        if (this.imageCache == null) {
            this.imageCache =
                Cache(this.config!!.cacheDuration.toLong() * 60 * 1000) { it?.let { url -> Util.getImage(url) } }
        } else {
            this.imageCache.setCacheDuration(this.config!!.cacheDuration * 60 * 1000)
        }
    }

    override fun onDisable() {
        try {
            this.dataHandler!!.saveData(true)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        lateinit var INSTANCE: Mapify
    }
}
