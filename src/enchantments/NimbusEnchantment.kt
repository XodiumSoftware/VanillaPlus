package org.xodium.vanillaplus.enchantments

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.attribute.Attribute
import org.bukkit.entity.HappyGhast
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling nimbus enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object NimbusEnchantment : EnchantmentInterface {
    private const val DEFAULT_FLY_SPEED = 0.05
    private val SPEED_MODIFIER = mapOf(1 to 1.5, 2 to 2.0, 3 to 2.5, 4 to 3.0, 5 to 3.5)

    override val guide =
        listOf(
            listOf(
                "<b><gold>Nimbus</gold></b>",
                "<dark_gray>Slot: <gray>Saddle</gray></dark_gray>",
                "<dark_gray>Levels: <gray>I–V</gray></dark_gray>",
                "",
                "<dark_aqua>Boosts Happy Ghast</dark_aqua>",
                "<dark_aqua>flying speed.</dark_aqua>",
                "",
                "<dark_gray>Lv I   → <gray>×1.5</gray></dark_gray>",
                "<dark_gray>Lv II  → <gray>×2.0</gray></dark_gray>",
                "<dark_gray>Lv III → <gray>×2.5</gray></dark_gray>",
                "<dark_gray>Lv IV  → <gray>×3.0</gray></dark_gray>",
                "<dark_gray>Lv V   → <gray>×3.5</gray></dark_gray>",
            ),
        )

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(2)
            .maxLevel(5)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 5))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 10))
            .activeSlots(EquipmentSlotGroup.SADDLE)

    /**
     * Handles the event when an entity's equipment changes, specifically for Happy Ghasts with the nimbus enchantment.
     * @param event The event representing the change in entity equipment.
     */
    fun nimbus(event: EntityEquipmentChangedEvent) {
        val entity = event.entity as? HappyGhast ?: return
        val harness = entity.equipment.getItem(EquipmentSlot.BODY)
        val attribute = entity.getAttribute(Attribute.FLYING_SPEED)

        if (harness.hasItemMeta() && harness.itemMeta.hasEnchant(get())) {
            val enchantLevel = harness.itemMeta.getEnchantLevel(get())

            attribute?.baseValue = DEFAULT_FLY_SPEED * (SPEED_MODIFIER[enchantLevel] ?: return)
        } else {
            attribute?.baseValue = DEFAULT_FLY_SPEED
        }
    }
}
