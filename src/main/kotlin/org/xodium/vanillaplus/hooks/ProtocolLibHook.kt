package org.xodium.vanillaplus.hooks

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** A utility object to check for ProtocolLib. */
object ProtocolLibHook {
    /** The ProtocolManager instance from ProtocolLib. */
    val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    /**
     * Checks if ProtocolLib is installed on the server.
     * @param log The log message to display if ProtocolLib is not found.
     * @return true if ProtocolLib is present, false otherwise.
     */
    fun getPlugin(log: String = "ProtocolLib not found"): Boolean {
        val plugin = instance.server.pluginManager.getPlugin("ProtocolLib") != null
        if (!plugin) instance.logger.warning(log)
        return plugin
    }

    /**
     * Sets a custom nametag for a player.
     * @param player The player whose nametag will be customized.
     */
    fun nametag(player: Player) {
        protocolManager.addPacketListener(object : PacketAdapter(instance, PacketType.Play.Server.PLAYER_INFO) {
            override fun onPacketSending(event: PacketEvent) {
                val packet = event.packet
                val playerInfoDataList = packet.playerInfoDataLists.read(0)
                playerInfoDataList.forEach { data ->
                    if (data.profile.name == player.name) {
                        data.displayName = WrappedChatComponent.fromText(player.displayName().toString())
                    }
                }
            }
        })
    }
}