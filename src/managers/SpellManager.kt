package org.xodium.vanillaplus.managers

import org.bukkit.enchantments.Enchantment
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.enchantments.BloodpactEnchantment
import org.xodium.vanillaplus.enchantments.FrostbindEnchantment
import org.xodium.vanillaplus.enchantments.InfernoEnchantment
import org.xodium.vanillaplus.enchantments.QuakeEnchantment
import org.xodium.vanillaplus.enchantments.SkysunderEnchantment
import org.xodium.vanillaplus.enchantments.TempestEnchantment
import org.xodium.vanillaplus.enchantments.VoidpullEnchantment
import org.xodium.vanillaplus.enchantments.WitherbrandEnchantment

/** Manages spell execution and cycling for multi-spell wands. */
internal object SpellManager {
    private val SPELL_MAP: Map<Enchantment, (PlayerInteractEvent) -> Unit> by lazy {
        mapOf(
            InfernoEnchantment.get() to { InfernoEnchantment.onPlayerInteract(it) },
            FrostbindEnchantment.get() to { FrostbindEnchantment.onPlayerInteract(it) },
            WitherbrandEnchantment.get() to { WitherbrandEnchantment.onPlayerInteract(it) },
            SkysunderEnchantment.get() to { SkysunderEnchantment.onPlayerInteract(it) },
            TempestEnchantment.get() to { TempestEnchantment.onPlayerInteract(it) },
            VoidpullEnchantment.get() to { VoidpullEnchantment.onPlayerInteract(it) },
            QuakeEnchantment.get() to { QuakeEnchantment.onPlayerInteract(it) },
            BloodpactEnchantment.get() to { BloodpactEnchantment.onPlayerInteract(it) },
        )
    }

    /** Gets the list of spells on a wand item. */
    fun getSpellsOnWand(item: ItemStack): List<Enchantment> = item.enchantments.keys.filter { it in SPELL_MAP }

    /**
     * Gets the display name for a spell enchantment.
     * Derives the name from the enchantment key.
     */
    fun getSpellName(spell: Enchantment): String =
        spell.key
            .toString()
            .substringAfterLast(':')
            .removeSuffix("_enchantment")
            .replaceFirstChar { it.uppercase() }

    /** Cycles to the next spell and returns its name. */
    fun cycleSpell(item: ItemStack): String? = getSpellsOnWand(item).firstOrNull()?.let { getSpellName(it) }

    /** Executes a spell. */
    fun executeSpell(event: PlayerInteractEvent, spell: Enchantment) {
        SPELL_MAP[spell]?.invoke(event)
    }
}
