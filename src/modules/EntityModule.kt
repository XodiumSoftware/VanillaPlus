@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.destroystokyo.paper.MaterialTags
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Blaze
import org.bukkit.entity.Creeper
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Enderman
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.Wither
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.xodium.vanillaplus.enchantments.NimbusEnchantment
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.random.Random

/** Represents a module handling entity mechanics within the system. */
internal object EntityModule : ModuleInterface {
    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (shouldCancelGrief(event.entity)) event.isCancelled = true
    }

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (shouldCancelGrief(event.entity)) event.blockList().clear()
    }

    @EventHandler
    fun on(event: EntityDeathEvent) {
        if (Random.nextDouble() <= Config.entityEggDropChance) {
            Material.matchMaterial("${event.entity.type.name}_SPAWN_EGG")?.let { event.drops.add(ItemStack.of(it)) }
        }
    }

    @EventHandler
    fun on(event: EntityEquipmentChangedEvent) = NimbusEnchantment.onEntityEquipmentChanged(event)

    @EventHandler
    fun on(event: PrepareAnvilEvent) = horseArmor(event)

    @Suppress("UnstableApiUsage")
    fun horseArmor(event: PrepareAnvilEvent) {
        val armor = event.inventory.getItem(0) ?: return
        val book = event.inventory.getItem(1) ?: return

        if (!MaterialTags.HORSE_ARMORS.isTagged(armor.type)) return
        if (book.type != Material.ENCHANTED_BOOK) return

        val bookMeta = book.itemMeta as? EnchantmentStorageMeta ?: return
        val applicable =
            bookMeta.storedEnchants.filter { (enchant, _) -> enchant in Config.horseArmorEnchants }

        if (applicable.isEmpty()) return

        val result = armor.clone()
        val resultMeta = result.itemMeta ?: return

        var cost = 0

        for ((enchant, bookLevel) in applicable) {
            if (resultMeta.enchants.keys.any { it.conflictsWith(enchant) }) continue

            val existingLevel = resultMeta.getEnchantLevel(enchant)
            val newLevel =
                when {
                    existingLevel == 0 -> bookLevel
                    existingLevel == bookLevel -> (bookLevel + 1).coerceAtMost(enchant.maxLevel)
                    bookLevel > existingLevel -> bookLevel
                    else -> continue
                }

            resultMeta.addEnchant(enchant, newLevel, true)
            cost += newLevel
        }

        if (cost == 0) return

        result.itemMeta = resultMeta
        event.result = result
        event.view.repairCost = cost
    }

    /**
     * Determines whether an entity's griefing behaviour should be cancelled.
     * @param entity The entity whose griefing behaviour is being evaluated.
     * @return `true` if the entity's type is in [Config.griefCancelTypes]; `false` otherwise.
     */
    private fun shouldCancelGrief(entity: Entity): Boolean = Config.griefCancelTypes.any { it.isInstance(entity) }

    /** Represents the config of the module. */
    object Config {
        val griefCancelTypes =
            setOf(Blaze::class, Creeper::class, EnderDragon::class, Enderman::class, Fireball::class, Wither::class)
        var entityEggDropChance: Double = 0.001
        val horseArmorEnchants =
            setOf(
                Enchantment.PROTECTION,
                Enchantment.FIRE_PROTECTION,
                Enchantment.BLAST_PROTECTION,
                Enchantment.PROJECTILE_PROTECTION,
                Enchantment.THORNS,
                Enchantment.MENDING,
                Enchantment.UNBREAKING,
                Enchantment.BINDING_CURSE,
                Enchantment.VANISHING_CURSE,
            )
    }
}
