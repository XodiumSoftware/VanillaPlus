package org.xodium.vanillaplus.potions

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.PotionContents
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ItemInterface
import org.xodium.vanillaplus.pdcs.ItemPDC.isManaPotion
import org.xodium.vanillaplus.utils.Utils.MM

/**
 * Represents a mana item that provides mana restoration in various forms.
 * Includes drinkable potions, splash/lingering variants, and tipped arrows.
 * Mana items can be brewed or crafted and are essential for extended use of
 * spell enchantments on Blaze Rods.
 */
@Suppress("UnstableApiUsage")
internal object ManaItem : ItemInterface {
    /** The display name using the Spellbite gradient. */
    private val POTION_DISPLAY_NAME: Component =
        MM.deserialize(
            "Potion of <gradient:#832466:#BF4299:#832466>Arcane Restoration</gradient>",
        )

    /** The display name for splash variant using the Spellbite gradient. */
    private val SPLASH_DISPLAY_NAME: Component =
        MM.deserialize(
            "Splash Potion of <gradient:#832466:#BF4299:#832466>Arcane Restoration</gradient>",
        )

    /** The display name for lingering variant using the Spellbite gradient. */
    private val LINGERING_DISPLAY_NAME: Component =
        MM.deserialize(
            "Lingering Potion of <gradient:#832466:#BF4299:#832466>Arcane Restoration</gradient>",
        )

    /** The display name for tipped arrow variant using the Spellbite gradient. */
    private val TIPPED_ARROW_DISPLAY_NAME: Component =
        MM.deserialize(
            "Arrow of <gradient:#832466:#BF4299:#832466>Arcane Restoration</gradient>",
        )

    /** The potion color (Spellbite purple). */
    private val POTION_COLOR: Color = Color.fromRGB(0x832466)

    override fun potion(): ItemStack =
        super.potion().apply {
            setData(DataComponentTypes.CUSTOM_NAME, POTION_DISPLAY_NAME)
            setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().customColor(POTION_COLOR))
            isManaPotion = true
        }

    override fun splashPotion(): ItemStack =
        super.splashPotion().apply {
            setData(DataComponentTypes.CUSTOM_NAME, SPLASH_DISPLAY_NAME)
            setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().customColor(POTION_COLOR))
            isManaPotion = true
        }

    override fun lingeringPotion(): ItemStack =
        super.lingeringPotion().apply {
            setData(DataComponentTypes.CUSTOM_NAME, LINGERING_DISPLAY_NAME)
            setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().customColor(POTION_COLOR))
            isManaPotion = true
        }

    override fun tippedArrow(): ItemStack =
        super.tippedArrow().apply {
            setData(DataComponentTypes.CUSTOM_NAME, TIPPED_ARROW_DISPLAY_NAME)
            setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().customColor(POTION_COLOR))
            isManaPotion = true
        }
}
