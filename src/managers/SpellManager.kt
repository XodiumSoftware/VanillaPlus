package org.xodium.vanillaplus.managers

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
    private val SPELL_MAP: Map<String, Pair<String, (PlayerInteractEvent) -> Unit>> by lazy {
        mapOf(
            InfernoEnchantment.key.key().toString() to Pair("Inferno") { InfernoEnchantment.onPlayerInteract(it) },
            FrostbindEnchantment.key
                .key()
                .toString() to Pair("Frostbind") { FrostbindEnchantment.onPlayerInteract(it) },
            WitherbrandEnchantment.key
                .key()
                .toString() to Pair("Witherbrand") { WitherbrandEnchantment.onPlayerInteract(it) },
            SkysunderEnchantment.key
                .key()
                .toString() to Pair("Skysunder") { SkysunderEnchantment.onPlayerInteract(it) },
            TempestEnchantment.key.key().toString() to Pair("Tempest") { TempestEnchantment.onPlayerInteract(it) },
            VoidpullEnchantment.key.key().toString() to Pair("Voidpull") { VoidpullEnchantment.onPlayerInteract(it) },
            QuakeEnchantment.key.key().toString() to Pair("Quake") { QuakeEnchantment.onPlayerInteract(it) },
            BloodpactEnchantment.key
                .key()
                .toString() to Pair("Bloodpact") { BloodpactEnchantment.onPlayerInteract(it) },
        )
    }

    /** Gets the list of spells on a wand item. */
    fun getSpellsOnWand(item: ItemStack): List<String> {
        if (!item.hasItemMeta()) return emptyList()

        return item.enchantments.keys
            .map { it.key.key().toString() }
            .filter { it in SPELL_MAP }
    }

    /** Gets the first available spell on the wand. */
    fun getFirstSpell(item: ItemStack): String? = getSpellsOnWand(item).firstOrNull()

    /** Gets the display name for a spell key. */
    fun getSpellName(spellKey: String): String = SPELL_MAP[spellKey]?.first ?: spellKey

    /** Cycles to the next spell and returns its name. */
    fun cycleSpell(item: ItemStack): String? {
        val spells = getSpellsOnWand(item)

        if (spells.isEmpty()) return null

        return getSpellName(spells.first())
    }

    /** Executes a spell by its key. */
    fun executeSpell(
        event: PlayerInteractEvent,
        spellKey: String,
    ) {
        SPELL_MAP[spellKey]?.second?.invoke(event)
    }
}
