package org.xodium.vanillaplus.utils

import org.bukkit.entity.Player
import org.xodium.vanillaplus.utils.Utils.toIntArray
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/** Message utilities. */
internal object MessageUtils {
    fun getLevelIdMessage(levelId: Int): ByteArray {
        ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use { dos ->
                dos.writeByte(0)
                dos.writeInt(levelId)
            }
            return baos.toByteArray()
        }
    }

    fun getHandshakeMessage(): ByteArray {
        ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use { dos ->
                dos.writeByte(1)
                dos.writeInt(3)
            }
            return baos.toByteArray()
        }
    }

    fun getTrackPlayerMessage(pl: Player): ByteArray {
        val baos = ByteArrayOutputStream()

        DataOutputStream(baos).use { dos ->
            // MessageType
            dos.writeByte(2)

            val uuidArray = pl.uniqueId.toIntArray()

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
            dos.writeDouble(pl.x)

            // Y position tag
            dos.writeByte(6) // Double tag type
            dos.writeUTF("y") // Tag name
            dos.writeDouble(pl.y)

            // Z position tag
            dos.writeByte(6) // Double tag type
            dos.writeUTF("z") // Tag name
            dos.writeDouble(pl.z)

            // Dimension tag
            dos.writeByte(8) // String tag type
            dos.writeUTF("d") // Tag name
            dos.writeUTF(pl.world.key.toString())

            // Compound tag end
            dos.writeByte(0)
        }

        return baos.toByteArray()
    }

    fun getUntrackPlayerMessage(pl: Player): ByteArray {
        val baos = ByteArrayOutputStream()

        DataOutputStream(baos).use { dos ->
            // MessageType
            dos.writeByte(2)

            val uuidArray = pl.uniqueId.toIntArray()

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

    fun getTrackResetMessage(): ByteArray = byteArrayOf(3) // MessageType only
}
