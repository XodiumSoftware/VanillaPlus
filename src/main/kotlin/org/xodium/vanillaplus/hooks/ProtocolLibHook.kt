package org.xodium.vanillaplus.hooks

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
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
                if (entityId != event.player.entityId) return

                val metadata = try {
                    event.packet.dataValueCollectionModifier.read(0)
                } catch (e: Exception) {
                    instance.logger.warning("Failed to read metadata for entity $entityId: ${e.message}")
                    return
                }

                val customNameIndex = 2
                metadata.removeIf { it.index == customNameIndex }

                val customNameJson = "{\"text\":\"TEST\"}"
                val customNameComponent = WrappedChatComponent.fromJson(customNameJson)
                val serializer = WrappedDataWatcher.Registry.getChatComponentSerializer()
                metadata.add(WrappedDataValue(customNameIndex, serializer, customNameComponent.handle))

                event.packet.dataValueCollectionModifier.write(0, metadata)
                instance.logger.info("Set custom name for player ${event.player.name} on entity $entityId")
            }
        })
    }
}