package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.block.data.Ageable
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling replant enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object ReplantEnchantment : EnchantmentInterface {
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
    fun replant(event: BlockBreakEvent) {
        val block = event.block
        val ageable = block.blockData as? Ageable ?: return
        val itemInHand = event.player.inventory.itemInMainHand

        if (ageable.age < ageable.maximumAge) return
        if (!itemInHand.hasItemMeta() || !itemInHand.itemMeta.hasEnchant(get())) return

        // TODO: take seed out of drop. since planting would require a seed being used.

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                val blockType = block.type

                block.type = blockType
                block.blockData = ageable.apply { age = 0 }
            },
            2,
        )
    }
}
