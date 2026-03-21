package org.xodium.vanillaplus.dialogs

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player
import org.xodium.vanillaplus.data.BookData
import org.xodium.vanillaplus.enchantments.EarthrendEnchantment
import org.xodium.vanillaplus.enchantments.EmbertreadEnchantment
import org.xodium.vanillaplus.enchantments.FeatherFallingEnchantment
import org.xodium.vanillaplus.enchantments.NightsightEnchantment
import org.xodium.vanillaplus.enchantments.NimbusEnchantment
import org.xodium.vanillaplus.enchantments.SilkTouchEnchantment
import org.xodium.vanillaplus.enchantments.TetherEnchantment
import org.xodium.vanillaplus.enchantments.VerdanceEnchantment
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents the FAQ dialog within the system. */
@Suppress("UnstableApiUsage")
internal object FaqDialog {
    private val ENCHANTMENTS =
        listOf(
            EarthrendEnchantment,
            EmbertreadEnchantment,
            FeatherFallingEnchantment,
            NightsightEnchantment,
            NimbusEnchantment,
            SilkTouchEnchantment,
            TetherEnchantment,
            VerdanceEnchantment,
        )

    private val book =
        BookData(
            title = "<gold>Enchantments Guide",
            pages =
                listOf(
                    listOf(
                        "<b><gold>Enchantments",
                        "<b><gold>Guide",
                        "",
                        "<gray>Describes all custom",
                        "<gray>enchantments added",
                        "<gray>by VanillaPlus.",
                    ),
                ) + ENCHANTMENTS.flatMap { it.guide },
        ).toBook()

    val dialog =
        Dialog.create {
            it
                .empty()
                .base(DialogBase.builder(MM.deserialize("<gradient:#CB2D3E:#EF473A>FAQ</gradient>")).build())
                .type(
                    DialogType
                        .multiAction(
                            listOf(
                                ActionButton
                                    .builder(MM.deserialize("Enchantments"))
                                    .action(
                                        DialogAction.customClick(
                                            { _, audience -> (audience as? Player)?.openBook(book) },
                                            ClickCallback.Options.builder().build(),
                                        ),
                                    ).build(),
                            ),
                        ).build(),
                )
        }
}
