package org.xodium.vanillaplus.dialogs

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.ClickCallback
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
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
    val key: TypedKey<Dialog> get() = TypedKey.create(RegistryKey.DIALOG, Key.key(INSTANCE, "faq"))

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

    private val enchantsBook by lazy {
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
    }

    /**
     * Configures the properties of the dialog using the provided builder.
     * @param builder The builder used to define the dialog properties.
     * @return The builder for method chaining.
     */
    fun invoke(builder: DialogRegistryEntry.Builder): DialogRegistryEntry.Builder =
        builder
            .base(DialogBase.builder(MM.deserialize("FAQ")).build())
            .type(
                DialogType
                    .multiAction(
                        listOf(
                            ActionButton
                                .builder(MM.deserialize("Enchantments"))
                                .action(
                                    DialogAction.customClick(
                                        { _, audience -> (audience as? Player)?.openBook(enchantsBook) },
                                        ClickCallback.Options.builder().build(),
                                    ),
                                ).build(),
                        ),
                    ).build(),
            )

    /**
     * Retrieves the dialog from the registry.
     * @return The [Dialog] instance corresponding to the key.
     * @throws NoSuchElementException if the dialog is not found in the registry.
     */
    fun get(): Dialog = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG).getOrThrow(key)
}
