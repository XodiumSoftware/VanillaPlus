package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling player mechanics within the system. */
internal class PlayerModule : ModuleInterface<PlayerModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerDeathEvent) {
        if (!enabled() || event.entity.killer == null) return

        val victim = event.entity
        val skull = ItemStack.of(Material.PLAYER_HEAD, 1)
        val meta = skull.itemMeta as SkullMeta
        if (meta.setOwningPlayer(victim)) {
            skull.itemMeta = meta
            victim.world.dropItemNaturally(victim.location, skull)
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
