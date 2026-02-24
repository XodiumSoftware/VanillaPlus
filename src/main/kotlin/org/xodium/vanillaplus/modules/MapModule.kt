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

    private val serverLevelId: Int = initServerLevelId()

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
    private fun initServerLevelId(): Int =
        runCatching {
            val file = getXaeroMapFile()

            if (file.exists()) readServerLevelId(file) else createServerLevelId(file)
        }.getOrElse {
            instance.logger.warning("Failed to initialize xaeromap.txt: $it")
            0
        }

    /**
     * Resolves the Xaero map data file inside the server world container.
     * @return The xaeromap.txt file location.
     */
    private fun getXaeroMapFile(): File =
        File(instance.server.worldContainer.canonicalPath + File.separator + "xaeromap.txt")

    /**
     * Creates a new server-level ID, writes it to the file, and returns it.
     * @param file The target file.
     * @return The generated server-level ID.
     */
    private fun createServerLevelId(file: File): Int =
        FileOutputStream(file, false).use { Random().nextInt().also { id -> it.write("id:$id".toByteArray()) } }

    /**
     * Reads and parses the server-level ID from the given file.
     * @param file The xaeromap.txt file.
     * @return The stored server-level ID.
     * @throws Exception If the file content is invalid.
     */
    private fun readServerLevelId(file: File): Int =
        FileReader(file).use { reader ->
            BufferedReader(reader).use {
                it
                    .readLine()
                    .split(":")
                    .also { args ->
                        if (args.getOrNull(0) != "id") throw Exception("Failed to read id from xaeromap.txt")
                    }.getOrNull(1)
                    ?.toInt()
                    ?: throw Exception("Failed to read id from xaeromap.txt")
            }
        }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
