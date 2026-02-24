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
    private const val WORLD_MAP_CHANNEL: String = "xaeroworldmap:main"
    private const val MINI_MAP_CHANNEL: String = "xaerominimap:main"

    init {
        if (isEnabled) {
            instance.server.messenger.apply {
                registerOutgoingPluginChannel(instance, WORLD_MAP_CHANNEL)
                registerOutgoingPluginChannel(instance, MINI_MAP_CHANNEL)
            }
        }
    }

    @EventHandler
    fun on(event: PlayerRegisterChannelEvent) {
        val channel = event.channel

        if (channel != WORLD_MAP_CHANNEL && channel != MINI_MAP_CHANNEL) return

        sendPlayerWorldId(event.player, channel)
    }

    @EventHandler
    fun on(event: PlayerChangedWorldEvent) {
        sendPlayerWorldId(event.player, WORLD_MAP_CHANNEL)
        sendPlayerWorldId(event.player, MINI_MAP_CHANNEL)
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
        val bytes = ByteStreams.newDataOutput()

        bytes.writeByte(0)
        bytes.writeInt(config.mapModule.serverId)

        player.sendPluginMessage(instance, channel, bytes.toByteArray())
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var serverId: Int = Random.nextInt(),
    )
}
