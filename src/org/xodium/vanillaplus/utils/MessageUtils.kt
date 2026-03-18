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
     * Creates a plugin message for setting the level/server ID.
     *
     * This message informs the client which server/world level they are connected to,
     * allowing the map mod to properly organize and separate maps from different servers.
     *
     * Message format:
     * - Byte 0: Message type identifier (0 = level ID)
     * - Int: The level/server ID value
     *
     * @param id The unique identifier for the current server/level.
     * @return Byte array ready to be sent as a plugin message.
     */
    fun getLevelIdMessage(id: Int): ByteArray {
        ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use { dos ->
                dos.writeByte(0)
                dos.writeInt(id)
            }
            return baos.toByteArray()
        }
    }

    /**
     * Creates a handshake plugin message for initializing communication with the client.
     *
     * This message is sent when a player registers the plugin channel, establishing
     * the protocol version and confirming compatibility between the server and client.
     *
     * Message format:
     * - Byte 1: Message type identifier (1 = handshake)
     * - Int: Protocol version (3 = current Xaero map protocol version)
     *
     * @return Byte array containing the handshake message.
     */
    fun getHandshakeMessage(): ByteArray {
        ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use { dos ->
                dos.writeByte(1)
                dos.writeInt(3)
            }
            return baos.toByteArray()
        }
    }

    /**
     * Creates a plugin message for tracking a player's position on the map.
     *
     * This message contains comprehensive player data formatted as NBT tags,
     * allowing the map mod to display the player's current position, dimension,
     * and other relevant information to other players on the same server.
     *
     * Message structure:
     * - MessageType (Byte: 2): Identifies this as a track/update message
     * - NBT Compound Tag: Contains all player data
     *   - "r" (Byte): Remove flag (0 = false - this is an add/update operation)
     *   - "i" (Int Array): Player's UUID split into 4 integers
     *   - "x" (Double): Player's X coordinate
     *   - "y" (Double): Player's Y coordinate
     *   - "z" (Double): Player's Z coordinate
     *   - "d" (String): Player's dimension/world identifier
     *
     * The NBT compound format follows Minecraft's NBT specification:
     * - Tag type (Byte): Identifies the type of the following tag
     * - Tag name (UTF): Name of the tag
     * - Tag value: The actual data (format depends on tag type)
     *
     * @param player The player whose tracking data should be generated.
     * @return Byte array containing the complete track message with NBT data.
     *
     * @see getUntrackPlayerMessage For removing a player from tracking.
     */
    fun getTrackPlayerMessage(player: Player): ByteArray {
        val baos = ByteArrayOutputStream()

        DataOutputStream(baos).use { dos ->
            // MessageType
            dos.writeByte(2)

            val uuidArray = player.uniqueId.toIntArray()

            // Compound tag start
            dos.writeByte(10) // Compound tag type

            // Remove tag
            dos.writeByte(1) // Tag type (byte)
            dos.writeUTF("r") // Tag name
            dos.writeByte(0) // Remove value (false)

            // UUID array tag
            dos.writeByte(11) // Int array tag type
            dos.writeUTF("i") // Tag name
            dos.writeInt(uuidArray.size)

            for (i in uuidArray) dos.writeInt(i)

            // X position tag
            dos.writeByte(6) // Double tag type
            dos.writeUTF("x") // Tag name
            dos.writeDouble(player.x)

            // Y position tag
            dos.writeByte(6) // Double tag type
            dos.writeUTF("y") // Tag name
            dos.writeDouble(player.y)

            // Z position tag
            dos.writeByte(6) // Double tag type
            dos.writeUTF("z") // Tag name
            dos.writeDouble(player.z)

            // Dimension tag
            dos.writeByte(8) // String tag type
            dos.writeUTF("d") // Tag name
            dos.writeUTF(player.world.key.toString())

            // Compound tag end
            dos.writeByte(0)
        }

        return baos.toByteArray()
    }

    /**
     * Creates a plugin message for removing a player from map tracking.
     *
     * This message tells the client to stop displaying a specific player on the map.
     * Used when a player leaves, changes worlds, becomes invisible (sneaking/vanished),
     * or should otherwise no longer be visible to other players.
     *
     * Message structure:
     * - MessageType (Byte: 2): Identifies this as a track/update message
     * - NBT Compound Tag: Contains minimal data for removal
     *   - "r" (Byte): Remove flag (1 = true - this is a removal operation)
     *   - "i" (Int Array): Player's UUID split into 4 integers
     *
     * The compound only needs the UUID and remove flag, as no position data is
     * required for removal operations.
     *
     * @param player The player that should be removed from tracking.
     * @return Byte array containing the untrack message.
     *
     * @see getTrackPlayerMessage For adding/updating player tracking.
     */
    fun getUntrackPlayerMessage(player: Player): ByteArray {
        val baos = ByteArrayOutputStream()

        DataOutputStream(baos).use { dos ->
            // MessageType
            dos.writeByte(2)

            val uuidArray = player.uniqueId.toIntArray()

            // Compound tag start
            dos.writeByte(10) // Compound tag type

            // Remove tag
            dos.writeByte(1) // Tag type (byte)
            dos.writeUTF("r") // Tag name
            dos.writeByte(1) // Remove value (true)

            // UUID array tag
            dos.writeByte(11) // Int array tag type
            dos.writeUTF("i") // Tag name
            dos.writeInt(uuidArray.size)

            for (i in uuidArray) dos.writeInt(i)

            // Compound tag end
            dos.writeByte(0)
        }

        return baos.toByteArray()
    }
}
