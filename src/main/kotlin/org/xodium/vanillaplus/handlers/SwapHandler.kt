package org.xodium.vanillaplus.handlers

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.messaging.PluginMessageListener
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Handles plugin messages related to swapping [ArmorStand] equipment items. */
internal class SwapHandler : PluginMessageListener {
    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray,
    ) {
        if (channel != "armorposer:swap_packet") return

        val byteBuf = FriendlyByteBuf(Unpooled.wrappedBuffer(message))
        val uuid = byteBuf.readUUID()
        val action = byteBuf.readEnum(Action::class.java)
        val entity = instance.server.getEntity(uuid)

        if (entity is ArmorStand) {
            when (action) {
                Action.SWAP_HANDS -> {
                    entity.setItem(EquipmentSlot.OFF_HAND, entity.getItem(EquipmentSlot.HAND))
                    entity.setItem(EquipmentSlot.HAND, entity.getItem(EquipmentSlot.OFF_HAND))
                    return
                }

                Action.SWAP_WITH_HEAD -> {
                    entity.setItem(EquipmentSlot.HEAD, entity.getItem(EquipmentSlot.HAND))
                    entity.setItem(EquipmentSlot.HAND, entity.getItem(EquipmentSlot.HEAD))
                    return
                }

                else -> throw IllegalArgumentException("Invalid Pose action")
            }
        }
    }

    /** Represents an action that can be performed by swapping equipment on an [ArmorStand]. */
    enum class Action {
        SWAP_WITH_HEAD,
        SWAP_HANDS,
    }
}
