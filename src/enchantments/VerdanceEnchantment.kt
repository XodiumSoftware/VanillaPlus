package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.block.data.Ageable
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling verdance enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object VerdanceEnchantment : EnchantmentInterface {
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
     * Resets a fully-grown crop back to age 0 after it is broken with a Verdance-enchanted tool.
     * Only activates when the broken block is an [Ageable] at its maximum age and the player
     * holds a Verdance-enchanted item. The reset is scheduled 2 ticks later to allow the
     * break event to complete first.
     * @param event The [BlockBreakEvent] to handle.
     */
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val ageable = block.blockData as? Ageable ?: return
        val itemInHand = event.player.inventory.itemInMainHand

        if (ageable.age < ageable.maximumAge) return
        if (!itemInHand.containsEnchantment(get())) return

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { block.blockData = ageable.apply { age = 0 } },
            2,
        )
    }
}
