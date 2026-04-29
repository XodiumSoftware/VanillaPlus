package org.xodium.illyriaplus.guis

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.data.KingdomData
import org.xodium.illyriaplus.managers.KingdomManager
import org.xodium.illyriaplus.utils.Utils
import org.xodium.illyriaplus.utils.Utils.MM
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/** GUI for viewing and managing kingdom members with paginated list view. */
@OptIn(ExperimentalUuidApi::class)
@Suppress("UnstableApiUsage")
internal object KingdomMembersGui {
    /**
     * Returns an item representing a member with their head and info.
     * @param kingdom The kingdom this member belongs to
     * @param memberUuid The UUID of the member to display
     * @return An InvUI item with the member's head and click handler
     */
    private fun memberItem(
        kingdom: KingdomData,
        memberUuid: Uuid,
    ): Item {
        val offlinePlayer = instance.server.getOfflinePlayer(memberUuid.toJavaUuid())
        val playerName = offlinePlayer.name ?: "Unknown"

        return Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.PLAYER_HEAD).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<white>$playerName"))
                    setData(
                        DataComponentTypes.LORE,
                        ItemLore.lore(
                            listOf(
                                Component.empty(),
                                MM.deserialize("<gray>UUID: <white>$memberUuid"),
                                Component.empty(),
                                MM.deserialize("<yellow>Shift+Click to remove"),
                            ),
                        ),
                    )
                },
            ).addClickHandler { _, click ->
                if (click.clickType.isShiftClick) {
                    val updatedKingdom = KingdomManager.removeMember(kingdom.id, memberUuid)
                    if (updatedKingdom != null) {
                        click.player.sendMessage(MM.deserialize("<green>Removed '$playerName' from the kingdom."))
                        window(click.player, updatedKingdom).open()
                    }
                }
            }.build()
    }

    /**
     * Returns a back button item that returns to the main kingdom GUI.
     * @param kingdom The kingdom to display in the main GUI
     * @return An InvUI item that navigates back to the main kingdom GUI
     */
    private fun backToMainItem(kingdom: KingdomData): Item =
        Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.BARRIER).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<gray>Back to Kingdom"))
                },
            ).addClickHandler { _, click -> KingdomGui.window(click.player, kingdom).open() }
            .build()

    /**
     * Builds and returns the members GUI window for the given player.
     * @param player The player viewing the GUI
     * @param kingdom The kingdom whose members to display
     * @return The configured Window instance
     */
    fun window(
        player: Player,
        kingdom: KingdomData,
    ): Window =
        Window
            .builder()
            .setTitle(MM.deserialize("<b><gradient:#FFA751:#FFE259>Members of ${kingdom.name}</gradient></b>"))
            .setUpperGui(
                PagedGui
                    .itemsBuilder()
                    .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# # # < # > # B #",
                    ).addIngredient('#', Utils.GUI.FILLER_ITEM)
                    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('B', backToMainItem(kingdom))
                    .addIngredient('<', Utils.GUI.PREVIOUS_PAGE_ITEM)
                    .addIngredient('>', Utils.GUI.NEXT_PAGE_ITEM)
                    .setContent(kingdom.members.map { memberItem(kingdom, it) })
                    .build(),
            ).setViewer(player)
            .build()
}
