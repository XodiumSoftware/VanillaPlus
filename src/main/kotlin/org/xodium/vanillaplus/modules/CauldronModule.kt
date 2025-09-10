package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.data.Levelled
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPhysicsEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling cauldron mechanics within the system. */
internal class CauldronModule : ModuleInterface<CauldronModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: BlockPhysicsEvent) {
        if (!config.enabled) return

        val block = event.block
        if (!Tag.CONCRETE_POWDER.isTagged(block.type)) return

        val below = block.getRelative(0, -1, 0)
        if (below.type != Material.WATER_CAULDRON) return

        val converted = when {
            config.convertConcretePowder && Tag.CONCRETE_POWDER.isTagged(block.type) ->
                Material.entries.firstOrNull { it.name == block.type.name.removeSuffix("_POWDER") }

            config.convertDirt && block.type == Material.DIRT -> Material.MUD
            else -> null
        }
        converted?.let {
            block.type = it
            val data = below.blockData
            if (data is Levelled) {
                data.level = (data.level - 1).coerceAtLeast(0)
                below.blockData = data
                if (data.level == 0) below.type = Material.CAULDRON
            }
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var convertConcretePowder: Boolean = true,
        var convertDirt: Boolean = true,
    ) : ModuleInterface.Config
}
