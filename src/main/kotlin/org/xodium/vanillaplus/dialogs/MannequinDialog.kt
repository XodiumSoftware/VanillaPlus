package org.xodium.vanillaplus.dialogs

import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents an object handling mannequin dialog implementation within the system. */
@Suppress("UnstableApiUsage", "Unused")
internal object MannequinDialog {
    /**
     * Creates and returns a configured dialog for editing the state of this [Mannequin].
     * @receiver The [Mannequin] instance for which the dialog is created.
     * @return A fully configured [Dialog] instance bound to this mannequin.
     */
    fun Mannequin.dialog(): Dialog =
        Dialog
            .create {
                it
                    .empty()
                    .base(
                        DialogBase
                            .builder(MM.deserialize("<b><gradient:#CB2D3E:#EF473A>Mannequin Editor</gradient></b>"))
                            .inputs(
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
                                    DialogInput
                                        .bool("immovable", MM.deserialize("Immovable"))
                                        .initial(isImmovable)
                                        .build(),
                                    DialogInput
                                        .bool("invulnerable", MM.deserialize("Invulnerable"))
                                        .initial(isInvulnerable)
                                        .build(),
                                    DialogInput
                                        .bool("gravity", MM.deserialize("Gravity"))
                                        .initial(hasGravity())
                                        .build(),
                                    DialogInput
                                        .bool("customNameVisible", MM.deserialize("Custom Name Visible"))
                                        .initial(isCustomNameVisible)
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

                                        customName(MM.deserialize(view.getText("customName") ?: ""))
                                        description = MM.deserialize(view.getText("description") ?: "")
                                        profile =
                                            ResolvableProfile.resolvableProfile().name(view.getText("profile")).build()
                                        isImmovable = view.getBoolean("immovable") ?: false
                                        isInvulnerable = view.getBoolean("invulnerable") ?: false
                                        setGravity(view.getBoolean("gravity") ?: false)
                                        isCustomNameVisible = view.getBoolean("customNameVisible") ?: false

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
