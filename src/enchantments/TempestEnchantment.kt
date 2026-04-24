package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.entity.WindCharge
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.util.Vector
import org.bukkit.GameMode
import org.bukkit.event.block.Action
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ScheduleUtils
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling tempest enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object TempestEnchantment : EnchantmentInterface {
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
        val baseDir = player.location.direction.normalize()
        val right = baseDir.clone().crossProduct(Vector(0, 1, 0)).normalize()
        val spawnBase = player.eyeLocation.add(baseDir.clone().multiply(1.5))
        val offsets = listOf(-Config.SPREAD_AMOUNT, 0.0, Config.SPREAD_AMOUNT)

        offsets.forEach {
            val dir = baseDir.clone().add(right.clone().multiply(it)).normalize()
            val charge =
                player.world.spawn(spawnBase.clone().add(right.clone().multiply(it * 0.5)), WindCharge::class.java)

            charge.shooter = player
            charge.velocity = dir.multiply(1.5)
            spawnWindChargeTrail(charge)
        }

        player.playSound(Config.CAST_SOUND)
    }

    /**
     * Spawns a repeating particle trail behind [charge] every tick until the entity is no longer valid.
     * @param charge The [WindCharge] to trail.
     */
    private fun spawnWindChargeTrail(charge: WindCharge) =
        ScheduleUtils.spawnProjectileTrail(charge) {
            Particle.GUST
                .builder()
                .location(it)
                .count(1)
                .spawn()
            Particle.CLOUD
                .builder()
                .location(it)
                .count(3)
                .offset(0.05, 0.05, 0.05)
                .extra(0.02)
                .spawn()
        }

    /** Configuration for the Tempest enchantment. */
    object Config {
        /** The XP cost to cast Tempest. */
        const val XP_COST = 25

        /** The horizontal spread amount between wind charges. */
        const val SPREAD_AMOUNT = 0.2

        /** The sound played when casting Tempest. */
        val CAST_SOUND: Sound = Sound.sound(Key.key("entity.breeze.shoot"), Sound.Source.HOSTILE, 1.0f, 1.0f)
    }
}
