package org.xodium.vanillaplus.mechanics.entity

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Camel
import org.bukkit.entity.Husk
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.MechanicInterface
import kotlin.random.Random

/** Handles sand drops when husks die, with bonus for Camel-riding husks. */
internal object HuskMechanic : MechanicInterface {
    private const val HUSK_SAND_DROP_CHANCE: Double = 1.0
    private const val HUSK_SAND_BASE_MIN: Int = 0
    private const val HUSK_SAND_BASE_MAX: Int = 2
    private const val HUSK_SAND_LOOTING_BONUS: Int = 1
    private const val CAMEL_HUSK_SAND_BASE_MAX: Int = 3
    private const val CAMEL_HUSK_SAND_LOOTING_BONUS: Int = 2

    @EventHandler
    fun on(event: EntityDeathEvent) {
        if (event.entity !is Husk) return
        if (Random.nextDouble() > HUSK_SAND_DROP_CHANCE) return

        val isCamelHusk = event.entity.vehicle is Camel
        val lootingLevel =
            event.entity.killer
                ?.inventory
                ?.itemInMainHand
                ?.getEnchantmentLevel(Enchantment.LOOTING) ?: 0
        val minAmount = HUSK_SAND_BASE_MIN
        val maxAmount =
            if (isCamelHusk) {
                CAMEL_HUSK_SAND_BASE_MAX + (lootingLevel * CAMEL_HUSK_SAND_LOOTING_BONUS)
            } else {
                HUSK_SAND_BASE_MAX + (lootingLevel * HUSK_SAND_LOOTING_BONUS)
            }
        val amount = Random.nextInt(minAmount, maxAmount + 1)

        if (amount > 0) event.drops.add(ItemStack.of(Material.SAND, amount))
    }
}
