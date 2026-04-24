@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.EnderPearl
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.managers.XpManager
import org.xodium.vanillaplus.utils.ScheduleUtils
import org.xodium.vanillaplus.utils.Utils.displayName
import java.util.*

/** Represents an object handling voidpull enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object VoidpullEnchantment : EnchantmentInterface {
    private val PROJECTILE_KEY by lazy { NamespacedKey(instance, "voidpull_projectile") }
    private val trailTasks = mutableMapOf<UUID, BukkitTask>()

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
        val player = XpManager.consumeXp(event, get(), Config.XP_COST) ?: return
        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))
        val pearl = player.world.spawn(spawnLocation, EnderPearl::class.java)

        pearl.setGravity(false)
        pearl.velocity = direction.multiply(Config.VELOCITY)
        pearl.persistentDataContainer.set(PROJECTILE_KEY, PersistentDataType.STRING, player.uniqueId.toString())

        trailTasks[pearl.uniqueId] = spawnPearlTrail(pearl)
        player.playSound(Config.CAST_SOUND)
    }

    @EventHandler
    fun on(event: ProjectileHitEvent) {
        val projectile = event.entity
        val playerUuidStr =
            projectile.persistentDataContainer.get(PROJECTILE_KEY, PersistentDataType.STRING) ?: return

        trailTasks.remove(projectile.uniqueId)?.cancel()

        val target = event.hitEntity ?: return
        val player = instance.server.getPlayer(UUID.fromString(playerUuidStr)) ?: return
        val destination =
            player.location.clone().add(
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

    /**
     * Spawns a repeating PORTAL particle trail behind [pearl] every tick until the entity is no longer valid.
     * @param pearl The [EnderPearl] to trail.
     * @return The [BukkitTask] running the trail, so it can be cancelled early on hit.
     */
    private fun spawnPearlTrail(pearl: EnderPearl) =
        ScheduleUtils.spawnProjectileTrail(pearl) {
            Particle.PORTAL
                .builder()
                .location(it)
                .count(8)
                .offset(0.1, 0.1, 0.1)
                .spawn()
            Particle.REVERSE_PORTAL
                .builder()
                .location(it)
                .count(3)
                .offset(0.05, 0.05, 0.05)
                .spawn()
        }

    /** Configuration for the Voidpull enchantment. */
    object Config {
        /** The XP cost to cast Voidpull. */
        const val XP_COST = 3

        /** The velocity multiplier for the ender pearl projectile. */
        const val VELOCITY = 2.5

        /** The sound played when casting Voidpull. */
        val CAST_SOUND: Sound = Sound.sound(Key.key("entity.ender_pearl.throw"), Sound.Source.PLAYER, 1.0f, 1.0f)

        /** The sound played when pulling with Voidpull. */
        val PULL_SOUND: Sound = Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.HOSTILE, 1.0f, 0.8f)
    }
}
