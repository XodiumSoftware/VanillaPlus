@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.handlers

import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.RecipeModule.reiNetworkKey
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/** Handles REI network communication between the server and clients. */
internal object REINetworkHandler : PluginMessageListener {
    private const val HANDSHAKE_PACKET_ID = 0
    private const val RECIPE_TRANSFER_PACKET_ID = 1
    private const val LATEST_PROTOCOL_VERSION = 19

    private val playerProtocolVersions = ConcurrentHashMap<UUID, Int>()

    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray,
    ) {
        if (channel != reiNetworkKey.toString()) return

        try {
            DataInputStream(ByteArrayInputStream(message)).use {
                when (it.readByte().toInt()) {
                    HANDSHAKE_PACKET_ID -> handleClientHandshake(player, it)
                    RECIPE_TRANSFER_PACKET_ID -> handleRecipeTransfer(player, it)
                }
            }
        } catch (e: Exception) {
            instance.logger.warning("Failed to handle REI packet for player ${player.name}: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Handles the client handshake packet, storing the client's protocol version and responding with the server's version.
     * @param player The player who sent the handshake.
     * @param data The data input stream containing the handshake data.
     */
    private fun handleClientHandshake(
        player: Player,
        data: DataInputStream,
    ) {
        val clientProtocolVersion = data.readInt()

        instance.logger.info(
            "Received REI handshake from ${player.name} (v$clientProtocolVersion).",
        )
        playerProtocolVersions[player.uniqueId] = clientProtocolVersion

        sendHandshake(player)
    }

    /**
     * Sends a handshake packet to the specified player, indicating the server's protocol version.
     * @param player The player to send the handshake to.
     */
    fun sendHandshake(player: Player) {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeByte(HANDSHAKE_PACKET_ID)
        dos.writeInt(LATEST_PROTOCOL_VERSION)

        if (player.isOnline) player.sendPluginMessage(instance, reiNetworkKey.toString(), baos.toByteArray())
    }

    /**
     * Cleans up the stored protocol version for a player when they quit.
     * @param player The player who is quitting.
     */
    fun onPlayerQuit(player: Player) {
        playerProtocolVersions.remove(player.uniqueId)
    }

    /**
     * Handles the recipe transfer request from the client.
     * @param player The player who sent the recipe transfer request.
     * @param data The data input stream containing the recipe transfer data.
     */
    private fun handleRecipeTransfer(
        player: Player,
        data: DataInputStream,
    ) {
        readString(data)
        readSlotMap(data)
        readSlotMap(data)
        data.readBoolean()
        instance.logger.fine("REI recipe transfer requested by ${player.name}")
    }

    /**
     * Reads a UTF-8 encoded string from the data input stream.
     * @param data The data input stream to read from.
     * @return The string read from the stream.
     */
    private fun readString(data: DataInputStream): String {
        val length = data.readInt()

        if (length < 0) throw IndexOutOfBoundsException("Invalid string length: $length")

        val bytes = ByteArray(length)

        data.readFully(bytes)

        return String(bytes, Charsets.UTF_8)
    }

    /**
     * Reads a slot map from the data input stream.
     * @param data The data input stream to read from.
     * @return A map representing the slot mappings.
     */
    private fun readSlotMap(data: DataInputStream): Map<Int, Int> {
        val size = data.readByte().toInt()
        val map = mutableMapOf<Int, Int>()

        repeat(size) { map[data.readByte().toInt()] = data.readByte().toInt() }

        return map
    }
}
