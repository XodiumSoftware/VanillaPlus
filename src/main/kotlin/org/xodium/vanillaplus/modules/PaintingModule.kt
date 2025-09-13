package org.xodium.vanillaplus.modules

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Painting
import org.bukkit.event.EventHandler
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.StonecuttingRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling painting mechanics within the system. */
internal class PaintingModule : ModuleInterface<PaintingModule.Config> {
    override val config: Config = Config()

    private val paintingKey = NamespacedKey(instance, "painting_variant")

    init {
        if (enabled()) {
            val paintingRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT)
            for (art in paintingRegistry) {
                val itemStack = ItemStack.of(Material.PAINTING)
                val itemMeta = itemStack.itemMeta
                val variantKey = paintingRegistry.getKeyOrThrow(art)
                if (itemMeta != null) {
                    itemMeta.persistentDataContainer.set(paintingKey, PersistentDataType.STRING, variantKey.asString())
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

    @EventHandler
    fun on(event: HangingPlaceEvent) {
        if (!enabled()) return
        if (event.entity !is Painting) return

        val painting = event.entity as Painting
        val item = event.itemStack ?: return

        val typeName = item.itemMeta?.persistentDataContainer?.get(paintingKey, PersistentDataType.STRING) ?: return
        val key = NamespacedKey.fromString(typeName) ?: return

        val paintingRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT)
        val variant = paintingRegistry.get(key) ?: return

        painting.variant = variant
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
