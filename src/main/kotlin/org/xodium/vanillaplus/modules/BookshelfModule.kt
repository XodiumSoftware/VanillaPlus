package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling bookshelf mechanics within the system. */
internal object BookshelfModule : ModuleInterface {
    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val player = event.player

        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        if (event.useInteractedBlock() == Event.Result.DENY) return

        val bookshelf = event.clickedBlock?.state as? ChiseledBookshelf ?: return

        handleBookshelfInteraction(player, bookshelf)
    }

    /**
     * Handles the interaction logic for a chiseled bookshelf.
     * @param player The player interacting with the bookshelf.
     * @param bookshelf The clicked chiseled bookshelf.
     */
    private fun handleBookshelfInteraction(
        player: Player,
        bookshelf: ChiseledBookshelf,
    ) {
        val inventory = bookshelf.inventory
        val slots = 0 until inventory.size

        if (!slots.any { inventory.getItem(it) != null }) return

        player.sendMessage(MM.deserialize(config.bookshelfModule.header))

        for (i in slots) {
            val item = inventory.getItem(i) ?: continue
            val slotNumber = i + 1
            val slotPrefix = createSlotPrefix(slotNumber)

            when (val itemMeta = item.itemMeta) {
                is BookMeta -> player.sendMessage(renderBook(slotPrefix, itemMeta))
                is EnchantmentStorageMeta -> player.sendMessage(renderEnchantedBook(slotPrefix, itemMeta))
                else -> player.sendMessage(renderGenericItem(slotPrefix, item.type.key.key))
            }
        }

        player.sendMessage(MM.deserialize(config.bookshelfModule.footer))
    }

    /**
     * Renders a written or writable book message.
     * @param slotPrefix The formatted slot prefix component.
     * @param meta The book meta.
     * @return The rendered message component.
     */
    private fun renderBook(
        slotPrefix: Component,
        meta: BookMeta,
    ): Component {
        val title = meta.title
        val author = meta.author

        var message =
            slotPrefix.append(
                MM.deserialize(
                    " <white><sprite:items:item/${if (title != null) "written_book" else "book"}></white>",
                ),
            )

        if (title != null) message = message.append(MM.deserialize(" <white>$title</white>"))
        if (author != null) message = message.append(MM.deserialize("<gray> by $author</gray>"))

        return message
    }

    /**
     * Renders an enchanted book message.
     * @param slotPrefix The formatted slot prefix component.
     * @param meta The enchantment storage meta.
     * @return The rendered message component.
     */
    private fun renderEnchantedBook(
        slotPrefix: Component,
        meta: EnchantmentStorageMeta,
    ): Component {
        val enchantmentList = meta.storedEnchants.entries.joinToString(", ") { "${it.key.key.key} ${it.value}" }

        return slotPrefix.append(
            MM.deserialize(
                " <white><sprite:items:item/enchanted_book></white><gray> $enchantmentList</gray>",
            ),
        )
    }

    /**
     * Renders a generic item message.
     * @param slotPrefix The formatted slot prefix component.
     * @param itemKey The namespaced item key.
     * @return The rendered message component.
     */
    private fun renderGenericItem(
        slotPrefix: Component,
        itemKey: String,
    ): Component = slotPrefix.append(MM.deserialize(" <white><sprite:items:item/$itemKey></white>"))

    /**
     * Creates the formatted slot prefix component.
     * @param slotNumber The 1-based slot index displayed to the player.
     * @return The rendered slot prefix component.
     */
    private fun createSlotPrefix(slotNumber: Int): Component =
        MM.deserialize(
            config.bookshelfModule.slotPrefix,
            Placeholder.component("slot", MM.deserialize(slotNumber.toString())),
        )

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var header: String =
            "\n<gradient:#FFA751:#FFE259><st>───────────────────────────────────</st></gradient>",
        var footer: String =
            "<gradient:#FFA751:#FFE259><st>───────────────────────────────────</st></gradient>\n",
        var slotPrefix: String = "<gradient:#CB2D3E:#EF473A><slot>.</gradient>",
    )
}
