@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm
import java.util.*

class OpenShulkerBoxListener : Listener {
    private val shulkerBoxSlots = HashMap<UUID, Int>()
    private val shulkerBoxOnCursors = HashSet<UUID>()
    private val pickupActions =
        EnumSet.of(
            InventoryAction.PICKUP_ALL,
            InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_ONE,
            InventoryAction.PICKUP_SOME,
            InventoryAction.SWAP_WITH_CURSOR,
        )

    private val placeActions =
        EnumSet.of(
            InventoryAction.PLACE_ALL,
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME,
            InventoryAction.SWAP_WITH_CURSOR,
        )

    private val dropSlotActions =
        EnumSet.of(
            InventoryAction.DROP_ALL_SLOT,
            InventoryAction.DROP_ONE_SLOT,
        )

    private val dropCursorActions =
        EnumSet.of(
            InventoryAction.DROP_ALL_CURSOR,
            InventoryAction.DROP_ONE_CURSOR,
        )

    private val hotbarActions =
        EnumSet.of(
            InventoryAction.HOTBAR_SWAP,
            InventoryAction.HOTBAR_MOVE_AND_READD,
        )

    companion object {
        private const val SHULKER_BOX_SIZE = 27
        private const val HOTBAR_START = 54
        private const val INVENTORY_START = 27
        private const val SPECIAL_SLOT_MARKER = -3141
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val player = event.player
        val itemInMainHand = player.inventory.itemInMainHand

        if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) &&
            itemInMainHand.isShulkerBox() && player.isSneaking
        ) {
            val meta = itemInMainHand.itemMeta as? BlockStateMeta ?: return
            val shulkerBox = meta.blockState as? ShulkerBox ?: return

            openShulkerBox(player, shulkerBox, meta)
            event.isCancelled = true
        }
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        val player = event.player

        if (shulkerBoxSlots.containsKey(player.uniqueId)) {
            val items = event.inventory.contents

            saveShulkerBox(player, items)
            shulkerBoxSlots.remove(player.uniqueId)
            player.world.playSound(player.location, Sound.BLOCK_SHULKER_BOX_CLOSE, .1f, 1.0f)
        }
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked

        if (shulkerBoxSlots.containsKey(player.uniqueId)) {
            if (event.cursor.isShulkerBox() && isInShulkerBox(event.rawSlot)) {
                event.isCancelled = true
                return
            }

            val items = event.inventory.contents

            saveShulkerBox(player, items)

            if (shulkerBoxSlots[player.uniqueId] == event.rawSlot) {
                if (pickupActions.contains(event.action)) {
                    shulkerBoxSlots[player.uniqueId] = SPECIAL_SLOT_MARKER
                    shulkerBoxOnCursors.add(player.uniqueId)
                    return
                } else if (dropSlotActions.contains(event.action)) {
                    dropItem(event.getCurrentItem()!!, player)
                    event.currentItem = null
                    player.closeInventory()
                    return
                }
            }

            var newItemSlot: Int? = null

            if (shulkerBoxOnCursors.contains(player.uniqueId)) {
                if (dropCursorActions.contains(event.action)) {
                    player.closeInventory()
                    return
                } else if (placeActions.contains(event.action)) {
                    newItemSlot = event.rawSlot
                    shulkerBoxOnCursors.remove(player.uniqueId)
                }
            }

            if (event.click == ClickType.NUMBER_KEY && hotbarActions.contains(event.action)) {
                if (isInShulkerBox(event.rawSlot) &&
                    player.inventory.getItem(event.hotbarButton) != null &&
                    player.inventory.getItem(event.hotbarButton)!!.isShulkerBox()
                ) {
                    event.isCancelled = true
                    return
                }

                if (shulkerBoxSlots[player.uniqueId] == event.rawSlot) {
                    newItemSlot = toRawSlot(event.hotbarButton)
                } else if (shulkerBoxSlots[player.uniqueId] == toRawSlot(event.hotbarButton)) {
                    newItemSlot = event.rawSlot
                }
            }

            if (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getCurrentItem() != null &&
                event.getCurrentItem()!!.isShulkerBox()
            ) {
                if (event.rawSlot in 54..<63) {
                    newItemSlot = moveItemToSlotRange(9, 36, event)
                } else if (event.rawSlot in 27..<54) {
                    newItemSlot = moveItemToSlotRange(0, 9, event)
                }

                if (newItemSlot != null && shulkerBoxSlots[player.uniqueId] != event.rawSlot) newItemSlot = null

                event.isCancelled = true
            }

            if (newItemSlot != null) shulkerBoxSlots[player.uniqueId] = newItemSlot
        }
    }

    @EventHandler
    fun on(event: InventoryDragEvent) {
        val player = event.whoClicked

        if (shulkerBoxSlots.containsKey(player.uniqueId) &&
            event.oldCursor.isShulkerBox()
        ) {
            if (event.rawSlots.stream().anyMatch { a: Int? -> a!! < 27 } ||
                event.rawSlots.size > 1
            ) {
                event.isCancelled = true
                return
            }

            if (shulkerBoxOnCursors.contains(player.uniqueId)) {
                shulkerBoxSlots[player.uniqueId] = toRawSlot(event.inventorySlots.toTypedArray()[0] as Int)
                shulkerBoxOnCursors.remove(player.uniqueId)
            }
        }
    }

    @EventHandler
    fun on(event: PlayerDropItemEvent) {
        val player = event.player

        if (shulkerBoxSlots.containsKey(player.uniqueId)) {
            val items = player.openInventory.topInventory.contents

            saveShulkerBox(player, items)
            player.world.playSound(player.location, Sound.BLOCK_SHULKER_BOX_CLOSE, .1f, 1.0f)
        }
    }

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId

        shulkerBoxSlots.remove(uuid)
        shulkerBoxOnCursors.remove(uuid)
    }

    private fun saveShulkerBox(
        player: HumanEntity,
        items: Array<ItemStack?>,
    ) {
        val shulkerbox =
            if (shulkerBoxOnCursors.contains(player.uniqueId)) {
                player.itemOnCursor
            } else {
                player.inventory.getItem(toSlot(shulkerBoxSlots[player.uniqueId]!!))
            }

        if (shulkerbox == null || !shulkerbox.isShulkerBox()) return

        val bsm = shulkerbox.itemMeta as BlockStateMeta
        val box = bsm.blockState as ShulkerBox

        box.inventory.contents = items
        bsm.blockState = box
        shulkerbox.setItemMeta(bsm)
    }

    private fun ItemStack.isShulkerBox(): Boolean = Tag.SHULKER_BOXES.isTagged(type)

    private fun isInShulkerBox(rawSlot: Int): Boolean = rawSlot in 0 until SHULKER_BOX_SIZE

    private fun toRawSlot(slot: Int): Int = if (slot in 0..<9) slot + HOTBAR_START else slot + (INVENTORY_START - 9)

    private fun toSlot(rawSlot: Int): Int = if (rawSlot >= HOTBAR_START) rawSlot - HOTBAR_START else rawSlot - (INVENTORY_START - 9)

    private fun moveItemToSlotRange(
        rangeMin: Int,
        rangeMax: Int,
        event: InventoryClickEvent,
    ): Int? {
        for (i in rangeMin..<rangeMax) {
            if (event.clickedInventory?.getItem(i) == null ||
                event.clickedInventory?.getItem(i)?.type == Material.AIR
            ) {
                event.clickedInventory?.setItem(i, event.getCurrentItem())
                event.currentItem = null

                return toRawSlot(i)
            }
        }
        return null
    }

    private fun dropItem(
        itemStack: ItemStack,
        player: HumanEntity,
    ) {
        val item = player.world.dropItem(player.eyeLocation, itemStack)

        item.velocity = player.location.getDirection().multiply(0.33)
        item.pickupDelay = 40
    }

    private fun openShulkerBox(
        player: Player,
        shulkerBox: ShulkerBox,
        meta: BlockStateMeta,
    ) {
        val title = meta.displayName() ?: "Shulker Box".mm()
        val inventory = instance.server.createInventory(null, SHULKER_BOX_SIZE, title)

        inventory.contents = shulkerBox.inventory.contents
        player.openInventory(inventory)
        shulkerBoxSlots[player.uniqueId] = toRawSlot(player.inventory.heldItemSlot)
        player.world.playSound(player.location, Sound.BLOCK_SHULKER_BOX_OPEN, .1f, 1.0f)
    }
}
