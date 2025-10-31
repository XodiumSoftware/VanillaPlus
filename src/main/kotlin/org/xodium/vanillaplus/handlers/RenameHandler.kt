package org.xodium.vanillaplus.handlers

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.GameMode
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Handles plugin messages related to renaming an ArmorStand entity. */
internal class RenameHandler : PluginMessageListener {
    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray,
    ) {
        if (channel != "armorposer:rename_packet") return

        val byteBuf = FriendlyByteBuf(Unpooled.wrappedBuffer(message))
        val uuid = byteBuf.readUUID()
        val name = byteBuf.readUtf()
        val entity = instance.server.getEntity(uuid)

        if (name.isNotEmpty() && entity is ArmorStand && (player.level >= 1 || player.gameMode == GameMode.CREATIVE)) {
            player.giveExpLevels(-1)
            entity.customName(name.mm())
        }
    }
}
