@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.*
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.MessageUtils
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/** Represents a module handling map mechanics within the system. */
@OptIn(ExperimentalUuidApi::class)
internal object MapModule : ModuleInterface {
    private val lastTrackTime = mutableMapOf<Uuid, Long>()
    private val lastBlockPos = mutableMapOf<Uuid, Triple<Int, Int, Int>>()
    private const val TRACK_THROTTLE_MS = 150L

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
        if (event.channel !in setOf(Channel.WORLD_MAP, Channel.MINI_MAP)) return

        sendHandshake(event.player, event.channel)
        sendLevelId(event.player, event.channel)
        trackOthers(event.player)
    }

    @EventHandler
    fun on(event: PlayerChangedWorldEvent) {
        val player = event.player
        val uuid = player.uniqueId.toKotlinUuid()

        lastBlockPos.remove(uuid)
        lastTrackTime.remove(uuid)

        untrackPlayer(player)
        sendLevelId(player, Channel.WORLD_MAP)
        sendLevelId(player, Channel.MINI_MAP)
        trackOthers(player)
    }

    @EventHandler
    fun on(event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedPosition()) return

        val player = event.player
        val uuid = player.uniqueId.toKotlinUuid()
        val now = System.currentTimeMillis()
        val last = lastTrackTime[uuid] ?: 0L
        val blockPos = Triple(player.location.blockX, player.location.blockY, player.location.blockZ)
        val lastPos = lastBlockPos[uuid]

        if (now - last >= TRACK_THROTTLE_MS && blockPos != lastPos) {
            lastTrackTime[uuid] = now
            lastBlockPos[uuid] = blockPos
            if (isVisible(player)) {
                trackPlayer(player)
            } else if (lastPos != null) {
                untrackPlayer(player)
            }
        }
    }

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId.toKotlinUuid()

        lastBlockPos.remove(uuid)
        lastTrackTime.remove(uuid)

        untrackPlayer(event.player)
    }

    @EventHandler
    fun on(event: PlayerToggleSneakEvent) {
        if (event.isSneaking) untrackPlayer(event.player) else trackPlayer(event.player)
    }

    /**
     * Sends the initial handshake packet to the client over the specified channel.
     * @param player The target player receiving the handshake.
     * @param channel The plugin channel used for communication.
     */
    private fun sendHandshake(
        player: Player,
        channel: String,
    ) {
        player.sendPluginMessage(instance, channel, MessageUtils.getHandshakeMessage())
    }

    /**
     * Sends the configured server-level ID to the given player over the specified plugin channel.
     * @param player The target player who will receive the plugin message.
     * @param channel The plugin channel to send the message through.
     */
    private fun sendLevelId(
        player: Player,
        channel: String,
    ) {
        player.sendPluginMessage(instance, channel, MessageUtils.getLevelIdMessage(config.mapModule.serverId))
    }

    /**
     * Synchronizes all currently online players to the specified player.
     * @param player The player that should receive tracking data.
     */
    private fun trackOthers(player: Player) {
        for (other in player.world.players) if (other != player) trackPlayer(other)
    }

    /**
     * Broadcasts the current position of the specified player to all other online players.
     * @param player The player whose location should be synced.
     */
    private fun trackPlayer(player: Player) {
        if (!isVisible(player)) {
            untrackPlayer(player)
            return
        }

        val message = MessageUtils.getTrackPlayerMessage(player)

        for (other in player.world.players) {
            if (other != player) {
                other.sendPluginMessage(instance, Channel.MINI_MAP, message)
                other.sendPluginMessage(instance, Channel.WORLD_MAP, message)
            }
        }
    }

    /**
     * Broadcasts an untrack packet for the specified player to all remaining online players.
     * @param player The player that should be removed from tracking.
     */
    private fun untrackPlayer(player: Player) {
        val message = MessageUtils.getUntrackPlayerMessage(player)

        for (other in player.world.players) {
            if (other != player) {
                other.sendPluginMessage(instance, Channel.MINI_MAP, message)
                other.sendPluginMessage(instance, Channel.WORLD_MAP, message)
            }
        }
    }

    /**
     * Determines whether the specified player should be visible on the map.
     * @param player The player whose visibility should be checked.
     * @return True if the player should be tracked, false otherwise.
     */
    private fun isVisible(player: Player): Boolean = !player.isSneaking && !player.hasMetadata("vanished")

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var serverId: Int = Random.nextInt(),
    )
}
