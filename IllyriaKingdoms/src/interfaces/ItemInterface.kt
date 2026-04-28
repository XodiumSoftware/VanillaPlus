package org.xodium.illyriaplus.interfaces

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

/** Represents a contract for items within the IllyriaKingdoms system. */
internal interface ItemInterface : Listener {
    /** MiniMessage instance for parsing rich text components. */
    val mm get() = MiniMessage.miniMessage()

    /** The configured ItemStack representing this item. */
    val item: ItemStack
}
