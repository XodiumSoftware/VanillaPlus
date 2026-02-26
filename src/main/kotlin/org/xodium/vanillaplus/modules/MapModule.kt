@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.google.common.io.ByteStreams
import kotlinx.serialization.Serializable
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.*
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
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
        sendPlayerWorldId(event.player, event.channel)
        trackOthers(event.player)
    }

    @EventHandler
    fun on(event: PlayerChangedWorldEvent) {
        val player = event.player

        untrackPlayer(player)
        sendPlayerWorldId(player, Channel.WORLD_MAP)
        sendPlayerWorldId(player, Channel.MINI_MAP)
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
        player.sendPluginMessage(
            instance,
            channel,
            ByteStreams
                .newDataOutput()
                .apply {
                    writeByte(1)
                    writeInt(1)
                }.toByteArray(),
        )
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

        val location = player.location
        val bytes =
            ByteStreams
                .newDataOutput()
                .apply {
                    writeByte(2)
                    writeLong(player.uniqueId.mostSignificantBits)
                    writeLong(player.uniqueId.leastSignificantBits)
                    writeDouble(location.x)
                    writeDouble(location.y)
                    writeDouble(location.z)
                    writeFloat(location.yaw)
                    writeInt(dimensionId(player.world))
                }.toByteArray()

        for (other in player.world.players) {
            if (other != player) {
                other.sendPluginMessage(instance, Channel.MINI_MAP, bytes)
                other.sendPluginMessage(instance, Channel.WORLD_MAP, bytes)
            }
        }
    }

    /**
     * Broadcasts an untrack packet for the specified player to all remaining online players.
     * @param player The player that should be removed from tracking.
     */
    private fun untrackPlayer(player: Player) {
        val bytes =
            ByteStreams
                .newDataOutput()
                .apply {
                    writeByte(3)
                    writeLong(player.uniqueId.mostSignificantBits)
                    writeLong(player.uniqueId.leastSignificantBits)
                }.toByteArray()

        for (other in player.world.players) {
            if (other != player) {
                other.sendPluginMessage(instance, Channel.MINI_MAP, bytes)
                other.sendPluginMessage(instance, Channel.WORLD_MAP, bytes)
            }
        }
    }

    /**
     * Resolves dimension id for the given world.
     *
     * Overworld is mapped to 0, Nether to -1, and The End to 1.
     * Any unknown environment defaults to 0.
     *
     * @param world The world whose dimension id should be resolved.
     * @return The corresponding Xaero dimension id.
     */
    private fun dimensionId(world: World): Int =
        when (world.environment) {
            World.Environment.NORMAL -> 0
            World.Environment.NETHER -> -1
            World.Environment.THE_END -> 1
            else -> 0
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
