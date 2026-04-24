package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.entity.WitherSkull
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.GameMode
import org.bukkit.event.block.Action
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ScheduleUtils
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling witherbrand enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object WitherbrandEnchantment : EnchantmentInterface {
    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(4)
            .maxLevel(1)
            .weight(1)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(20, 5))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 5))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        // TODO: Implement XP cost check instead of mana
        // if (!hasEnoughXp(event.player, Config.XP_COST)) return
        if (event.action != Action.LEFT_CLICK_AIR && event.action != Action.LEFT_CLICK_BLOCK) return
        val item = event.item ?: return
        if (item.type != org.bukkit.Material.BLAZE_ROD) return
        if (!item.containsEnchantment(get())) return
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE) {
            event.isCancelled = true
        } else if (player.gameMode != GameMode.SURVIVAL && player.gameMode != GameMode.ADVENTURE) {
            return
        } else {
            event.isCancelled = true
            // TODO: Deduct XP cost
            // player.giveExp(-Config.XP_COST)
        }
        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))
        val skull = player.world.spawn(spawnLocation, WitherSkull::class.java)

        skull.shooter = player
        skull.direction = direction.clone().multiply(1.5)
        skull.isCharged = false
        spawnSkullTrail(skull)
        player.playSound(Config.CAST_SOUND)
    }

    /**
     * Spawns a repeating particle trail behind [skull] every tick until the entity is no longer valid.
     * Emits [Particle.SOUL] and [Particle.ASH] at the skull's current location.
     * @param skull The [WitherSkull] to trail.
     */
    private fun spawnSkullTrail(skull: WitherSkull) {
        ScheduleUtils.spawnProjectileTrail(skull) {
            Particle.SOUL
                .builder()
                .location(it)
                .count(3)
                .offset(0.05, 0.05, 0.05)
                .spawn()
            Particle.ASH
                .builder()
                .location(it)
                .count(5)
                .offset(0.1, 0.1, 0.1)
                .spawn()
        }
    }

    /** Configuration for the Witherbrand enchantment. */
    object Config {
        /** The XP cost to cast Witherbrand. */
        const val XP_COST = 15

        /** The sound played when casting Witherbrand. */
        val CAST_SOUND: Sound = Sound.sound(Key.key("entity.wither.shoot"), Sound.Source.HOSTILE, 1.0f, 1.0f)
    }
}
