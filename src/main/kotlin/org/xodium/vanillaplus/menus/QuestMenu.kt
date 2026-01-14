@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.menus

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.modules.QuestModule
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents the quest menu in the game. */
internal object QuestMenu {
    private val title = MM.deserialize("<b><gradient:#FFA751:#FFE259>Quests</gradient></b>")

    /**
     * Opens the quests menu for the player.
     * @receiver The player for whom to open the quests' menu.
     * @return The InventoryView of the opened quests' menu.
     */
    @Suppress("UnstableApiUsage")
    fun Player.questsMenu(): InventoryView =
        MenuType.GENERIC_9X1
            .builder()
            .title(title)
            .build(this)
            .apply {
                topInventory.apply {
                    val quests = QuestModule.getAssignedQuests(this@questsMenu)

                    filler()

                    placeQuests(this, quests, QuestModule.Quest.Difficulty.EASY, intArrayOf(0, 1))
                    placeQuests(this, quests, QuestModule.Quest.Difficulty.MEDIUM, intArrayOf(3, 4))
                    placeQuests(this, quests, QuestModule.Quest.Difficulty.HARD, intArrayOf(6))

                    val allComplete = quests.isNotEmpty() && quests.all { it.requirement.isComplete }
                    val allReward = QuestModule.config.questModule.allQuestsReward
                    val requirementLine =
                        if (allComplete) {
                            "<gray>Requirement:</gray> <yellow>Complete all quests</yellow> <green><b>Completed</b></green>"
                        } else {
                            "<gray>Requirement:</gray> <yellow>Complete all quests</yellow>"
                        }

                    setItem(
                        8,
                        createQuestItem(
                            Material.ENDER_EYE,
                            "<blue><b>Completing all quests reward</b></blue>",
                            requirementLine,
                            "<gray>Reward:</gray> <yellow>${allReward.description}</yellow>",
                            glint = allComplete,
                        ),
                    )
                }
            }

    /**
     * Places quest items in the inventory based on their difficulty.
     * @param inventory The inventory to place the quest items in.
     * @param quests The list of quests to choose from.
     * @param difficulty The difficulty level of the quests to place.
     * @param slots The slots in the inventory where the quest items should be placed.
     */
    private fun placeQuests(
        inventory: Inventory,
        quests: List<QuestModule.Quest>,
        difficulty: QuestModule.Quest.Difficulty,
        slots: IntArray,
    ) {
        val picked =
            quests
                .asSequence()
                .filter { it.difficulty == difficulty }
                .take(slots.size)
                .toList()

        for (i in slots.indices) {
            val q = picked.getOrNull(i)

            inventory.setItem(
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
        val view = event.view

        if (event.clickedInventory != view.topInventory) return
        if (view.title() == title) event.isCancelled = true
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
                ItemLore.lore(listOf(MM.deserialize(line1), MM.deserialize(line2))),
            )
            setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint)
        }
}
