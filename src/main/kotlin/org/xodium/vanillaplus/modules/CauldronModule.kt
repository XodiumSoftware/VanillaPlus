package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
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

    private val schedulerDelay = 0L
    private val schedulerPeriod = 20L

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
                schedulerDelay,
                schedulerPeriod,
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
                config.convertCoarseDirt && item.itemStack.type == Material.COARSE_DIRT -> Material.MUD
                config.convertRootedDirt && item.itemStack.type == Material.ROOTED_DIRT -> Material.MUD
                config.convertConcretePowder && Tag.CONCRETE_POWDER.isTagged(item.itemStack.type) -> {
                    Material.entries.firstOrNull {
                        it.name ==
                            item.itemStack.type.name
                                .removeSuffix("_POWDER")
                    }
                }

                else -> null
            }

        convertedMaterial?.let { convertItem(item, it, blockBelow, cauldronData) }
    }

    /**
     * Determines if a material is convertible by this cauldron module.
     * @param material the material to check for convertibility.
     * @return true if the material can be converted, false otherwise.
     */
    private fun isConvertible(material: Material): Boolean =
        (config.convertDirt && material == Material.DIRT) ||
            (config.convertCoarseDirt && material == Material.COARSE_DIRT) ||
            (config.convertRootedDirt && material == Material.ROOTED_DIRT) ||
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

        val newLevel = cauldronData.level - 1
        if (newLevel <= 0) {
            cauldronBlock.type = Material.CAULDRON
        } else {
            cauldronData.level = newLevel
            cauldronBlock.blockData = cauldronData
        }
    }

    @Serializable
    data class Config(
        override var enabled: Boolean = true,
        var convertConcretePowder: Boolean = true,
        var convertDirt: Boolean = true,
        var convertCoarseDirt: Boolean = true,
        var convertRootedDirt: Boolean = true,
    ) : ModuleInterface.Config
}
