package org.xodium.illyriaplus.items

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.ItemInterface

/** The Teleport Scroll item. */
@Suppress("UnstableApiUsage")
internal object TeleportScrollItem : ItemInterface {
    private val TELEPORT_SCROLL_KEY = NamespacedKey(instance, "teleport_scroll")

    override val item: ItemStack =
        ItemStack.of(Material.PAPER).apply {
            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<aqua>Teleport Scroll</aqua>"))
            editPersistentDataContainer { it.set(TELEPORT_SCROLL_KEY, PersistentDataType.BOOLEAN, true) }
        }

    /** Checks if the given [ItemStack] is a Teleport Scroll. */
    fun isTeleportScroll(item: ItemStack?): Boolean =
        item?.persistentDataContainer?.get(TELEPORT_SCROLL_KEY, PersistentDataType.BOOLEAN) == true
}
