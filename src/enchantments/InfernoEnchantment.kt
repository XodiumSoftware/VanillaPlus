package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.entity.SmallFireball
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ManaUtils
import org.xodium.vanillaplus.utils.Utils.displayName
import kotlin.uuid.ExperimentalUuidApi

/** Represents an object handling inferno enchantment implementation within the system. */
@OptIn(ExperimentalUuidApi::class)
@Suppress("UnstableApiUsage")
internal object InfernoEnchantment : EnchantmentInterface {
    object Config {
        const val MANA_COST = 10
        val LAUNCH_SOUND: Sound = Sound.sound(Key.key("entity.blaze.shoot"), Sound.Source.HOSTILE, 1.0f, 1.0f)
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
     * Handles a left-click interaction to launch an Inferno fireball.
     * @param event The [PlayerInteractEvent] to handle.
     */
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = ManaUtils.consumeMana(event, get(), Config.MANA_COST) ?: return
        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))
        val fireball = player.world.spawn(spawnLocation, SmallFireball::class.java)

        fireball.shooter = player
        fireball.direction = direction.clone().multiply(1.5)
        fireball.yield = 0.0f
        spawnFireballTrail(fireball)
        player.playSound(Config.LAUNCH_SOUND)
    }

    /**
     * Spawns a repeating particle trail behind [fireball] every tick until the entity is no longer valid.
     * Emits [Particle.FLAME] and [Particle.LAVA] at the fireball's current location.
     * @param fireball The [SmallFireball] to trail.
     */
    private fun spawnFireballTrail(fireball: SmallFireball) {
        var task: BukkitTask? = null
        task =
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    if (!fireball.isValid) {
                        task?.cancel()
                        return@Runnable
                    }
                    val loc = fireball.location

                    Particle.FLAME
                        .builder()
                        .location(loc)
                        .count(5)
                        .offset(0.05, 0.05, 0.05)
                        .spawn()
                    Particle.LAVA
                        .builder()
                        .location(loc)
                        .count(1)
                        .spawn()
                },
                1L,
                1L,
            )
    }
}
