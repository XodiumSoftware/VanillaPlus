package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling armor stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        if (!enabled() || event.rightClicked !is ArmorStand || event.hand != EquipmentSlot.HAND) return

        val armorStand = event.rightClicked as ArmorStand
        val player = event.player
        val itemInHand = player.inventory.itemInMainHand
        val armorStandHandItem = armorStand.getItem(EquipmentSlot.HAND)
        if (itemInHand.type.isAir) {
            if (!armorStandHandItem.type.isAir) {
                giveItemToPlayer(player, armorStandHandItem)
                armorStand.setItem(EquipmentSlot.HAND, ItemStack.of(Material.AIR))
                event.isCancelled = true
            }
        } else {
            if (isTool(itemInHand.type) && armorStandHandItem.type.isAir) {
                armorStand.setItem(EquipmentSlot.HAND, itemInHand.clone())
                player.inventory.setItemInMainHand(null)
                event.isCancelled = true
            }
        }
    }

    private fun giveItemToPlayer(
        player: Player,
        item: ItemStack,
    ) {
        val remainingItems = player.inventory.addItem(item.clone())
        if (remainingItems.isNotEmpty()) {
            for (remainingItem in remainingItems.values) {
                player.world.dropItem(player.location, remainingItem)
            }
        }
    }

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
