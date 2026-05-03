package org.xodium.illyriaplus.recipes

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.interfaces.RecipeInterface
import org.xodium.illyriaplus.items.TeleportScrollItem

/** Recipe for crafting the Teleport Scroll. */
internal object TeleportScrollRecipe : RecipeInterface {
    override val recipes =
        setOf(
            ShapedRecipe(
                NamespacedKey(instance, "teleport_scroll_shaped_recipe"),
                TeleportScrollItem.item,
            ).apply {
                shape(" E ", "GPG", " R ")
                setIngredient('E', Material.ENDER_PEARL)
                setIngredient('G', Material.GLOWSTONE_DUST)
                setIngredient('P', Material.PAPER)
                setIngredient('R', Material.REDSTONE)
            },
        )
}
