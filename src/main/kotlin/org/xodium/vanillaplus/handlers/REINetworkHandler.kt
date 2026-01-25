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

internal object REINetworkHandler : PluginMessageListener {
    private const val HANDSHAKE_PACKET_ID = 0
    private const val RECIPE_TRANSFER_PACKET_ID = 1
    private const val LATEST_PROTOCOL_VERSION = 19

    private val playerProtocolVersions = mutableMapOf<UUID, Int>()

    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray,
    ) {
        if (channel != reiNetworkKey.toString()) return

        try {
            val data = DataInputStream(ByteArrayInputStream(message))
            val packetId = data.readByte().toInt()

            when (packetId) {
                HANDSHAKE_PACKET_ID -> handleClientHandshake(player, data)
                RECIPE_TRANSFER_PACKET_ID -> handleRecipeTransfer(player, data)
            }
        } catch (e: Exception) {
            instance.logger.warning("Failed to handle REI packet for player ${player.name}: ${e.message}")
            e.printStackTrace()
        }
    }

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

    fun sendHandshake(player: Player) {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeByte(HANDSHAKE_PACKET_ID)
        dos.writeInt(LATEST_PROTOCOL_VERSION)

        if (player.isOnline) player.sendPluginMessage(instance, reiNetworkKey.toString(), baos.toByteArray())
    }

    fun onPlayerQuit(player: Player) {
        playerProtocolVersions.remove(player.uniqueId)
    }

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

    private fun readString(data: DataInputStream): String {
        val length = data.readInt()

        if (length < 0) throw IndexOutOfBoundsException("Invalid string length: $length")

        val bytes = ByteArray(length)

        data.readFully(bytes)

        return String(bytes, Charsets.UTF_8)
    }

    private fun readSlotMap(data: DataInputStream): Map<Int, Int> {
        val size = data.readByte().toInt()
        val map = mutableMapOf<Int, Int>()

        repeat(size) { map[data.readByte().toInt()] = data.readByte().toInt() }

        return map
    }
}
