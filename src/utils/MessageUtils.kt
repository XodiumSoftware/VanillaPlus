package org.xodium.vanillaplus.utils

import org.bukkit.entity.Player
import org.xodium.vanillaplus.utils.MessageUtils.getTrackPlayerMessage
import org.xodium.vanillaplus.utils.MessageUtils.getUntrackPlayerMessage
import org.xodium.vanillaplus.utils.Utils.toIntArray
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/** Message utilities. */
internal object MessageUtils {
    /**
     * Builds a plugin message [ByteArray] using a [DataOutputStream] builder block.
     * @param block The builder block used to write data to the [DataOutputStream].
     * @return [ByteArray] containing the written message data.
     */
    private fun buildMessage(block: DataOutputStream.() -> Unit): ByteArray {
        val baos = ByteArrayOutputStream()

        DataOutputStream(baos).use { it.block() }

        return baos.toByteArray()
    }

    /**
     * Writes a [Player] NBT compound tag to this [DataOutputStream].
     * @param player The [Player] whose data should be written.
     * @param remove Whether to set the remove flag in the compound tag.
     */
    private fun DataOutputStream.writePlayerCompound(
        player: Player,
        remove: Boolean,
    ) {
        writeByte(2) // MessageType
        writeByte(10) // Compound tag type
        writeByte(1)
        writeUTF("r")
        writeByte(if (remove) 1 else 0)

        val uuidArray = player.uniqueId.toIntArray()

        writeByte(11)
        writeUTF("i")
        writeInt(uuidArray.size)

        for (i in uuidArray) writeInt(i)
    }

    /**
     * Creates a plugin message for setting the level/server ID.
     *
     * Message format:
     * - Byte 0: Message type identifier (0 = level ID)
     * - Int: The level/server ID value
     *
     * @param id The unique identifier for the current server/level.
     * @return [ByteArray] ready to be sent as a plugin message.
     */
    fun getLevelIdMessage(id: Int): ByteArray =
        buildMessage {
            writeByte(0)
            writeInt(id)
        }

    /**
     * Creates a handshake plugin message for initializing communication with the client.
     *
     * Message format:
     * - Byte 1: Message type identifier (1 = handshake)
     * - Int: Protocol version (3 = current Xaero map protocol version)
     *
     * @return [ByteArray] containing the handshake message.
     */
    fun getHandshakeMessage(): ByteArray =
        buildMessage {
            writeByte(1)
            writeInt(3)
        }

    /**
     * Creates a plugin message for tracking a [Player]'s position on the map.
     *
     * NBT Compound Tag contents:
     * - "r" (Byte): Remove flag (0 = add/update)
     * - "i" (Int Array): [Player]'s UUID split into 4 integers
     * - "x" (Double): [Player]'s X coordinate
     * - "y" (Double): [Player]'s Y coordinate
     * - "z" (Double): [Player]'s Z coordinate
     * - "d" ([String]): [Player]'s dimension/world identifier
     *
     * @param player The [Player] whose tracking data should be generated.
     * @return [ByteArray] containing the complete track message with NBT data.
     * @see getUntrackPlayerMessage For removing a [Player] from tracking.
     */
    fun getTrackPlayerMessage(player: Player): ByteArray =
        buildMessage {
            writePlayerCompound(player, remove = false)
            writeByte(6)
            writeUTF("x")
            writeDouble(player.x)
            writeByte(6)
            writeUTF("y")
            writeDouble(player.y)
            writeByte(6)
            writeUTF("z")
            writeDouble(player.z)
            writeByte(8)
            writeUTF("d")
            writeUTF(player.world.key.toString())
            writeByte(0) // Compound tag end
        }

    /**
     * Creates a plugin message for removing a [Player] from map tracking.
     *
     * NBT Compound Tag contents:
     * - "r" (Byte): Remove flag (1 = remove)
     * - "i" (Int Array): [Player]'s UUID split into 4 integers
     *
     * @param player The [Player] that should be removed from tracking.
     * @return [ByteArray] containing the untrack message.
     * @see getTrackPlayerMessage For adding/updating [Player] tracking.
     */
    fun getUntrackPlayerMessage(player: Player): ByteArray =
        buildMessage {
            writePlayerCompound(player, remove = true)
            writeByte(0) // Compound tag end
        }
}
