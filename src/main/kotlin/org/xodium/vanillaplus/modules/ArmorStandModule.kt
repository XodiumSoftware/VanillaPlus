package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling armor stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        if (!enabled() || event.rightClicked !is ArmorStand) return

        val armorStand = event.rightClicked as ArmorStand
        val player = event.player
        val itemInHand = player.inventory.itemInMainHand
        val equipment = armorStand.equipment

        when {
            isHoldingTool(itemInHand) -> handleToolPlacement(armorStand, equipment, itemInHand, player)
            isEmptyHand(itemInHand) -> handleEmptyHandInteraction(armorStand, equipment)
        }
    }

    private fun isHoldingTool(item: ItemStack) = item.type != Material.AIR && isTool(item.type)

    private fun isEmptyHand(item: ItemStack) = item.type == Material.AIR

    private fun handleToolPlacement(
        armorStand: ArmorStand,
        equipment: EntityEquipment,
        tool: ItemStack,
        player: Player,
    ) {
        when {
            equipment.itemInMainHand.type == Material.AIR -> {
                equipment.setItemInMainHand(tool)
                updateArmorStandState(armorStand, player)
            }

            equipment.itemInOffHand.type == Material.AIR -> {
                equipment.setItemInOffHand(tool)
                updateArmorStandState(armorStand, player)
            }
        }
    }

    private fun handleEmptyHandInteraction(
        armorStand: ArmorStand,
        equipment: EntityEquipment,
    ) {
        val hasItemsInHands =
            equipment.itemInMainHand.type != Material.AIR || equipment.itemInOffHand.type != Material.AIR
        val noItemsInHands =
            equipment.itemInMainHand.type == Material.AIR && equipment.itemInOffHand.type == Material.AIR

        if (hasItemsInHands && noItemsInHands) {
            armorStand.setArms(false)
        }
    }

    private fun updateArmorStandState(
        armorStand: ArmorStand,
        player: Player,
    ) {
        armorStand.setArms(true)
        player.inventory.setItemInMainHand(ItemStack.of(Material.AIR))
    }

    /**
     * Checks if the given material represents a tool item.
     * @param material the [Material] to check.
     * @return `true` if the material is a tool, `false` otherwise.
     */
    private fun isTool(material: Material): Boolean =
        Tag.ITEMS_AXES.isTagged(material) ||
            Tag.ITEMS_PICKAXES.isTagged(material) ||
            Tag.ITEMS_SHOVELS.isTagged(material) ||
            Tag.ITEMS_HOES.isTagged(material) ||
            Tag.ITEMS_SWORDS.isTagged(material) ||
            material == Material.FISHING_ROD ||
            material == Material.FLINT_AND_STEEL ||
            material == Material.SHEARS ||
            material == Material.BOW ||
            material == Material.CROSSBOW ||
            material == Material.TRIDENT ||
            material == Material.SHIELD ||
            material == Material.CARROT_ON_A_STICK ||
            material == Material.WARPED_FUNGUS_ON_A_STICK

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
