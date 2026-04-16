package org.xodium.vanillaplus.recipes

import io.papermc.paper.potion.PotionMix
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.RecipeChoice
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface
import org.xodium.vanillaplus.potions.ManaPotion
import kotlin.time.measureTime

/**
 * Represents the brewing recipe for the mana potion.
 * Brewed by combining an awkward potion with a blaze rod in a brewing stand.
 */
internal object ManaPotionRecipe : RecipeInterface {
    override val recipes = emptySet<org.bukkit.inventory.Recipe>()

    /**
     * Registers the mana potion brewing mix with the server's potion brewer.
     * This recipe allows players to brew mana potions by combining an awkward potion
     * with a blaze rod in a brewing stand.
     * @return The time taken to register the recipe in milliseconds.
     */
    override fun register(): Long =
        measureTime {
            val mix =
                PotionMix(
                    Config.RECIPE_KEY,
                    ManaPotion.createPotion(),
                    RecipeChoice.MaterialChoice(Material.POTION),
                    RecipeChoice.MaterialChoice(Material.BLAZE_ROD),
                )
            instance.server.potionBrewer.addPotionMix(mix)
        }.inWholeMilliseconds

    /** Configuration for the mana potion recipe. */
    private object Config {
        /** The namespaced key for this brewing recipe. */
        val RECIPE_KEY = NamespacedKey(instance, "mana_potion_mix")
    }
}
