package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.managers.ManaManager
import org.xodium.vanillaplus.pdcs.PlayerPDC.mana
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling bloodpact enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object BloodpactEnchantment : EnchantmentInterface {
    object Config {
        const val HEALTH_COST = 4.0
        const val MANA_GAIN = 40
        val CAST_SOUND: Sound = Sound.sound(Key.key("entity.player.hurt"), Sound.Source.PLAYER, 1.0f, 0.6f)
    }

    private val ACTIVE_GAME_MODES = setOf(GameMode.SURVIVAL, GameMode.ADVENTURE)

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
                    VoidpullEnchantment.key,
                ),
            )

    /**
     * Handles a left-click interaction to sacrifice health for mana via Bloodpact.
     * Costs [Config.HEALTH_COST] HP and restores [Config.MANA_GAIN] mana.
     * Requires the player to have more health than [Config.HEALTH_COST] to prevent a lethal drain.
     * @param event The [PlayerInteractEvent] to handle.
     */
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_AIR && event.action != Action.LEFT_CLICK_BLOCK) return

        val item = event.item ?: return

        if (item.type != Material.BLAZE_ROD) return
        if (!item.containsEnchantment(get())) return

        val player = event.player

        event.isCancelled = true

        if (player.gameMode !in ACTIVE_GAME_MODES) return
        if (player.mana >= ManaManager.Config.MAX_MANA) {
            player.playSound(ManaManager.NO_MANA_SOUND)
            return
        }
        if (player.health <= Config.HEALTH_COST) {
            player.playSound(ManaManager.NO_MANA_SOUND)
            return
        }

        player.health -= Config.HEALTH_COST
        player.mana = (player.mana + Config.MANA_GAIN).coerceAtMost(ManaManager.Config.MAX_MANA)
        ManaManager.showManaBar(player)

        Particle.DAMAGE_INDICATOR
            .builder()
            .location(player.location.add(0.0, 1.0, 0.0))
            .count(12)
            .offset(0.3, 0.4, 0.3)
            .spawn()
        Particle.CRIMSON_SPORE
            .builder()
            .location(player.location.add(0.0, 1.0, 0.0))
            .count(20)
            .offset(0.3, 0.5, 0.3)
            .spawn()

        player.playSound(Config.CAST_SOUND)
    }
}
