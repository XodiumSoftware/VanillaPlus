package org.xodium.illyriaplus.interfaces

import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

/** Represents a contract for a gui within the system. */
internal interface GuiInterface {
    /**
     * Creates a window for the given player.
     * @param player The player to create the window for.
     * @return The window builder configured for this player.
     */
    fun window(player: Player): Window

    /** The GUI instance containing the structure and items. */
    val gui: Gui
}
