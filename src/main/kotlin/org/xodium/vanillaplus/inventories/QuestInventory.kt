package org.xodium.vanillaplus.inventories

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.QuestModule
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents the inventory interface for quests. */
internal class QuestInventory : InventoryHolder {
    private val title = MM.deserialize("<b><gradient:#FFA751:#FFE259>Quests</gradient></b>")
    private val size = 9
    private val template: Inventory by lazy { instance.server.createInventory(this, size, title).apply { filler() } }

    override fun getInventory(): Inventory = template

    // TODO
    fun test(player: Player) {
        @Suppress("UnstableApiUsage")
        MenuType.GENERIC_9X1
            .builder()
            .title(title)
            .build(player)
            .apply {
                topInventory.setItem(3, ItemStack.of(Material.DIAMOND))
            }.open()
    }

    /**
     * Opens the quest inventory for the specified player.
     * @param player The player for whom to open the inventory.
     */
    fun openFor(player: Player) {
        val inv = instance.server.createInventory(this, size, title)

        inv.contents = template.contents

        val quests = QuestModule.getAssignedQuests(player)

        placeQuests(inv, quests, QuestModule.Quest.Difficulty.EASY, intArrayOf(0, 1))
        placeQuests(inv, quests, QuestModule.Quest.Difficulty.MEDIUM, intArrayOf(3, 4))
        placeQuests(inv, quests, QuestModule.Quest.Difficulty.HARD, intArrayOf(6))

        val allComplete = quests.isNotEmpty() && quests.all { it.requirement.isComplete }
        val allReward = QuestModule.config.questModule.allQuestsReward
        val requirementLine =
            if (allComplete) {
                "<gray>Requirement:</gray> <yellow>Complete all quests</yellow> <green><b>Completed</b></green>"
            } else {
                "<gray>Requirement:</gray> <yellow>Complete all quests</yellow>"
            }

        inv.setItem(
            8,
            createQuestItem(
                Material.ENDER_EYE,
                "<blue><b>Completing all quests reward</b></blue>",
                requirementLine,
                "<gray>Reward:</gray> <yellow>${allReward.description}</yellow>",
                glint = allComplete,
            ),
        )

        player.openInventory(inv)
    }

    private fun placeQuests(
        inv: Inventory,
        quests: List<QuestModule.Quest>,
        difficulty: QuestModule.Quest.Difficulty,
        slots: IntArray,
    ) {
        val picked = quests.filter { it.difficulty == difficulty }.take(slots.size)

        for (i in slots.indices) {
            val q = picked.getOrNull(i)

            inv.setItem(
                slots[i],
                if (q == null) {
                    createQuestItem(
                        Material.PAPER,
                        difficulty.description,
                        "<gray>Requirement: \u2014</gray>",
                        "<gray>Reward: \u2014</gray>",
                    )
                } else {
                    val req = q.requirement
                    val requirementLine =
                        if (req.isComplete) {
                            "<gray>Requirement:</gray> <yellow>${req.description}</yellow> <green><b>Completed</b></green>"
                        } else {
                            "<gray>Requirement:</gray> <yellow>${req.description}</yellow> <gray>(</gray><yellow>${req.currentProgress}</yellow><gray>/</gray><yellow>${req.amount}</yellow><gray>)</gray>"
                        }
                    val rewardLine = "<gray>Reward:</gray> <yellow>${q.reward.description}</yellow>"

                    createQuestItem(
                        Material.PAPER,
                        difficulty.description,
                        requirementLine,
                        rewardLine,
                        glint = req.isComplete,
                    )
                },
            )
        }
    }

    /**
     * Handles inventory click events to prevent interaction with the quest inventory.
     * @param event The inventory click event to handle.
     */
    fun inventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory?.holder is QuestInventory) event.isCancelled = true
    }

    /**
     * Fills empty slots in the inventory with black stained-glass panes as fillers.
     * @receiver The inventory to fill.
     */
    private fun Inventory.filler() {
        for (i in 0 until size) {
            if (getItem(i) == null) {
                setItem(
                    i,
                    @Suppress("UnstableApiUsage")
                    ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
                        setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize(""))
                    },
                )
            }
        }
    }

    /**
     * Helper function to create a quest item with the given parameters.
     * @param material The material for the item.
     * @param name The display name for the item.
     * @param line1 The first line of the lore.
     * @param line2 The second line of the lore.
     * @param glint Whether the item should have a glint effect.
     * @return The created ItemStack.
     */
    @Suppress("UnstableApiUsage")
    private fun createQuestItem(
        material: Material,
        name: String,
        line1: String,
        line2: String,
        glint: Boolean = false,
    ): ItemStack =
        ItemStack.of(material).apply {
            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize(name))
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        MM.deserialize(line1),
                        MM.deserialize(line2),
                    ),
                ),
            )
            setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint)
        }
}
