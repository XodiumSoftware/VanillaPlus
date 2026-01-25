package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.handlers.REINetworkHandler
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling recipe mechanics within the system. */
internal object RecipeModule : ModuleInterface {
    lateinit var recipeKeys: List<NamespacedKey>
        private set
    lateinit var reiNetworkKey: NamespacedKey
        private set
    lateinit var reiDeletePacketKey: NamespacedKey
        private set
    lateinit var reiCreateItemPacketKey: NamespacedKey
        private set

    private val networkHandler = REINetworkHandler

    init {
        val reiNetworkChannelName = reiNetworkKey.toString()
        val reiDeleteChannelName = reiDeletePacketKey.toString()
        val reiCreateItemChannelName = reiCreateItemPacketKey.toString()

        instance.server.messenger.registerIncomingPluginChannel(instance, reiNetworkChannelName, networkHandler)
        instance.server.messenger.registerOutgoingPluginChannel(instance, reiNetworkChannelName)
        instance.logger.info("Registered REI channel: $reiNetworkChannelName")

        instance.server.messenger.registerIncomingPluginChannel(instance, reiDeleteChannelName, networkHandler)
        instance.server.messenger.registerOutgoingPluginChannel(instance, reiDeleteChannelName)
        instance.logger.info("Registered REI delete item channel: $reiDeleteChannelName")

        instance.server.messenger.registerIncomingPluginChannel(instance, reiCreateItemChannelName, networkHandler)
        instance.logger.info("Registered REI create item channel: $reiCreateItemChannelName")
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        event.player.discoverRecipes(recipeKeys)
        networkHandler.sendHandshake(event.player)
    }

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        networkHandler.onPlayerQuit(event.player)
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
