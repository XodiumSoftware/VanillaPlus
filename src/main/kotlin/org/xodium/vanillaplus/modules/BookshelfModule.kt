package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.util.Vector
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM
import org.bukkit.block.data.type.ChiseledBookshelf as ChiseledBookshelfData

/** Represents a module handling bookshelf mechanics within the system. */
internal object BookshelfModule : ModuleInterface {
    override val moduleConfig get() = config.bookshelfModule

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val player = event.player

        if (player.gameMode !in listOf(GameMode.SURVIVAL, GameMode.ADVENTURE)) return
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        if (event.useInteractedBlock() == Event.Result.DENY) return

        val block = event.clickedBlock ?: return
        val bookshelf = block.state as? ChiseledBookshelf ?: return
        val facing = (block.blockData as? ChiseledBookshelfData)?.facing ?: return
        if (event.blockFace != facing) return
        val slot =
            player
                .rayTraceBlocks(6.0)
                ?.takeIf { it.hitBlock == block }
                ?.hitPosition
                ?.toSlot(block, facing) ?: return
        val item = bookshelf.inventory.getItem(slot)

        if (item == null || item.type.isAir) return

        player.sendActionBar(
            when (val meta = item.itemMeta) {
                is BookMeta -> renderBook(meta, item.type.key.key)
                is EnchantmentStorageMeta -> renderEnchantedBook(meta)
                else -> MM.deserialize("<white><sprite:items:item/${item.type.key.key}></white>")
            },
        )
    }

    /**
     * Maps this hit [Vector] to a chiseled bookshelf slot index (0–5).
     * Slots are laid out in a 3×2 grid on the front face:
     * [0][1][2] (top row) / [3][4][5] (bottom row).
     * @param block The clicked block.
     * @return The slot index, or null if the facing direction is unsupported.
     */
    private fun Vector.toSlot(
        block: Block,
        facing: BlockFace,
    ): Int? {
        val relX = x - block.x
        val relY = y - block.y
        val relZ = z - block.z
        val localX =
            when (facing) {
                BlockFace.SOUTH -> relX
                BlockFace.NORTH -> 1.0 - relX
                BlockFace.EAST -> 1.0 - relZ
                BlockFace.WEST -> relZ
                else -> return null
            }
        val col = (localX * 3).toInt().coerceIn(0, 2)
        val row = if (relY >= 0.5) 0 else 1

        return row * 3 + col
    }

    /**
     * Renders a written or writable book component.
     * @param meta The book meta.
     * @return The rendered component.
     */
    private fun renderBook(
        meta: BookMeta,
        itemKey: String,
    ): Component {
        val title = meta.title
        val author = meta.author

        var message = MM.deserialize("<white><sprite:items:item/$itemKey></white>")

        if (title != null) message = message.append(MM.deserialize(" <white>$title</white>"))
        if (author != null) message = message.append(MM.deserialize("<gray> by $author</gray>"))

        return message
    }

    /**
     * Renders an enchanted book component.
     * @param meta The enchantment storage meta.
     * @return The rendered component.
     */
    private fun renderEnchantedBook(meta: EnchantmentStorageMeta): Component {
        val enchantments = meta.storedEnchants.entries.map { (enchantment, level) -> enchantment.displayName(level) }

        return MM
            .deserialize("<white><sprite:items:item/enchanted_book></white><gray> </gray>")
            .append(Component.join(JoinConfiguration.separator(MM.deserialize("<gray>, </gray>")), enchantments))
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        override var enabled: Boolean = false,
    ) : ModuleConfigInterface
}
