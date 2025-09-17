package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/** Represents a module handling player mechanics within the system. */
internal class PlayerModule : ModuleInterface<PlayerModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerDeathEvent) {
        if (!enabled()) return
        if (event.entity.killer == null) return

        val victim = event.entity

        val skull = ItemStack.of(Material.PLAYER_HEAD, 1)
        val meta = skull.itemMeta as SkullMeta
        meta.owningPlayer = victim
        skull.itemMeta = meta

        val uuid = victim.uniqueId.toString()
        val apiUrl = "https://api.minecraft-heads.com/MHAPI/v2/heads?uuid=$uuid"
        try {
            val connection = URI(apiUrl).toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "VanillaPlus")
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().readText()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        victim.world.dropItemNaturally(victim.location, skull)
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
