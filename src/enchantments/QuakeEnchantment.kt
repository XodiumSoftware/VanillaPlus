package org.xodium.illyriaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.util.Vector
import org.xodium.illyriaplus.interfaces.EnchantmentInterface
import org.xodium.illyriaplus.managers.XpManager
import org.xodium.illyriaplus.utils.Utils
import org.xodium.illyriaplus.utils.Utils.displayName
import kotlin.math.cos
import kotlin.math.sin

/** Represents an object handling quake enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object QuakeEnchantment : EnchantmentInterface {
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

        val player = XpManager.consumeXp(event, get(), Config.XP_COST) ?: return
        val location = player.location
        val world = player.world

        for (ring in 1..Config.RING_COUNT) {
            val ringRadius = Config.RADIUS * ring / Config.RING_COUNT
            val particleCount = (ringRadius * 16).toInt()

            for (i in 0 until particleCount) {
                val angle = 2 * Math.PI * i / particleCount
                val x = location.x + ringRadius * cos(angle)
                val z = location.z + ringRadius * sin(angle)
                val y = location.y + 0.1
                val particleLoc = Location(world, x, y, z)

                world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0.05, 0.0, 0.05, 0.01)
                world.spawnParticle(Particle.CRIT, particleLoc.add(0.0, 0.2, 0.0), 1, 0.05, 0.05, 0.05, 0.0)
            }
        }

        world
            .getNearbyEntities(location, Config.RADIUS, Config.RADIUS / 2, Config.RADIUS)
            .filterIsInstance<LivingEntity>()
            .filter { it != player }
            .forEach {
                val direction =
                    it.location
                        .toVector()
                        .subtract(location.toVector())
                        .normalize()
                val upward = Vector(0.0, 0.4, 0.0)
                val knockback = direction.multiply(Config.KNOCKBACK_STRENGTH).add(upward)

                it.velocity = knockback
                it.damage(Config.DAMAGE, player)

                Particle.CRIT
                    .builder()
                    .location(it.location.add(0.0, 1.0, 0.0))
                    .count(8)
                    .offset(0.3, 0.5, 0.3)
                    .spawn()
            }

        world.spawnParticle(
            Particle.CAMPFIRE_COSY_SMOKE,
            location.clone().add(0.0, 0.1, 0.0),
            20,
            Config.RADIUS / 2,
            0.1,
            Config.RADIUS / 2,
            0.05,
        )

        world.players
            .filter { it.location.distance(location) <= 32 }
            .forEach {
                it.playSound(Config.CAST_SOUND)
                it.playSound(Config.HIT_SOUND)
            }
    }

    /** Configuration for the Quake enchantment. */
    object Config {
        /** The XP cost to cast Quake. */
        const val XP_COST = 3

        /** The radius in blocks for the shockwave effect. */
        const val RADIUS = 4.0

        /** The damage dealt to entities hit by the shockwave. */
        const val DAMAGE = 6.0

        /** The knockback strength applied to hit entities. */
        const val KNOCKBACK_STRENGTH = 1.2

        /** The number of particle rings to spawn. */
        const val RING_COUNT = 3

        /** The sound played when casting Quake. */
        val CAST_SOUND: Sound = Sound.sound(Key.key("entity.warden.attack_impact"), Sound.Source.BLOCK, 1.0f, 0.8f)

        /** The sound played when entities are hit by Quake. */
        val HIT_SOUND: Sound = Sound.sound(Key.key("block.anvil.land"), Sound.Source.BLOCK, 0.6f, 0.5f)
    }
}
