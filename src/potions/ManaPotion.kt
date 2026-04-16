package org.xodium.vanillaplus.potions

import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.PotionInterface
import org.xodium.vanillaplus.pdcs.ItemPDC.isManaPotion
import org.xodium.vanillaplus.utils.Utils.MM

/**
 * Represents a mana potion that instantly refills the player's mana pool when consumed.
 * Mana potions can be brewed or obtained through other means and are essential for
 * extended use of spell enchantments on Blaze Rods.
 */
@Suppress("UnstableApiUsage")
internal object ManaPotion : PotionInterface {
    /** The namespaced key for this potion type. */
    const val KEY = "vanillaplus:mana_potion"

    /** The display name using the Spellbite gradient. */
    private const val DISPLAY_NAME = "<gradient:#832466:#BF4299:#832466>Potion of Arcane Restoration</gradient>"

    /** The potion color (Spellbite purple). */
    private val POTION_COLOR = Color.fromRGB(0x832466)

    override val key: String = KEY

    /**
     * Creates a new mana potion [ItemStack].
     * The potion has a custom purple color from the Spellbite gradient,
     * a custom display name, and the [isManaPotion] PDC marker.
     * @return A configured mana potion item.
     */
    override fun createPotion(): ItemStack {
        val potion = ItemStack(Material.POTION)
        potion.editMeta(org.bukkit.inventory.meta.PotionMeta::class.java) { meta ->
            meta.setColor(POTION_COLOR)
            meta.displayName(MM.deserialize(DISPLAY_NAME))
        }
        potion.isManaPotion = true
        return potion
    }

    /**
     * Creates a splash mana potion [ItemStack] for ranged application.
     * @return A configured splash mana potion item.
     */
    fun createSplashPotion(): ItemStack {
        val potion = ItemStack(Material.SPLASH_POTION)
        potion.editMeta(org.bukkit.inventory.meta.PotionMeta::class.java) { meta ->
            meta.setColor(POTION_COLOR)
            meta.displayName(MM.deserialize("<gradient:#832466:#BF4299:#832466>Splash Potion of Arcane Restoration</gradient>"))
        }
        potion.isManaPotion = true
        return potion
    }
}