package org.xodium.vanillaplus.interfaces

import org.bukkit.inventory.Recipe
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Represents a contract for recipes within the system. */
internal interface RecipeInterface {
    /**
     * Retrieves the set of recipes to be registered.
     * @return A set of [Recipe] instances.
     */
    fun recipes(): Set<Recipe>

    /** Registers all recipes returned by [recipes] with the server. */
    fun register() = recipes().forEach { recipe -> instance.server.addRecipe(recipe) }
}
