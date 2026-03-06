package org.xodium.vanillaplus.dialogs

import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import org.xodium.vanillaplus.interfaces.DialogInterface

/** Represents an object handling mannequin dialog implementation within the system. */
@Suppress("UnstableApiUsage")
internal object MannequinDialog : DialogInterface {
    override fun invoke(builder: DialogRegistryEntry.Builder): DialogRegistryEntry.Builder = builder
}
