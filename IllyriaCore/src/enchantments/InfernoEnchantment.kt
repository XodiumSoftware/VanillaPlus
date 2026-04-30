package org.xodium.illyriaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.entity.SmallFireball
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.illyriaplus.interfaces.EnchantmentInterface
import org.xodium.illyriaplus.managers.XpManager
import org.xodium.illyriaplus.utils.ScheduleUtils
import org.xodium.illyriaplus.utils.Utils
import org.xodium.illyriaplus.utils.Utils.displayName
import kotlin.uuid.ExperimentalUuidApi

/** Represents an object handling inferno enchantment implementation within the system. */
@OptIn(ExperimentalUuidApi::class)
@Suppress("UnstableApiUsage")
internal object InfernoEnchantment : EnchantmentInterface {
    private const val XP_COST = 1

    private val CAST_SOUND: Sound = Sound.sound(Key.key("entity.blaze.shoot"), Sound.Source.HOSTILE, 1.0f, 1.0f)

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
        if (!Utils.isSelectedSpell(event.item, get())) return

        val player = XpManager.consumeXp(event, get(), XP_COST) ?: return
        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))
        val fireball = player.world.spawn(spawnLocation, SmallFireball::class.java)

        fireball.shooter = player
        fireball.direction = direction.clone().multiply(1.5)
        fireball.yield = 0.0f
        spawnFireballTrail(fireball)
        player.playSound(CAST_SOUND)
    }

    /**
     * Spawns a repeating particle trail behind [fireball] every tick until the entity is no longer valid.
     * Emits [Particle.FLAME] and [Particle.LAVA] at the fireball's current location.
     *
     * @param fireball The [SmallFireball] to trail.
     */
    private fun spawnFireballTrail(fireball: SmallFireball) =
        ScheduleUtils.spawnProjectileTrail(fireball) {
            Particle.FLAME
                .builder()
                .location(it)
                .count(5)
                .offset(0.05, 0.05, 0.05)
                .spawn()
            Particle.LAVA
                .builder()
                .location(it)
                .count(1)
                .spawn()
        }
}
