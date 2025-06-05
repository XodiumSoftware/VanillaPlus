/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.mapify

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files

class DataHandler {
    private var gson: Gson = GsonBuilder().create()
    private var dataFile: File? = null

    @JvmField
    var data: PluginData? = null
    private var dirty: Boolean = false

    init {
        this.dataFile = File(Mapify.INSTANCE.dataFolder, "data.json")
        try {
            this.loadData()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun dirty() {
        this.dirty = true
    }

    @Throws(IOException::class)
    fun loadData() {
        if (!this.dataFile!!.exists()) {
            Mapify.INSTANCE.logger.info("No data file found.  Using blank data...")
            this.data = PluginData()
        } else {
            Mapify.INSTANCE.logger.info("Loading from data file...")
            val content = Files.readString(this.dataFile!!.toPath())
            this.data = gson.fromJson(content, PluginData::class.java)
        }
    }

    fun trySaveData(force: Boolean) {
        try {
            this.saveData(force)
        } catch (ex: IOException) {
            throw RuntimeException("Error saving data file", ex)
        }
    }

    @Throws(IOException::class)
    fun saveData(force: Boolean) {
        if (!dirty && !force) return
        Mapify.INSTANCE.config?.let {
            if (it.debug) {
                Mapify.INSTANCE.logger.info("Saving data file...")
            }
        }
        val json = gson.toJson(data)
        if (!this.dataFile!!.getParentFile().exists()) this.dataFile!!.getParentFile().mkdir()
        if (!this.dataFile!!.exists()) this.dataFile!!.createNewFile()
        val fw = FileWriter(this.dataFile!!)
        fw.write(json)
        fw.close()
        Mapify.INSTANCE.config?.let {
            if (it.debug) {
                Mapify.INSTANCE.logger.info("Done saving data.")
            }
        }
    }
}
