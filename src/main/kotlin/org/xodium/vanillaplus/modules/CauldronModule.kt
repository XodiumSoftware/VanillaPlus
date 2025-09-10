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
        if (below.type == Material.WATER_CAULDRON) {
            val concreteType = when (block.type) {
                Material.WHITE_CONCRETE_POWDER -> Material.WHITE_CONCRETE
                Material.ORANGE_CONCRETE_POWDER -> Material.ORANGE_CONCRETE
                Material.MAGENTA_CONCRETE_POWDER -> Material.MAGENTA_CONCRETE
                Material.LIGHT_BLUE_CONCRETE_POWDER -> Material.LIGHT_BLUE_CONCRETE
                Material.YELLOW_CONCRETE_POWDER -> Material.YELLOW_CONCRETE
                Material.LIME_CONCRETE_POWDER -> Material.LIME_CONCRETE
                Material.PINK_CONCRETE_POWDER -> Material.PINK_CONCRETE
                Material.GRAY_CONCRETE_POWDER -> Material.GRAY_CONCRETE
                Material.LIGHT_GRAY_CONCRETE_POWDER -> Material.LIGHT_GRAY_CONCRETE
                Material.CYAN_CONCRETE_POWDER -> Material.CYAN_CONCRETE
                Material.PURPLE_CONCRETE_POWDER -> Material.PURPLE_CONCRETE
                Material.BLUE_CONCRETE_POWDER -> Material.BLUE_CONCRETE
                Material.BROWN_CONCRETE_POWDER -> Material.BROWN_CONCRETE
                Material.GREEN_CONCRETE_POWDER -> Material.GREEN_CONCRETE
                Material.RED_CONCRETE_POWDER -> Material.RED_CONCRETE
                Material.BLACK_CONCRETE_POWDER -> Material.BLACK_CONCRETE
                else -> null
            }

            concreteType?.let {
                block.type = it
                val data = below.blockData
                if (data is Levelled) {
                    data.level = (data.level - 1).coerceAtLeast(0)
                    below.blockData = data
                    if (data.level == 0) below.type = Material.CAULDRON
                }
            }
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
