package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.Particle
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.managers.ManaManager
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling skysunder enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object SkysunderEnchantment : EnchantmentInterface {
    object Config {
        const val MANA_COST = 20
        const val RANGE = 30.0
    }

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(4)
            .maxLevel(1)
            .weight(1)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(20, 5))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 5))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    /**
     * Handles a left-click interaction to call down a lightning strike via Skysunder.
     * @param event The [PlayerInteractEvent] to handle.
     */
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = ManaManager.consumeMana(event, get(), Config.MANA_COST) ?: return
        val result = player.rayTraceBlocks(Config.RANGE)
        val target =
            result?.hitPosition?.toLocation(player.world)
                ?: player.eyeLocation.add(
                    player.location.direction
                        .normalize()
                        .multiply(Config.RANGE),
                )

        Particle.ELECTRIC_SPARK
            .builder()
            .location(target)
            .count(30)
            .offset(0.3, 0.5, 0.3)
            .spawn()

        player.world.strikeLightning(target)
    }
}
