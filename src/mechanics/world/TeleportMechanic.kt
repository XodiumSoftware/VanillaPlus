package org.xodium.illyriaplus.mechanics.world

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.TeleportAnchorData
import org.xodium.illyriaplus.interfaces.MechanicInterface
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window

/** Represents a mechanic handling teleportation within the system. */
internal object TeleportMechanic : MechanicInterface {
    /**
     * Represents the data structure for a collection of teleport anchors.
     *
     * @property anchors The list of [TeleportAnchorData] entries available for teleportation.
     */
    private data class Anchors(
        val anchors: MutableList<TeleportAnchorData> = mutableListOf(),
    )

    private const val UI_TITLE = "<b><gradient:#FFE259:#FFA751>Select Teleport Destination</gradient></b>"

    private val ANCHORS = Anchors()
    private val BACK_BUTTON =
        BoundItem
            .pagedBuilder()
            .setItemProvider(ItemBuilder(Material.ARROW).hideTooltip(true))
            .addClickHandler { _, gui, _ -> gui.page-- }
            .build()
    private val FORWARD_BUTTON =
        BoundItem
            .pagedBuilder()
            .setItemProvider(ItemBuilder(Material.ARROW).hideTooltip(true))
            .addClickHandler { _, gui, _ -> gui.page++ }
            .build()
    private val WINDOW =
        Window
            .builder()
            .setTitle(MM.deserialize(UI_TITLE))
            .setUpperGui(
                PagedGui
                    .itemsBuilder()
                    .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# # # < # > # # #",
                    ).addIngredient('#', Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)))
                    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('<', BACK_BUTTON)
                    .addIngredient('>', FORWARD_BUTTON)
                    .setContent(ANCHORS.anchors.map { anchorItem(it) })
                    .build(),
            )

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        when {
            event.action != Action.RIGHT_CLICK_BLOCK -> return
            event.clickedBlock?.type != Material.LODESTONE -> return
            event.item?.type == Material.COMPASS -> return
            event.item?.type == Material.NAME_TAG -> rename(event.clickedBlock ?: return, event.item ?: return)
            else -> WINDOW.open(event.player)
        }
    }

    @EventHandler
    fun on(event: BlockPlaceEvent) {
        val block = event.blockPlaced

        if (block.type != Material.LODESTONE) return

        val location = block.location

        if (ANCHORS.anchors.any {
                it.world == block.world &&
                    it.location.blockX == block.x &&
                    it.location.blockY == block.y &&
                    it.location.blockZ == block.z
            }
        ) {
            return
        }

        ANCHORS.anchors.add(TeleportAnchorData(block.world, location))
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        val block = event.block

        if (block.type != Material.LODESTONE) return

        ANCHORS.anchors.removeIf {
            it.world == block.world &&
                it.location.blockX == block.x &&
                it.location.blockY == block.y &&
                it.location.blockZ == block.z
        }
    }

    /**
     * Creates a GUI item representing a teleport anchor.
     *
     * @param anchor The [TeleportAnchorData] to represent.
     * @return The constructed [Item] for the GUI.
     */
    @Suppress("UnstableApiUsage")
    private fun anchorItem(anchor: TeleportAnchorData): Item =
        Item
            .builder()
            .setItemProvider(
                ItemStack
                    .of(Material.LODESTONE)
                    .apply {
                        setData(DataComponentTypes.ITEM_NAME, MM.deserialize(anchor.name))
                        setData(
                            DataComponentTypes.LORE,
                            ItemLore.lore(
                                listOf(
                                    Component.empty(),
                                    MM.deserialize("World: ${anchor.world.name}"),
                                    MM.deserialize(
                                        "Location: ${anchor.location.x}, ${anchor.location.y}, ${anchor.location.z}",
                                    ),
                                ),
                            ),
                        )
                    },
            ).addClickHandler { _, click -> click.player.teleport(anchor.location) }
            .build()

    /**
     * Renames a teleport anchor using the custom name from a name tag.
     *
     * @param block The [Block] representing the teleport anchor to rename.
     * @param item The [ItemStack] (expected to be a name tag) containing the new name.
     */
    @Suppress("UnstableApiUsage")
    private fun rename(
        block: Block,
        item: ItemStack,
    ) {
        val anchor =
            ANCHORS.anchors.firstOrNull {
                it.world == block.world &&
                    it.location.blockX == block.x &&
                    it.location.blockY == block.y &&
                    it.location.blockZ == block.z
            } ?: return
        val index = ANCHORS.anchors.indexOf(anchor)

        ANCHORS.anchors[index] =
            anchor.copy(name = item.getData(DataComponentTypes.CUSTOM_NAME)?.let { MM.serialize(it) } ?: return)
    }
}
