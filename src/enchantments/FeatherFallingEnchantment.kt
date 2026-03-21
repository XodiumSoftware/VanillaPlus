package org.xodium.vanillaplus.enchantments

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents an object handling feather falling enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object FeatherFallingEnchantment : EnchantmentInterface {
    override val guide by lazy {
        ItemStack.of(Material.GOLDEN_BOOTS).apply {
            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<!italic><b><gold>Feather Falling</gold></b>"))
            setData(
                DataComponentTypes.LORE,
                ItemLore
                    .lore()
                    .addLine(MM.deserialize("<!italic><dark_gray>Slot: <gray>Boots</gray></dark_gray>"))
                    .addLine(MM.deserialize("<!italic>"))
                    .addLine(MM.deserialize("<!italic><dark_aqua>Prevents trampling farmland when worn.</dark_aqua>"))
                    .addLine(MM.deserialize("<!italic>"))
                    .addLine(MM.deserialize("<!italic><gray><i>Vanilla enchantment, extended behaviour.</i></gray>"))
                    .build(),
            )
        }
    }

    /**
     * Handles the PlayerInteractEvent to prevent farmland trampling when wearing boots with Feather Falling enchantment.
     * @param event The PlayerInteractEvent to handle.
     */
    fun featherFalling(event: PlayerInteractEvent) {
        when {
            event.action != Action.PHYSICAL -> return
            event.clickedBlock?.type != Material.FARMLAND -> return
            !isValidTool(event.player.inventory.boots) -> return
            else -> event.isCancelled = true
        }
    }

    /**
     * Checks if the item is a pickaxe with Silk Touch.
     * @param item The item to check.
     * @return `true` if the item is a pickaxe with Silk Touch, otherwise `false`.
     */
    private fun isValidTool(item: ItemStack?): Boolean =
        item?.let {
            Tag.ITEMS_FOOT_ARMOR.isTagged(it.type) && it.containsEnchantment(Enchantment.FEATHER_FALLING)
        } ?: false
}
