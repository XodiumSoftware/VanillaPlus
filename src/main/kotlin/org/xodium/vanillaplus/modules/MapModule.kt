@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.google.common.io.ByteStreams
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*

/** Represents a module handling map mechanics within the system. */
internal object MapModule : ModuleInterface {
    private const val WORLD_MAP_CHANNEL: String = "xaeroworldmap:main"
    private const val MINI_MAP_CHANNEL: String = "xaerominimap:main"

    private val serverLevelId: Int = initializeServerLevelId()

    init {
        if (isEnabled) {
            instance.server.messenger.apply {
                registerOutgoingPluginChannel(instance, WORLD_MAP_CHANNEL)
                registerOutgoingPluginChannel(instance, MINI_MAP_CHANNEL)
            }
        }
    }

    @EventHandler
    fun on(event: PlayerRegisterChannelEvent) {
        val channel = event.channel

        if (channel != WORLD_MAP_CHANNEL && channel != MINI_MAP_CHANNEL) return

        sendPlayerWorldId(event.player, channel)
    }

    @EventHandler
    fun on(event: PlayerChangedWorldEvent) {
        val player = event.player

        sendPlayerWorldId(player, WORLD_MAP_CHANNEL)
        sendPlayerWorldId(player, MINI_MAP_CHANNEL)
    }

    /**
     * Sends the configured server-level ID to the given player over the specified plugin channel.
     * @param player The target player who will receive the plugin message.
     * @param channel The plugin channel to send the message through.
     */
    private fun sendPlayerWorldId(
        player: Player,
        channel: String,
    ) {
        val bytes = ByteStreams.newDataOutput()

        bytes.writeByte(0)
        bytes.writeInt(serverLevelId)

        player.sendPluginMessage(instance, channel, bytes.toByteArray())
    }

    /**
     * Initializes and returns the persistent server-level ID used by Xaero's map mods.
     * @return The initialized or loaded server-level ID, or `0` if initialization fails.
     */
    private fun initializeServerLevelId(): Int {
        try {
            val worldFolder = instance.server.worldContainer.canonicalPath
            val xaeromapFile = File("$worldFolder${File.separator}xaeromap.txt")

            if (!xaeromapFile.exists()) {
                try {
                    FileOutputStream(xaeromapFile, false).use { xaeromapFileStream ->
                        val id = Random().nextInt()
                        val idString = "id:$id"

                        xaeromapFileStream.write(idString.toByteArray())

                        return id
                    }
                } catch (ex: Exception) {
                    instance.logger.warning("Failed to create xaeromap.txt: $ex")
                }
            } else {
                try {
                    FileReader(xaeromapFile).use { fileReader ->
                        BufferedReader(fileReader).use { bufferedReader ->
                            val line = bufferedReader.readLine()
                            val args = line.split(":")

                            if (args[0] != "id") throw Exception("Failed to read id from xaeromap.txt")

                            return args[1].toInt()
                        }
                    }
                } catch (ex: Exception) {
                    instance.logger.warning("Failed to read xaeromap.txt: $ex")
                }
            }
        } catch (ex: Exception) {
            instance.logger.warning("Failed to get world ID: $ex")
        }

        return 0
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
