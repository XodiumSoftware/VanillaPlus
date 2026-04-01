package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.managers.ManaManager
import org.xodium.vanillaplus.utils.ScheduleUtils
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling frostbind enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object FrostbindEnchantment : EnchantmentInterface {
    object Config {
        const val MANA_COST = 15
        const val FREEZE_TICKS = 500
        val LAUNCH_SOUND: Sound = Sound.sound(Key.key("entity.snow_golem.shoot"), Sound.Source.NEUTRAL, 1.0f, 1.2f)
        val HIT_SOUND: Sound = Sound.sound(Key.key("block.powder_snow.place"), Sound.Source.BLOCK, 1.0f, 0.8f)
    }

    private val PROJECTILE_KEY by lazy { NamespacedKey(instance, "frostbind_projectile") }

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
                    TempestEnchantment.key,
                    VoidpullEnchantment.key,
                ),
            )

    /**
     * Handles a left-click interaction to launch a Frostbind snowball.
     * @param event The [PlayerInteractEvent] to handle.
     */
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = ManaManager.consumeMana(event, get(), Config.MANA_COST) ?: return
        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))
        val snowball = player.world.spawn(spawnLocation, Snowball::class.java)

        snowball.shooter = player
        snowball.setGravity(false)
        snowball.velocity = direction.multiply(2.0)
        snowball.persistentDataContainer.set(PROJECTILE_KEY, PersistentDataType.BOOLEAN, true)
        spawnSnowballTrail(snowball)
        player.playSound(Config.LAUNCH_SOUND)
    }

    /**
     * Spawns a repeating particle trail behind [snowball] every tick until the entity is no longer valid.
     * @param snowball The [Snowball] to trail.
     */
    private fun spawnSnowballTrail(snowball: Snowball) =
        ScheduleUtils.spawnProjectileTrail(snowball) {
            Particle.SNOWFLAKE
                .builder()
                .location(it)
                .count(5)
                .offset(0.05, 0.05, 0.05)
                .spawn()
            Particle.ITEM_SNOWBALL
                .builder()
                .location(it)
                .count(2)
                .offset(0.05, 0.05, 0.05)
                .spawn()
        }

    /**
     * Handles a projectile hit event, freezing the struck entity if the projectile is a Frostbind snowball.
     * Sets [Config.FREEZE_TICKS] on the hit entity, causing it to be fully frozen for several seconds.
     * @param event The [ProjectileHitEvent] to handle.
     */
    fun onProjectileHit(event: ProjectileHitEvent) {
        val projectile = event.entity

        if (!projectile.persistentDataContainer.has(PROJECTILE_KEY)) return

        val entity = event.hitEntity ?: return

        entity.freezeTicks = Config.FREEZE_TICKS

        Particle.SNOWFLAKE
            .builder()
            .location(entity.location.add(0.0, 1.0, 0.0))
            .count(20)
            .offset(0.3, 0.5, 0.3)
            .spawn()

        entity.world.players
            .filter { it.location.distance(entity.location) <= 16 }
            .forEach { it.playSound(Config.HIT_SOUND) }
    }
}
