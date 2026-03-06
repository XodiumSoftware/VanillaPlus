package org.xodium.vanillaplus.dialogs

import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.MannequinPDC.owner
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents an object handling mannequin dialog implementation within the system. */
@Suppress("Unused")
internal object MannequinDialog {
    /**
     * Creates and returns a configured dialog for editing the state of this [Mannequin].
     * @receiver The [Mannequin] instance for which the dialog is created.
     * @return A fully configured [Dialog] instance bound to this mannequin.
     */
    @Suppress("UnstableApiUsage")
    fun Mannequin.dialog(): Dialog =
        Dialog
            .create {
                it
                    .empty()
                    .base(
                        DialogBase
                            .builder(MM.deserialize("<b><gradient:#CB2D3E:#EF473A>Mannequin Editor</gradient></b>"))
                            .body(
                                listOf(
                                    DialogBody.plainMessage(
                                        MM.deserialize(
                                            "Owner: ${instance.server.getOfflinePlayer(owner).name ?: "Unknown"}",
                                        ),
                                    ),
                                ),
                            ).inputs(
                                listOf(
                                    DialogInput
                                        .text("customName", MM.deserialize("Custom Name"))
                                        .initial(MM.serializeOrNull(customName()) ?: "")
                                        .maxLength(1024)
                                        .build(),
                                    DialogInput
                                        .text("description", MM.deserialize("Description"))
                                        .initial(MM.serializeOrNull(description) ?: "")
                                        .maxLength(1024)
                                        .build(),
                                    DialogInput
                                        .text("profile", MM.deserialize("Skin Profile (Mojang Username)"))
                                        .initial(profile.name() ?: "")
                                        .maxLength(1024)
                                        .build(),
                                ),
                            ).canCloseWithEscape(true)
                            .build(),
                    ).type(
                        DialogType.confirmation(
                            ActionButton.create(
                                MM.deserialize("Save"),
                                MM.deserialize("Click to confirm your input."),
                                100,
                                DialogAction.customClick(
                                    { view, audience ->
                                        if (audience !is Player) return@customClick
                                        if (isDead) {
                                            audience.sendActionBar(MM.deserialize("<red>Mannequin is dead.</red>"))
                                        }

                                        val distance = audience.location.distanceSquared(location)

                                        if (audience.world != world || distance > 36.0) {
                                            audience.sendActionBar(
                                                MM.deserialize("<red>You are too far away from the mannequin.</red>"),
                                            )
                                            return@customClick
                                        }

                                        val newName = view.getText("customName") ?: ""

                                        customName(if (newName.isEmpty()) null else MM.deserialize(newName))
                                        description = MM.deserialize(view.getText("description") ?: "")
                                        profile =
                                            ResolvableProfile.resolvableProfile().name(view.getText("profile")).build()

                                        audience.sendActionBar(
                                            MM.deserialize("<green>Changes successfully applied.</green>"),
                                        )
                                    },
                                    ClickCallback.Options
                                        .builder()
                                        .uses(1)
                                        .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                        .build(),
                                ),
                            ),
                            ActionButton.create(
                                MM.deserialize("Discard"),
                                MM.deserialize("Click to discard your input."),
                                100,
                                null,
                            ),
                        ),
                    )
            }
}
