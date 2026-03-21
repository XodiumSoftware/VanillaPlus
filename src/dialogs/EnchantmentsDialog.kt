package org.xodium.vanillaplus.dialogs

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import org.xodium.vanillaplus.enchantments.EarthrendEnchantment
import org.xodium.vanillaplus.enchantments.EmbertreadEnchantment
import org.xodium.vanillaplus.enchantments.FeatherFallingEnchantment
import org.xodium.vanillaplus.enchantments.NightsightEnchantment
import org.xodium.vanillaplus.enchantments.NimbusEnchantment
import org.xodium.vanillaplus.enchantments.SilkTouchEnchantment
import org.xodium.vanillaplus.enchantments.TetherEnchantment
import org.xodium.vanillaplus.enchantments.VerdanceEnchantment
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents the enchantments overview dialog within the system. */
@Suppress("UnstableApiUsage")
internal object EnchantmentsDialog {
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

    val dialog by lazy {
        Dialog.create { factory ->
            factory
                .empty()
                .base(
                    DialogBase
                        .builder(MM.deserialize("<gold>Enchantments</gold>"))
                        .body(ENCHANTMENTS.map { DialogBody.item(it.guide).showTooltip(true).build() })
                        .build(),
                ).type(DialogType.notice())
        }
    }
}
