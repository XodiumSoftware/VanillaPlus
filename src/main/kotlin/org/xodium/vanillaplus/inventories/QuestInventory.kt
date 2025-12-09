package org.xodium.vanillaplus.inventories

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.InventoryInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents the inventory for quest-related items and interactions. */
internal object QuestInventory : InventoryInterface {
    /** Map of quest difficulties to their respective quest pools. */
    private val questPool: Map<QuestDifficulty, List<QuestData>> =
        mapOf(
            QuestDifficulty.EASY to
                listOf(
                    QuestData("Collect 5 wooden planks", 5),
                    QuestData("Mine 10 cobblestone", 8),
                    QuestData("Craft a wooden pickaxe", 10),
                    QuestData("Collect 3 wheat", 5),
                ),
            QuestDifficulty.MEDIUM to
                listOf(
                    QuestData("Craft a stone pickaxe", 20),
                    QuestData("Defeat 3 skeletons", 25),
                    QuestData("Mine 5 iron ore", 30),
                    QuestData("Cook 10 food items", 22),
                ),
            QuestDifficulty.HARD to
                listOf(
                    QuestData("Slay the cave spider boss", 100),
                    QuestData("Defeat 5 zombies without taking damage", 80),
                    QuestData("Mine 10 diamonds", 120),
                    QuestData("Complete a dungeon", 150),
                ),
        )

    private val _inventory: Inventory =
        instance.server.createInventory(this, 9, "<gradient:#CB2D3E:#EF473A>Quests</gradient>".mm()).apply {
            fill(Material.GRAY_STAINED_GLASS_PANE)

            // EASY Quests (slots 2, 3)
            setItem(2, randomQuestItem(QuestDifficulty.EASY))
            setItem(3, randomQuestItem(QuestDifficulty.EASY))

            // MEDIUM Quests (slots 4, 5)
            setItem(4, randomQuestItem(QuestDifficulty.MEDIUM))
            setItem(5, randomQuestItem(QuestDifficulty.MEDIUM))

            // HARD Quest (slot 6)
            setItem(6, randomQuestItem(QuestDifficulty.HARD))
        }

    override fun getInventory(): Inventory = _inventory

    /**
     * Creates a random quest item from the pool for the specified difficulty.
     * @param difficulty The difficulty level of the quest.
     * @param material The material of the quest item. Defaults to PAPER.
     * @return The created quest item as an ItemStack.
     */
    private fun randomQuestItem(
        difficulty: QuestDifficulty,
        material: Material = Material.PAPER,
    ): ItemStack {
        // TODO: Make it so the quests can only be used once every x time and then it resets. so we dont need to check for not available quests.
        val quest = questPool[difficulty]?.random() ?: QuestData("No quest available", 0)

        return questItem(difficulty, quest.objective, quest.crystals, material)
    }

    /**
     * Creates a quest item with the specified title, objective, rewards, and material.
     * @param difficulty The difficulty level of the quest.
     * @param objective The objective description of the quest.
     * @param crystals The number of crystals rewarded for completing the quest.
     * @param material The material of the quest item. Defaults to PAPER.
     * @return The created quest item as an ItemStack.
     */
    @Suppress("UnstableApiUsage")
    private fun questItem(
        difficulty: QuestDifficulty,
        objective: String,
        crystals: Int,
        material: Material = Material.PAPER,
    ): ItemStack =
        ItemStack.of(material).apply {
            setData(DataComponentTypes.CUSTOM_NAME, difficulty.title)
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        Component.empty(),
                        "<yellow>> Objective:".mm(),
                        "<white>$objective".mm(),
                        Component.empty(),
                        "<yellow>> Rewards:".mm(),
                        "<white>$crystals Crystals".mm(),
                    ),
                ),
            )
        }

    /**
     * Creates a crystal ItemStack with the specified amount.
     * @param amount The amount of crystals. Defaults to 1.
     * @return The created crystal ItemStack.
     */
    @Suppress("UnstableApiUsage")
    fun crystal(amount: Int = 1): ItemStack =
        ItemStack.of(Material.AMETHYST_SHARD, amount).apply {
            setData(DataComponentTypes.CUSTOM_NAME, "<gradient:#9b59b6:#8e44ad>Crystal</gradient>".mm())
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        Component.empty(),
                        "<yellow>A rare crystal used as currency,".mm(),
                        "<yellow>obtainable via quests.".mm(),
                    ),
                ),
            )
        }

    /**
     * Represents the difficulty levels for quests.
     * @param title The title component associated with the quest difficulty.
     * * EASY: Green title
     * * MEDIUM: Blue title
     * * HARD: Red title
     */
    enum class QuestDifficulty(
        val title: Component,
    ) {
        EASY("<green>Easy Quest".mm()),
        MEDIUM("<blue>Medium Quest".mm()),
        HARD("<red>Hard Quest".mm()),
    }

    /** Data class representing a quest with its objective and rewards. */
    private data class QuestData(
        val objective: String,
        val crystals: Int,
    )
}
