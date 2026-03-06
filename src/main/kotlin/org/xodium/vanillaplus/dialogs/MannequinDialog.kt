package org.xodium.vanillaplus.dialogs

import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.type.DialogType
import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.interfaces.DialogInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents an object handling mannequin dialog implementation within the system. */
@Suppress("UnstableApiUsage")
internal object MannequinDialog : DialogInterface {
    override fun invoke(builder: DialogRegistryEntry.Builder): DialogRegistryEntry.Builder =
        builder
            .base(
                DialogBase
                    .builder(MM.deserialize(config.mannequinModule.mannequinDialog.title))
                    .canCloseWithEscape(true)
                    .build(),
            ).type(DialogType.notice())

    @Serializable
    data class Config(
        var title: String = "<b><gradient:#CB2D3E:#EF473A>Mannequin Editor</gradient></b>",
    )
}
