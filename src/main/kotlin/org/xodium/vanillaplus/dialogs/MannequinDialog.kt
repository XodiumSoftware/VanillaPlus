package org.xodium.vanillaplus.dialogs

import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
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
                    .inputs(
                        listOf(
                            DialogInput.text("input", MM.deserialize("Name")).build(),
                            DialogInput.text("profile", MM.deserialize("Skin Profile (Mojang Username)")).build(),
                            DialogInput.bool("immovable", MM.deserialize("Immovable")).build(),
                            DialogInput.bool("gravity", MM.deserialize("Gravity")).build(),
                        ),
                    ).canCloseWithEscape(true)
                    .build(),
            ).type(
                DialogType.confirmation(
                    ActionButton.create(
                        MM.deserialize("Save"),
                        MM.deserialize("Click to confirm your input."),
                        100,
                        DialogAction.customClick(Key.key(INSTANCE, "mannequin/confirm"), null),
                    ),
                    ActionButton.create(
                        MM.deserialize("Discard"),
                        MM.deserialize("Click to discard your input."),
                        100,
                        null,
                    ),
                ),
            )

    @Serializable
    data class Config(
        var title: String = "<b><gradient:#CB2D3E:#EF473A>Mannequin Editor</gradient></b>",
    )
}
