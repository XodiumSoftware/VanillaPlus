package org.xodium.vanillaplus.features

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.FeatureInterface

/** Represents a feature handling cauldron mechanics within the system. */
internal object CauldronFeature : FeatureInterface {
    private val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractEvent) = cauldron(event)

    /**
     * Handles all cauldron interaction mechanics for right-click conversions.
     * @param event the interaction event from the player.
     */
    fun cauldron(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return

        if (block.type != Material.WATER_CAULDRON) return

        val player = event.player

        if (!player.isSneaking) return

        val item = event.item ?: return

        if (!isConvertible(item.type)) return

        val cauldronData = block.blockData as? Levelled ?: return

        if (cauldronData.level <= 0) return

        val converted = getConvertedMaterial(item.type) ?: return

        convertHeldItem(player, item, converted, block, cauldronData)
    }

    /**
     * Determines the conversion result for a given material.
     * @param material the material to convert.
     * @return the resulting material or null if not convertible.
     */
    private fun getConvertedMaterial(material: Material): Material? =
        when {
            config.convertDirt && material == Material.DIRT -> Material.MUD
            config.convertCoarseDirt && material == Material.COARSE_DIRT -> Material.MUD
            config.convertRootedDirt && material == Material.ROOTED_DIRT -> Material.MUD
            config.convertConcretePowder && Tag.CONCRETE_POWDER.isTagged(material) -> {
                Material.entries.firstOrNull {
                    it.name == material.name.removeSuffix("_POWDER")
                }
            }

            else -> null
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
     * Converts the held item and drains cauldron water.
     * @param player the player performing the action.
     * @param item the held item stack.
     * @param newMat the result material.
     * @param cauldronBlock the cauldron block.
     * @param cauldronData the levelled cauldron data.
     */
    private fun convertHeldItem(
        player: Player,
        item: ItemStack,
        newMat: Material,
        cauldronBlock: Block,
        cauldronData: Levelled,
    ) {
        item.amount -= 1
        player.inventory.addItem(ItemStack.of(newMat))

        val newLevel = cauldronData.level - 1

        if (newLevel <= 0) {
            cauldronBlock.type = Material.CAULDRON
        } else {
            cauldronData.level = newLevel
            cauldronBlock.blockData = cauldronData
        }
    }

    data class Config(
        var convertConcretePowder: Boolean = true,
        var convertDirt: Boolean = true,
        var convertCoarseDirt: Boolean = true,
        var convertRootedDirt: Boolean = true,
    )
}
