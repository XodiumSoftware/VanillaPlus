package org.xodium.vanillaplus.recipes

import io.papermc.paper.potion.PotionMix
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface
import org.xodium.vanillaplus.potions.ManaItem

/**
 * Represents the recipes for mana items.
 * Includes brewing recipes for potion variants and a crafting recipe for tipped arrows.
 */
internal object ManaItemRecipe : RecipeInterface {
    override val recipes =
        setOf(
            ShapedRecipe(
                NamespacedKey(instance, "mana_tipped_arrow_shaped_recipe"),
                ManaItem.tippedArrow(),
            ).apply {
                shape("AAA", "ABA", "AAA")
                setIngredient('A', Material.ARROW)
                setIngredient('B', ManaItem.lingeringPotion())
            },
        )

    override val potions: Collection<PotionMix> =
        setOf(
            PotionMix(
                NamespacedKey(instance, "mana_potion_mix"),
                ManaItem.potion(),
                RecipeChoice.ExactChoice(ItemStack.of(Material.POTION)),
                RecipeChoice.ExactChoice(ItemStack.of(Material.BLAZE_ROD)),
            ),
            PotionMix(
                NamespacedKey(instance, "mana_splash_potion_mix"),
                ManaItem.splashPotion(),
                RecipeChoice.ExactChoice(ItemStack.of(Material.SPLASH_POTION)),
                RecipeChoice.ExactChoice(ItemStack.of(Material.BLAZE_ROD)),
            ),
            PotionMix(
                NamespacedKey(instance, "mana_lingering_potion_mix"),
                ManaItem.lingeringPotion(),
                RecipeChoice.ExactChoice(ItemStack.of(Material.LINGERING_POTION)),
                RecipeChoice.ExactChoice(ItemStack.of(Material.BLAZE_ROD)),
            ),
        )
}
