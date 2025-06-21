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
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

data class NicknameData(
    val nicknames: Map<UUID, String> = emptyMap()
) {
    companion object {
        private val mapper = jacksonObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        private val filePath = instance.dataFolder.toPath().resolve("nicknames.json")
        private val cache = mutableMapOf<UUID, String>()

        init {
            load()
        }

        private fun load() {
            if (filePath.toFile().exists()) {
                cache.clear()
                cache.putAll(mapper.readValue(filePath.toFile(), NicknameData::class.java).nicknames)
            }
        }

        private fun save() {
            instance.server.scheduler.runTaskAsynchronously(instance, Runnable {
                try {
                    filePath.parent.createDirectories()
                    filePath.writeText(mapper.writeValueAsString(NicknameData(cache)))
                } catch (e: IOException) {
                    instance.logger.severe("Failed to write ${NicknameData::class.simpleName} to file: ${e.message}")
                    e.printStackTrace()
                }
            })
        }

        /**
         * Retrieves the nickname for a given UUID.
         * @param uuid The UUID of the player.
         * @return The nickname if it exists, null otherwise.
         */
        fun get(uuid: UUID): String? = cache[uuid]

        /**
         * Sets the nickname for a given UUID and saves the changes.
         * @param uuid The UUID of the player.
         * @param nickname The nickname to set.
         */
        fun set(uuid: UUID, nickname: String) {
            cache[uuid] = nickname
            save()
        }
    }
}
