package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.managers.ManaManager
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling voidpull enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object VoidpullEnchantment : EnchantmentInterface {
    object Config {
        const val MANA_COST = 20
        const val RANGE = 30.0
        val PULL_SOUND: Sound = Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.HOSTILE, 1.0f, 0.8f)
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
            .exclusiveWith(
                RegistrySet.keySet(
                    RegistryKey.ENCHANTMENT,
                    InfernoEnchantment.key,
                    SkysunderEnchantment.key,
                    WitherbrandEnchantment.key,
                    FrostbindEnchantment.key,
                    TempestEnchantment.key,
                ),
            )

    /**
     * Handles a left-click interaction to pull a targeted entity to the player via Voidpull.
     * Ray-traces up to [Config.RANGE] blocks for an entity; if found, teleports it directly in front of the player.
     * @param event The [PlayerInteractEvent] to handle.
     */
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = ManaManager.consumeMana(event, get(), Config.MANA_COST) ?: return
        val result = player.rayTraceEntities(Config.RANGE.toInt()) ?: return
        val target = result.hitEntity ?: return
        val destination =
            player.location.add(
                player.location.direction
                    .normalize()
                    .multiply(2.0),
            )

        destination.y = player.location.y

        Particle.PORTAL
            .builder()
            .location(target.location.add(0.0, 1.0, 0.0))
            .count(30)
            .offset(0.3, 0.5, 0.3)
            .spawn()

        target.teleport(destination)

        Particle.PORTAL
            .builder()
            .location(destination.add(0.0, 1.0, 0.0))
            .count(30)
            .offset(0.3, 0.5, 0.3)
            .spawn()

        player.playSound(Config.PULL_SOUND)
    }
}
