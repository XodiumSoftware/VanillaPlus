@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.google.common.io.ByteStreams
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.random.Random

/** Represents a module handling map mechanics within the system. */
internal object MapModule : ModuleInterface {
    private object Channel {
        const val WORLD_MAP: String = "xaeroworldmap:main"
        const val MINI_MAP: String = "xaerominimap:main"
    }

    init {
        if (isEnabled) {
            instance.server.messenger.apply {
                registerOutgoingPluginChannel(instance, Channel.WORLD_MAP)
                registerOutgoingPluginChannel(instance, Channel.MINI_MAP)
            }
        }
    }

    @EventHandler
    fun on(event: PlayerRegisterChannelEvent) {
        if (event.channel in setOf(Channel.WORLD_MAP, Channel.MINI_MAP)) sendPlayerWorldId(event.player, event.channel)
    }

    @EventHandler
    fun on(event: PlayerChangedWorldEvent) {
        sendPlayerWorldId(event.player, Channel.WORLD_MAP)
        sendPlayerWorldId(event.player, Channel.MINI_MAP)
    }

    /**
     * Sends the configured server-level ID to the given player over the specified plugin channel.
     * @param player The target player who will receive the plugin message.
     * @param channel The plugin channel to send the message through.
     */
    private fun sendPlayerWorldId(
        player: Player,
        channel: String,
    ) {
        player.sendPluginMessage(
            instance,
            channel,
            ByteStreams
                .newDataOutput()
                .apply {
                    writeByte(0)
                    writeInt(config.mapModule.serverId)
                }.toByteArray(),
        )
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var serverId: Int = Random.nextInt(),
    )
}
