package org.xodium.vanillaplus.interfaces

import org.bukkit.inventory.Recipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Represents a contract for recipes within the system. */
internal interface RecipeInterface {
    /**
     * Retrieves the set of recipes to be registered.
     * @return A set of [Recipe] instances.
     */
    fun getRecipes(): Set<Recipe>

    /** Registers all recipes returned by [getRecipes] with the server. */
    fun register() = getRecipes().forEach { recipe -> instance.server.addRecipe(recipe) }
}
