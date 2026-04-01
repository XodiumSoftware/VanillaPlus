package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.entity.WitherSkull
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.managers.ManaManager
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling witherbrand enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object WitherbrandEnchantment : EnchantmentInterface {
    object Config {
        const val MANA_COST = 15
        val LAUNCH_SOUND: Sound = Sound.sound(Key.key("entity.wither.shoot"), Sound.Source.HOSTILE, 1.0f, 1.0f)
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
                    FrostbindEnchantment.key,
                    TempestEnchantment.key,
                    VoidpullEnchantment.key,
                    BloodpactEnchantment.key,
                ),
            )

    /**
     * Handles a left-click interaction to launch a Witherbrand wither skull.
     * @param event The [PlayerInteractEvent] to handle.
     */
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = ManaManager.consumeMana(event, get(), Config.MANA_COST) ?: return
        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))
        val skull = player.world.spawn(spawnLocation, WitherSkull::class.java)

        skull.shooter = player
        skull.direction = direction.clone().multiply(1.5)
        skull.isCharged = false
        spawnSkullTrail(skull)
        player.playSound(Config.LAUNCH_SOUND)
    }

    /**
     * Spawns a repeating particle trail behind [skull] every tick until the entity is no longer valid.
     * Emits [Particle.SOUL] and [Particle.ASH] at the skull's current location.
     * @param skull The [WitherSkull] to trail.
     */
    private fun spawnSkullTrail(skull: WitherSkull) {
        var task: BukkitTask? = null

        task =
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    if (!skull.isValid) {
                        task?.cancel()
                        return@Runnable
                    }
                    val loc = skull.location

                    Particle.SOUL
                        .builder()
                        .location(loc)
                        .count(3)
                        .offset(0.05, 0.05, 0.05)
                        .spawn()
                    Particle.ASH
                        .builder()
                        .location(loc)
                        .count(5)
                        .offset(0.1, 0.1, 0.1)
                        .spawn()
                },
                1L,
                1L,
            )
    }
}
