package org.xodium.illyriaplus.items

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.ItemInterface

/** The Teleport Scroll item. */
@Suppress("UnstableApiUsage")
internal object TeleportScrollItem : ItemInterface {
    override val item: ItemStack =
        ItemStack.of(Material.PAPER).apply {
            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<aqua>Teleport Scroll</aqua>"))
        }
}
