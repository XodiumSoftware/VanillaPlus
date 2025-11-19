package org.xodium.vanillaplus.recipies

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.SmokingRecipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface

/** Handles the registration of wood to log conversion recipes. */
internal object RottenFleshRecipe : RecipeInterface {
    override fun getRecipes() =
        setOf(
            FurnaceRecipe(
                NamespacedKey(instance, "rotten_flesh_to_leather_furnace"),
                ItemStack.of(Material.LEATHER),
                Material.ROTTEN_FLESH,
                0.1f,
                200,
            ),
            SmokingRecipe(
                NamespacedKey(instance, "rotten_flesh_to_leather_smoking"),
                ItemStack.of(Material.LEATHER),
                Material.ROTTEN_FLESH,
                0.1f,
                100,
            ),
            CampfireRecipe(
                NamespacedKey(instance, "rotten_flesh_to_leather_campfire"),
                ItemStack.of(Material.LEATHER),
                Material.ROTTEN_FLESH,
                0.05f,
                600,
            ),
        )
}
