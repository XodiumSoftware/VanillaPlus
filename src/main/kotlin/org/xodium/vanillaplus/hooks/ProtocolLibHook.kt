package org.xodium.vanillaplus.hooks

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedDataValue
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** A utility object to check for ProtocolLib. */
object ProtocolLibHook {
    /** The ProtocolManager instance from ProtocolLib. */
    private val protocolManager = ProtocolLibrary.getProtocolManager()

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
     * Registers a metadata packet listener for entity metadata modifications.
     * @param callback Function to handle metadata modification
     */
    fun registerMetadataListener(
        callback: (entityId: Int, player: Player, metadata: MutableList<WrappedDataValue>) -> Unit
    ) {
        protocolManager.addPacketListener(object :
            PacketAdapter(instance, ListenerPriority.HIGHEST, PacketType.Play.Server.ENTITY_METADATA) {
            override fun onPacketSending(event: PacketEvent) {
                val entityId = event.packet.integers.read(0)
                val metadata = try {
                    event.packet.dataValueCollectionModifier.read(0)
                } catch (e: Exception) {
                    instance.logger.warning("Failed to read metadata for entity $entityId: ${e.message}")
                    return
                }

                callback(entityId, event.player, metadata)
                event.packet.dataValueCollectionModifier.write(0, metadata)
            }
        })
    }
}