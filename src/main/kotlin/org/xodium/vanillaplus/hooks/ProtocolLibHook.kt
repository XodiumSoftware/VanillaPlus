package org.xodium.vanillaplus.hooks

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedWatchableObject
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

    /** Sets a custom nametag for a player. */
    fun nametag() {
        protocolManager.addPacketListener(object :
            PacketAdapter(instance, ListenerPriority.HIGHEST, PacketType.Play.Server.ENTITY_METADATA) {
            override fun onPacketSending(event: PacketEvent) {
                val entityId = event.packet.integers.read(0)
                val player = instance.server.getPlayer(event.player.uniqueId)
                if (player != null && player.entityId == entityId) {
                    val metadata = event.packet.watchableCollectionModifier.read(0)
                    val customNameIndex = 3
                    val customName = WrappedChatComponent.fromJson("{\"text\":\"Your Custom Name\"}")
                    val newMetadata = metadata.map { watcher ->
                        if (watcher.index == customNameIndex) {
                            WrappedWatchableObject(customNameIndex, customName)
                        } else {
                            watcher
                        }
                    }
                    event.packet.watchableCollectionModifier.write(0, newMetadata)
                }
            }
        })
    }
}