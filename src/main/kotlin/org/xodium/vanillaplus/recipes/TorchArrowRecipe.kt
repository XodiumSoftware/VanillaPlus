package org.xodium.vanillaplus.recipes

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface

/** Represents an object handling torch arrow recipe implementation within the system. */
internal object TorchArrowRecipe : RecipeInterface {
    override val recipes =
        setOf(
            ShapelessRecipe(
                NamespacedKey(instance, "torch_arrow"),
                ItemStack.of(Material.ARROW),
            ).apply {
                addIngredient(Material.ARROW)
                addIngredient(Material.TORCH)
            },
        )
}
