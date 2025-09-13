package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling cauldron mechanics within the system. */
internal class CauldronModule : ModuleInterface<CauldronModule.Config> {
    override val config: Config = Config()

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    for (world in instance.server.worlds) {
                        for (entity in world.entities) {
                            if (entity is Item && isConvertible(entity.itemStack.type)) {
                                checkItemInCauldron(entity)
                            }
                        }
                    }
                },
                0L,
                20L,
            )
        }
    }

    /**
     * Checks if an item entity is in a water cauldron and performs conversion if applicable.
     * @param item the item entity to check for cauldron conversion.
     */
    private fun checkItemInCauldron(item: Item) {
        val blockBelow = item.location.clone().block
        if (blockBelow.type != Material.WATER_CAULDRON) return

        val cauldronData = blockBelow.blockData as? Levelled ?: return
        if (cauldronData.level <= 0) return

        val convertedMaterial =
            when {
                config.convertDirt && item.itemStack.type == Material.DIRT -> Material.MUD
                config.convertConcretePowder && Tag.CONCRETE_POWDER.isTagged(item.itemStack.type) -> {
                    Material.entries.firstOrNull {
                        it.name ==
                            item.itemStack.type.name
                                .removeSuffix("_POWDER")
                    }
                }

                else -> null
            }

        convertedMaterial?.let { mat -> convertItem(item, mat, blockBelow, cauldronData) }
    }

    /**
     * Determines if a material is convertible by this cauldron module.
     * @param material the material to check for convertibility.
     * @return true if the material can be converted, false otherwise.
     */
    private fun isConvertible(material: Material): Boolean =
        (config.convertDirt && material == Material.DIRT) ||
            (config.convertConcretePowder && Tag.CONCRETE_POWDER.isTagged(material))

    /**
     * Converts an item to a new material and updates the cauldron state.
     * @param item the item entity to convert.
     * @param newMaterial the target material to convert the item to.
     * @param cauldronBlock the cauldron block where the conversion occurs.
     * @param cauldronData the levelled block data of the cauldron.
     */
    private fun convertItem(
        item: Item,
        newMaterial: Material,
        cauldronBlock: Block,
        cauldronData: Levelled,
    ) {
        item.remove()

        cauldronBlock.world.dropItemNaturally(
            cauldronBlock.location.add(0.5, 1.0, 0.5),
            ItemStack.of(newMaterial, item.itemStack.amount),
        )

        cauldronData.level = (cauldronData.level - 1).coerceAtLeast(0)
        cauldronBlock.blockData = cauldronData

        if (cauldronData.level == 0) cauldronBlock.type = Material.CAULDRON
    }

    data class Config(
        override var enabled: Boolean = true,
        var convertConcretePowder: Boolean = true,
        var convertDirt: Boolean = true,
    ) : ModuleInterface.Config
}
