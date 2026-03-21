package org.xodium.vanillaplus.dialogs

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents the FAQ dialog within the system. */
@Suppress("UnstableApiUsage")
internal object FaqDialog {
    val dialog =
        Dialog.create {
            it
                .empty()
                .base(DialogBase.builder(MM.deserialize("<b><gradient:#CB2D3E:#EF473A>FAQ</gradient></b>")).build())
                .type(
                    DialogType
                        .multiAction(
                            listOf(
                                ActionButton
                                    .builder(MM.deserialize("Enchantments"))
                                    .action(
                                        DialogAction.customClick(
                                            { _, audience -> audience.showDialog(EnchantmentsDialog.dialog) },
                                            ClickCallback.Options.builder().build(),
                                        ),
                                    ).build(),
                            ),
                        ).build(),
                )
        }
}
