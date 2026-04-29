package org.xodium.illyriaplus.interfaces

import org.bukkit.entity.Player
import org.xodium.illyriaplus.data.KingdomData
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

/** Represents a contract for a gui within the system. */
internal interface GuiInterface {
    /**
     * Creates a window for the given kingdom.
     * @param player The player to open the window for.
     * @param kingdom The kingdom to create the window for.
     * @return The window configured for this kingdom.
     */
    fun window(
        player: Player,
        kingdom: KingdomData,
    ): Window

    /** The GUI instance containing the structure and items. */
    val gui: Gui
}
