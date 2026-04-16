package org.xodium.vanillaplus.potions

import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.xodium.vanillaplus.interfaces.PotionInterface
import org.xodium.vanillaplus.pdcs.ItemPDC.isManaPotion
import org.xodium.vanillaplus.utils.Utils.MM

/**
 * Represents a mana potion that instantly refills the player's mana pool when consumed.
 * Mana potions can be brewed or obtained through other means and are essential for
 * extended use of spell enchantments on Blaze Rods.
 */
internal object ManaPotion : PotionInterface {
    override val key: String = "vanillaplus:mana_potion"

    /** The display name using the Spellbite gradient. */
    private const val DISPLAY_NAME = "<gradient:#832466:#BF4299:#832466>Potion of Arcane Restoration</gradient>"

    /** The potion color (Spellbite purple). */
    private val POTION_COLOR = Color.fromRGB(0x832466)

    override fun createPotion(): ItemStack {
        val potion = ItemStack.of(Material.POTION)
        potion.editMeta(PotionMeta::class.java) {
            it.color = POTION_COLOR
            it.displayName(MM.deserialize(DISPLAY_NAME))
        }
        potion.isManaPotion = true
        return potion
    }

    override fun createSplashPotion(): ItemStack {
        val potion = ItemStack.of(Material.SPLASH_POTION)
        potion.editMeta(PotionMeta::class.java) {
            it.color = POTION_COLOR
            it.displayName(
                MM.deserialize("<gradient:#832466:#BF4299:#832466>Splash Potion of Arcane Restoration</gradient>"),
            )
        }
        potion.isManaPotion = true
        return potion
    }
}
