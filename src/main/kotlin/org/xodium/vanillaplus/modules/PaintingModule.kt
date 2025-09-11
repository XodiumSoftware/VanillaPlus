package org.xodium.vanillaplus.modules

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.StonecuttingRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling painting mechanics within the system. */
internal class PaintingModule : ModuleInterface<PaintingModule.Config> {
    override val config: Config = Config()

    private val key = NamespacedKey(instance, "painting_variant")


    init {
        if (enabled()) {
            val paintingRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT)
            for (art in paintingRegistry) {
                val itemStack = ItemStack.of(Material.PAINTING)
                val itemMeta = itemStack.itemMeta
                val variantKey = paintingRegistry.getKeyOrThrow(art)
                if (itemMeta != null) {
                    itemMeta.persistentDataContainer.set(key, PersistentDataType.STRING, variantKey.asString())
                    itemMeta.displayName("Painting: ${variantKey.key}".mm())
                    itemStack.itemMeta = itemMeta
                }
                val recipe = StonecuttingRecipe(
                    NamespacedKey(instance, "painting_${variantKey.key}"),
                    itemStack,
                    Material.PAINTING
                )
                instance.server.addRecipe(recipe)
            }
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
