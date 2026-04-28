package org.xodium.illyriaplus.managers

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.enchantments.FrostbindEnchantment
import org.xodium.illyriaplus.enchantments.InfernoEnchantment
import org.xodium.illyriaplus.enchantments.QuakeEnchantment
import org.xodium.illyriaplus.enchantments.SkysunderEnchantment
import org.xodium.illyriaplus.enchantments.TempestEnchantment
import org.xodium.illyriaplus.enchantments.VoidpullEnchantment
import org.xodium.illyriaplus.enchantments.WitherbrandEnchantment
import org.xodium.illyriaplus.pdcs.ItemPDC.selectedSpell
import org.xodium.illyriaplus.utils.Utils.MM

/** Manages spell execution and cycling for multi-spell wands. */
internal object SpellManager {
    private val SPELL_MAP: Map<Enchantment, (PlayerInteractEvent) -> Unit> by lazy {
        mapOf(
            FrostbindEnchantment.get() to { FrostbindEnchantment.on(it) },
            InfernoEnchantment.get() to { InfernoEnchantment.on(it) },
            QuakeEnchantment.get() to { QuakeEnchantment.on(it) },
            SkysunderEnchantment.get() to { SkysunderEnchantment.on(it) },
            TempestEnchantment.get() to { TempestEnchantment.on(it) },
            VoidpullEnchantment.get() to { VoidpullEnchantment.on(it) },
            WitherbrandEnchantment.get() to { WitherbrandEnchantment.on(it) },
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

    /** Gets the spell key string for storage. */
    private fun getSpellKey(spell: Enchantment): String = spell.key.toString()

    /** Shows the selected spell name in the player's action bar. */
    private fun showSelectedSpell(
        player: Player,
        spellName: String,
    ) {
        player.sendActionBar(
            MM.deserialize(
                "<gradient:#832466:#BF4299>Current Spell > <white><spell></white></gradient>",
                Placeholder.unparsed("spell", spellName),
            ),
        )
    }

    /** Cycles to the next spell, updates the item's selected spell, and returns its name. */
    fun cycleSpell(item: ItemStack): String? =
        getSpellsOnWand(item).takeIf { it.isNotEmpty() }?.let { spells ->
            val nextSpell = spells[(spells.indexOfFirst { getSpellKey(it) == item.selectedSpell } + 1) % spells.size]

            item.selectedSpell = getSpellKey(nextSpell)
            getSpellName(nextSpell)
        }

    /** Gets the currently selected spell for the item. */
    fun getSelectedSpell(item: ItemStack): Enchantment? =
        getSpellsOnWand(item).takeIf { it.isNotEmpty() }?.let { spells ->
            spells.find { getSpellKey(it) == item.selectedSpell } ?: spells.first()
        }

    /**
     * Handles wand interactions for multi-spell casting.
     * Right-click cycles through spells, left-click casts the selected spell.
     * @param event The PlayerInteractEvent to handle.
     */
    fun handleWandInteraction(event: PlayerInteractEvent) {
        val item = event.item ?: return

        if (item.type != Material.BLAZE_ROD) return
        if (getSpellsOnWand(item).isEmpty()) return

        when (event.action) {
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
                event.isCancelled = true
                cycleSpell(item)?.let { showSelectedSpell(event.player, it) }
            }

            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
                SPELL_MAP[getSelectedSpell(item) ?: return]?.invoke(event)
            }

            else -> {}
        }
    }

    /**
     * Handles wand selection when player scrolls to it in hotbar.
     * Shows the currently selected spell in the action bar.
     * @param event The PlayerItemHeldEvent to handle.
     */
    fun handleWandSelection(event: PlayerItemHeldEvent) {
        val item = event.player.inventory.getItem(event.newSlot) ?: return

        if (item.type != Material.BLAZE_ROD) return
        if (getSpellsOnWand(item).isEmpty()) return

        getSelectedSpell(item)?.let { showSelectedSpell(event.player, getSpellName(it)) }
    }
}
