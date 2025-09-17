package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

/** Represents a module handling player mechanics within the system. */
internal class PlayerModule : ModuleInterface<PlayerModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerDeathEvent) {
        if (!enabled()) return
        val killer = event.entity.killer ?: return

        val entity = event.entity
        val itemStack = ItemStack.of(Material.PLAYER_HEAD)
        val itemMeta = itemStack.itemMeta as SkullMeta
        if (itemMeta.setOwningPlayer(entity)) {
            itemMeta.customName(entity.displayName().append("'s Skull".fireFmt().mm()))
            itemMeta.lore(listOf("${entity.name} killed by ${killer.name}").mm())
            itemStack.itemMeta = itemMeta
            entity.world.dropItemNaturally(entity.location, itemStack)
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
