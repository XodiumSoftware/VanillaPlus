package org.xodium.illyriaplus.interfaces

import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

/** Represents a contract for a gui within the system. */
internal interface GuiInterface {
    /** The window instance that displays this GUI to the player. */
    val window: Window.Builder.Normal.Split

    /** The GUI instance containing the structure and items. */
    val gui: Gui
}
