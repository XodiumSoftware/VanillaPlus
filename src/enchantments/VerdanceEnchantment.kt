package org.xodium.vanillaplus.enchantments

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling verdance enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object VerdanceEnchantment : EnchantmentInterface {
    override val guide by lazy {
        ItemStack.of(Material.DIAMOND_HOE).apply {
            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<!italic><b><gold>Verdance</gold></b>"))
            setData(
                DataComponentTypes.LORE,
                ItemLore
                    .lore()
                    .addLine(
                        MM.deserialize(
                            "<!italic><dark_gray>Slot: <gray>Hoe</gray> | Levels: <gray>I</gray></dark_gray>",
                        ),
                    ).addLine(MM.deserialize("<!italic>"))
                    .addLine(MM.deserialize("<!italic><dark_aqua>Automatically replants fully grown</dark_aqua>"))
                    .addLine(MM.deserialize("<!italic><dark_aqua>crops after harvest.</dark_aqua>"))
                    .build(),
            )
        }
    }

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(2)
            .maxLevel(1)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 0))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 0))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    /**
     * Automatically replants a crop block after it has been fully grown and harvested.
     * @param event The BlockBreakEvent triggered when a block is broken.
     */
    fun verdance(event: BlockBreakEvent) {
        val block = event.block
        val ageable = block.blockData as? Ageable ?: return
        val itemInHand = event.player.inventory.itemInMainHand

        if (ageable.age < ageable.maximumAge) return
        if (!itemInHand.hasItemMeta() || !itemInHand.itemMeta.hasEnchant(get())) return

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { block.blockData = ageable.apply { age = 0 } },
            2,
        )
    }
}
