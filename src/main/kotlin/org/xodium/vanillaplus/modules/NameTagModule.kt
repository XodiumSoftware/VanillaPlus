package org.xodium.vanillaplus.modules

import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.hooks.ProtocolLibHook
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling NameTag mechanics within the system. */
class NameTagModule : ModuleInterface<NameTagModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean {
        if (!config.enabled) return false

        return ProtocolLibHook.getPlugin("ProtocolLib not found, disabling NameTagModule")
    }

    init {
        if (enabled()) nametag()
    }

    private fun nametag() {
        ProtocolLibHook.registerMetadataListener { entityId, player, metadata ->
            if (entityId != player.entityId) return@registerMetadataListener

            val customNameIndex = 2
            metadata.removeIf { it.index == customNameIndex }

            val customNameJson = "{\"text\":\"${player.displayName()}\"}"
            val customNameComponent = WrappedChatComponent.fromJson(customNameJson)
            val serializer = WrappedDataWatcher.Registry.getChatComponentSerializer()
            metadata.add(WrappedDataValue(customNameIndex, serializer, customNameComponent.handle))

            instance.logger.info("Set custom name for player ${player.name} on entity $entityId")
        }
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}