package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.managers.ManaManager
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling frostbind enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object FrostbindEnchantment : EnchantmentInterface {
    object Config {
        const val MANA_COST = 10
        const val FREEZE_RADIUS = 3.0
        val LAUNCH_SOUND: Sound = Sound.sound(Key.key("entity.snowball.throw"), Sound.Source.PLAYER, 1.0f, 0.6f)
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
     * Handles a left-click interaction to fire a Frostbind snowball.
     * @param event The [PlayerInteractEvent] to handle.
     */
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = ManaManager.consumeMana(event, get(), Config.MANA_COST) ?: return
        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))
        val snowball = player.world.spawn(spawnLocation, Snowball::class.java)

        snowball.shooter = player
        snowball.velocity = direction.multiply(2.0)
        spawnSnowballTrail(snowball)
        player.playSound(Config.LAUNCH_SOUND)
    }

    /**
     * Freezes the living entity directly struck by the Frostbind snowball.
     * Only triggers when the shooter is a [Player] holding the enchanted item.
     * @param event The [ProjectileHitEvent] fired on impact.
     */
    fun onProjectileHit(event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return
        val shooter = snowball.shooter as? Player ?: return

        if (!shooter.inventory.itemInMainHand.containsEnchantment(get())) return

        val target = event.hitEntity as? LivingEntity ?: return

        if (target == shooter) return

        // TODO: change to something high for more damage.
        target.freezeTicks = target.maxFreezeTicks
    }

    /**
     * Spawns a repeating particle trail behind [snowball] every tick until the entity is no longer valid.
     * Emits [Particle.SNOWFLAKE] along the flight path. On impact, freezes all nearby living entities
     * within [Config.FREEZE_RADIUS] blocks and bursts [Particle.SNOWFLAKE] at the hit location.
     * @param snowball The [Snowball] to trail.
     */
    private fun spawnSnowballTrail(snowball: Snowball) {
        var lastLoc = snowball.location
        var task: BukkitTask? = null

        task =
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    if (!snowball.isValid) {
                        task?.cancel()
                        lastLoc.world
                            ?.getNearbyLivingEntities(lastLoc, Config.FREEZE_RADIUS)
                            ?.filter { it != snowball.shooter }
                            ?.forEach { it.freezeTicks = it.maxFreezeTicks }
                        Particle.SNOWFLAKE
                            .builder()
                            .location(lastLoc)
                            .count(40)
                            .offset(0.5, 0.5, 0.5)
                            .spawn()
                        return@Runnable
                    }

                    lastLoc = snowball.location
                    Particle.SNOWFLAKE
                        .builder()
                        .location(lastLoc)
                        .count(3)
                        .offset(0.05, 0.05, 0.05)
                        .spawn()
                },
                1L,
                1L,
            )
    }
}
