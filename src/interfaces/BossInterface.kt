package org.xodium.illyriaplus.interfaces

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import kotlin.time.measureTime

/**
 * Represents a contract for custom bosses within the system.
 * Extends MechanicInterface for automatic event registration.
 */
internal interface BossInterface : Listener {
    /** The display name of the boss (shown in boss bar and above entity). */
    val bossName: Component

    /** The base entity type for this boss. */
    val bossType: EntityType

    /** The boss bar displayed for this boss. */
    val bossBar: BossBar

    /** The items dropped when the boss dies. */
    val drops: List<ItemStack>

    /**
     * Custom attributes to apply to the boss (e.g., MAX_HEALTH, ATTACK_DAMAGE, MOVEMENT_SPEED).
     * Must include MAX_HEALTH - it defines the boss's max health.
     */
    val attributes: Map<Attribute, Double>

    /**
     * Spawns the boss at the specified location.
     *
     * @param location Where to spawn the boss.
     * @return The spawned LivingEntity instance.
     */
    fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, bossType) as LivingEntity).apply {
            customName(bossName)
            isCustomNameVisible = true
            showBossBar(bossBar)
            attributes.forEach { (attr, value) -> getAttribute(attr)?.baseValue = value }
            health = getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        }

    /**
     * Updates the boss bar progress based on entity health.
     *
     * @param entity The boss entity.
     */
    fun updateBossBar(entity: LivingEntity) {
        val maxHealth = entity.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        bossBar.progress((entity.health / maxHealth).toFloat())
    }

    /**
     * Called when the boss dies.
     *
     * @param entity The dying boss entity.
     */
    fun onDeath(entity: LivingEntity) {
        drops.forEach { entity.world.dropItemNaturally(entity.location, it) }
    }

    /**
     * Called when the boss takes damage.
     *
     * @param entity The boss entity.
     */
    fun onDamage(entity: LivingEntity) {
        updateBossBar(entity)
    }

    /**
     * Called each tick to update boss behavior/abilities.
     *
     * @param entity The boss entity.
     */
    fun onTick(entity: LivingEntity)

    /**
     * Registers the events with the server.
     *
     * @return The time taken to register the feature in milliseconds.
     */
    fun register(): Long =
        measureTime { instance.server.pluginManager.registerEvents(this, instance) }.inWholeMilliseconds

    @EventHandler
    fun on(event: EntityDeathEvent) {
        onDeath(event.entity)
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        onDamage(event.entity as? LivingEntity ?: return)
    }
}
