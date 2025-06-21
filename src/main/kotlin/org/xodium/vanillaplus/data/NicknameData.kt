/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

data class NicknameData(
    val nicknames: Map<UUID, String> = emptyMap()
) {
    companion object {
        private val mapper = jacksonObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        private val filePath: Path = instance.dataFolder.toPath().resolve("nicknames.json")
        private val cache = mutableMapOf<UUID, String>()
    }

    init {
        load()
    }

    /** Writes the current state to the JSON file asynchronously. */
    private fun readState(): NicknameData {
        return if (filePath.toFile().exists()) {
            mapper.readValue(filePath.toFile(), NicknameData::class.java)
        } else {
            NicknameData()
        }
    }

    /** Loads the current state to the JSON file asynchronously. */
    private fun load(): NicknameData {
        val state = readState()
        cache.clear()
        cache.putAll(state.nicknames)
        return state
    }

    private fun save(data: NicknameData) {
        instance.server.scheduler.runTaskAsynchronously(instance, Runnable {
            try {
                Files.createDirectories(filePath.parent)
                Files.writeString(filePath, mapper.writeValueAsString(data))
            } catch (e: IOException) {
                instance.logger.severe("Failed to write NicknameData to file: ${e.message}")
                e.printStackTrace()
            }
        })
    }

    fun get() {}

    fun set() {}
}
