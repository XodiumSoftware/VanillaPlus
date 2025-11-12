@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.handlers

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.messaging.PluginMessageListener
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*

/** Handles plugin messages related to swapping [ArmorStand] equipment items. */
internal class SwapHandler : PluginMessageListener {
    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray,
    ) {
        if (channel != "armorposer:swap_packet") return

        try {
            ByteArrayInputStream(message).use { byteStream ->
                DataInputStream(byteStream).use { inputStream ->
                    val mostSigBits = inputStream.readLong()
                    val leastSigBits = inputStream.readLong()
                    val uuid = UUID(mostSigBits, leastSigBits)
                    val actionOrdinal = inputStream.readByte().toInt()
                    val action =
                        Action.entries.getOrNull(actionOrdinal) ?: run {
                            instance.logger.warning("Invalid action ordinal: $actionOrdinal")
                            return
                        }
                    val entity = instance.server.getEntity(uuid)

                    if (entity is ArmorStand) {
                        when (action) {
                            Action.SWAP_HANDS -> {
                                entity.setItem(EquipmentSlot.HAND, entity.getItem(EquipmentSlot.OFF_HAND))
                                entity.setItem(EquipmentSlot.OFF_HAND, entity.getItem(EquipmentSlot.HAND))
                            }

                            Action.SWAP_WITH_HEAD -> {
                                entity.setItem(EquipmentSlot.HAND, entity.getItem(EquipmentSlot.HEAD))
                                entity.setItem(EquipmentSlot.HEAD, entity.getItem(EquipmentSlot.HAND))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            instance.logger.warning("Failed to process swap packet: ${e.message}")
            e.printStackTrace()
        }
    }

    /** Represents an action that can be performed by swapping equipment on an [ArmorStand]. */
    enum class Action {
        SWAP_WITH_HEAD,
        SWAP_HANDS,
    }
}
