package org.xodium.illyriaplus.interfaces

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.mechanics.entity.BossMechanic

/**
 * Represents a contract for custom bosses within the system.
 */
internal interface BossInterface {
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

    /** The equipment the boss spawns with (armor, weapons). */
    val equipment: Map<EquipmentSlot, ItemStack> get() = emptyMap()

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
            attributes.forEach { (attr, value) -> getAttribute(attr)?.baseValue = value }
            health = getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            this@BossInterface.equipment.forEach { (slot, item) ->
                when (slot) {
                    EquipmentSlot.HAND -> this@apply.equipment?.setItemInMainHand(item)
                    EquipmentSlot.OFF_HAND -> this@apply.equipment?.setItemInOffHand(item)
                    EquipmentSlot.HEAD -> this@apply.equipment?.setHelmet(item)
                    EquipmentSlot.CHEST -> this@apply.equipment?.setChestplate(item)
                    EquipmentSlot.LEGS -> this@apply.equipment?.setLeggings(item)
                    EquipmentSlot.FEET -> this@apply.equipment?.setBoots(item)
                    else -> return@apply
                }
            }
            BossMechanic.registerBoss(this@apply, this@BossInterface)
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
     * Triggers the boss's unique ability.
     *
     * @param entity The boss entity.
     */
    fun ability(entity: LivingEntity)
}
