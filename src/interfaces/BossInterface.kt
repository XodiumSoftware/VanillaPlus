package org.xodium.illyriaplus.interfaces

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

/**
 * Represents a contract for custom bosses within the system.
 * Extends MechanicInterface for automatic event registration.
 */
internal interface BossInterface {
    /** The display name of the boss (shown in boss bar and above entity). */
    val bossName: Component

    /** The base entity type for this boss. */
    val entityType: EntityType

    /** The maximum health pool for this boss. */
    val bossMaxHealth: Double

    /** The boss bar displayed for this boss. */
    val bossBar: BossBar

    /** The items dropped when the boss dies. */
    val drops: List<ItemStack> get() = emptyList()

    /**
     * Spawns the boss at the specified location.
     *
     * @param location Where to spawn the boss.
     * @return The spawned LivingEntity instance.
     */
    fun spawn(location: Location): LivingEntity

    /**
     * Despawns the boss and cleans up associated resources.
     *
     * @param entity The boss entity to despawn.
     */
    fun despawn(entity: LivingEntity)

    /**
     * Called when the boss dies.
     *
     * @param entity The dying boss entity.
     */
    fun onDeath(entity: LivingEntity)

    /**
     * Called when the boss takes damage.
     *
     * @param entity The boss entity.
     * @param damage The amount of damage taken.
     */
    fun onDamage(
        entity: LivingEntity,
        damage: Double,
    )

    /**
     * Called each tick to update boss behavior/abilities.
     *
     * @param entity The boss entity.
     */
    fun onTick(entity: LivingEntity)
}
