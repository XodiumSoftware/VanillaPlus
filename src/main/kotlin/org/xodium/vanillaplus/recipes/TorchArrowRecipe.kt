package org.xodium.vanillaplus.recipes

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents an object handling torch arrow recipe implementation within the system. */
internal object TorchArrowRecipe : RecipeInterface {
    /** Namespaced key for identifying torch arrows. */
    val torchArrowKey = NamespacedKey(instance, "torch_arrow")

    /** Represents configuration for a specific torch arrow variant. */
    data class TorchArrowType(
        val id: String,
        val displayName: String,
        val torchMaterial: Material,
        val wallTorchMaterial: Material,
        val arrowColor: Color,
    )

    /** List of different torch arrow types with their properties. */
    private val torchTypes =
        listOf(
            TorchArrowType(
                "torch",
                "Torch Arrow",
                Material.TORCH,
                Material.WALL_TORCH,
                Color.YELLOW,
            ),
            TorchArrowType(
                "soul",
                "Soul Torch Arrow",
                Material.SOUL_TORCH,
                Material.SOUL_WALL_TORCH,
                Color.BLUE,
            ),
            TorchArrowType(
                "redstone",
                "Redstone Torch Arrow",
                Material.REDSTONE_TORCH,
                Material.REDSTONE_WALL_TORCH,
                Color.RED,
            ),
            TorchArrowType(
                "copper",
                "Copper Torch Arrow",
                Material.COPPER_TORCH,
                Material.COPPER_WALL_TORCH,
                Color.ORANGE,
            ),
        )

    /** Gets the torch arrow type configuration by ID. */
    fun getTorchArrowTypeById(id: String): TorchArrowType? = torchTypes.find { it.id == id }

    /**
     * Creates a torch arrow item stack with the specified type.
     * @param type The type of torch arrow to create.
     * @return A pair containing the namespaced key and the created item stack.
     */
    private fun createTorchArrow(type: TorchArrowType): ItemStack =
        ItemStack.of(Material.ARROW).apply {
            @Suppress("UnstableApiUsage")
            setData(DataComponentTypes.CUSTOM_NAME, type.displayName.mm())
            editPersistentDataContainer { it.set(torchArrowKey, PersistentDataType.STRING, type.id) }
        }

    override val recipes =
        torchTypes
            .map { type ->
                ShapelessRecipe(
                    NamespacedKey(instance, "${type.id}_torch_arrow"),
                    createTorchArrow(type),
                ).apply {
                    addIngredient(Material.ARROW)
                    addIngredient(type.torchMaterial)
                }
            }.toSet()
}
