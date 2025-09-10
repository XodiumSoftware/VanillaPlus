package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.data.Levelled
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling cauldron mechanics within the system. */
internal class CauldronModule : ModuleInterface<CauldronModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerDropItemEvent) {
        if (!enabled()) return

        val item = event.itemDrop
        val blockBelow = item.location.block
        if (blockBelow.type != Material.WATER_CAULDRON) return

        val mat = when {
            config.convertDirt && item.itemStack.type == Material.DIRT -> Material.MUD
            config.convertConcretePowder && Tag.CONCRETE_POWDER.isTagged(item.itemStack.type) ->
                Material.entries.firstOrNull { it.name == item.itemStack.type.name.removeSuffix("_POWDER") }

            else -> null
        }

        mat?.let {
            item.remove()
            blockBelow.world.dropItemNaturally(
                blockBelow.location.add(0.5, 0.5, 0.5),
                ItemStack.of(it)
            )

            val data = blockBelow.blockData
            if (data is Levelled) {
                data.level = (data.level - 1).coerceAtLeast(0)
                blockBelow.blockData = data
                if (data.level == 0) blockBelow.type = Material.CAULDRON
            }
        }
    }


    data class Config(
        override var enabled: Boolean = true,
        var convertConcretePowder: Boolean = true,
        var convertDirt: Boolean = true,
    ) : ModuleInterface.Config
}
