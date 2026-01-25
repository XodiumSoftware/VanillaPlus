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
    lateinit var reiNetworkKey: NamespacedKey
        private set

    private val recipeKeys = mutableSetOf<NamespacedKey>()
    private val networkHandler = REINetworkHandler

    init {
        loadRecipeKeys()
        registerChannels()
    }

    private fun loadRecipeKeys() {
        val iterator = instance.server.recipeIterator()
        while (iterator.hasNext()) {
            val recipe = iterator.next()
            val key = recipe.key

            if (key != null) recipeKeys.add(key)
        }
        instance.logger.info("Loaded ${recipeKeys.size} recipe keys")
    }

    /** Initializes the module, setting up necessary keys. */
    fun registerChannels() {
        instance.server.messenger.registerIncomingPluginChannel(instance, reiNetworkKey.toString(), networkHandler)
        instance.server.messenger.registerOutgoingPluginChannel(instance, reiNetworkKey.toString())
        instance.logger.info("Registered REI channel: $reiNetworkKey")
    }

    /** Unregisters the module's channels from the server. */
    fun unregisterChannels() {
        instance.server.messenger.unregisterIncomingPluginChannel(instance, reiNetworkKey.toString(), networkHandler)
        instance.server.messenger.unregisterOutgoingPluginChannel(instance, reiNetworkKey.toString())
        instance.logger.info("Unregistered REI channel: $reiNetworkKey")
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
